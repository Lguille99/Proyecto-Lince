begin;

create schema lince_privado;
revoke all on schema lince_privado from public, anon, authenticated;
grant usage on schema lince_privado to authenticated;

create function lince_privado.usuario_actual() returns uuid
language sql stable security definer set search_path = '' as $$
    select id from public.perfiles where auth_user_id = auth.uid() and activo;
$$;

create function lince_privado.es_coordinador() returns boolean
language sql stable security definer set search_path = '' as $$
    select exists(select 1 from public.perfiles
        where auth_user_id = auth.uid() and activo and rol = 'COORDINADOR');
$$;

create function lince_privado.validar_asignacion() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if not exists(select 1 from public.perfiles
        where id = new.usuario_id and activo and rol in ('CONDUCTOR', 'GUIA')) then
        raise exception 'Solo se asignan servicios a conductores o guías activos.';
    end if;
    if tg_op = 'UPDATE' then
        if new.id <> old.id or new.servicio_id <> old.servicio_id or new.usuario_id <> old.usuario_id then
            raise exception 'La identidad de una asignación no se puede cambiar.';
        end if;
        if old.estado <> 'PENDIENTE' and row(new.estado, new.motivo_rechazo, new.respondido_en)
            is distinct from row(old.estado, old.motivo_rechazo, old.respondido_en) then
            raise exception 'La asignación ya fue respondida.';
        end if;
    end if;
    return new;
end;
$$;
create trigger validar_asignacion before insert or update on public.asignaciones
for each row execute function lince_privado.validar_asignacion();

create function lince_privado.validar_ejecucion() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if not exists(select 1 from public.asignaciones a join public.perfiles p on p.id = a.usuario_id
        where a.id = new.asignacion_id and a.estado = 'CONFIRMADO' and p.rol = 'CONDUCTOR' and p.activo) then
        raise exception 'La ejecución requiere una asignación confirmada de un conductor activo.';
    end if;
    if tg_op = 'UPDATE' then
        if new.asignacion_id <> old.asignacion_id or new.inicio_en <> old.inicio_en
            or new.observaciones_inicio <> old.observaciones_inicio then
            raise exception 'El check-in no se puede modificar.';
        end if;
        if old.termino_en is not null then raise exception 'El servicio ya finalizó.'; end if;
    end if;
    return new;
end;
$$;
create trigger validar_ejecucion before insert or update on public.ejecuciones
for each row execute function lince_privado.validar_ejecucion();

create function lince_privado.validar_disponibilidad() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if not exists(select 1 from public.perfiles where id = new.usuario_id
        and activo and rol in ('CONDUCTOR', 'GUIA')) then
        raise exception 'La disponibilidad corresponde a un conductor o guía activo.';
    end if;
    if new.fecha < (current_timestamp at time zone 'America/Santiago')::date then
        raise exception 'No se puede modificar la disponibilidad de una fecha pasada.';
    end if;
    if tg_op = 'UPDATE' and (new.usuario_id <> old.usuario_id or new.fecha <> old.fecha) then
        raise exception 'La cuenta y la fecha de un registro no se pueden cambiar.';
    end if;
    return new;
end;
$$;
create trigger validar_disponibilidad before insert or update on public.disponibilidad
for each row execute function lince_privado.validar_disponibilidad();

create function public.actualizar_mi_perfil(p_nombre text, p_correo text) returns public.perfiles
language plpgsql security definer set search_path = '' as $$
declare v_usuario uuid := lince_privado.usuario_actual(); v_resultado public.perfiles;
begin
    if v_usuario is null then raise exception 'Debes iniciar sesión con un perfil activo.'; end if;
    if p_nombre is null or char_length(btrim(p_nombre)) not between 1 and 120
        or p_correo is null or char_length(p_correo) > 254 or position('@' in p_correo) < 2 then
        raise exception 'Nombre o correo de contacto inválido.';
    end if;
    update public.perfiles set nombre = btrim(p_nombre), correo = btrim(p_correo)
        where id = v_usuario returning * into v_resultado;
    return v_resultado;
end;
$$;

create function public.responder_asignacion(p_asignacion uuid, p_confirmar boolean, p_motivo text default null)
returns public.asignaciones language plpgsql security definer set search_path = '' as $$
declare v_usuario uuid := lince_privado.usuario_actual(); v_asignacion public.asignaciones;
begin
    if v_usuario is null or p_confirmar is null then raise exception 'Sesión o respuesta inválida.'; end if;
    select * into v_asignacion from public.asignaciones where id = p_asignacion for update;
    if not found or v_asignacion.usuario_id <> v_usuario or lince_privado.es_coordinador() then
        raise exception 'No puedes responder esta asignación.';
    end if;
    if v_asignacion.estado <> 'PENDIENTE' then raise exception 'La asignación ya fue respondida.'; end if;
    if not p_confirmar and (p_motivo is null or char_length(btrim(p_motivo)) not between 1 and 300) then
        raise exception 'Indica un motivo de rechazo de entre 1 y 300 caracteres.';
    end if;
    update public.asignaciones set estado = case when p_confirmar then 'CONFIRMADO' else 'RECHAZADO' end,
        motivo_rechazo = case when p_confirmar then null else btrim(p_motivo) end,
        respondido_en = clock_timestamp() where id = p_asignacion returning * into v_asignacion;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, case when p_confirmar then 'CONFIRMACION' else 'RECHAZO' end,
            case when p_confirmar then 'Asignación confirmada' else 'Asignación rechazada: ' || btrim(p_motivo) end);
    return v_asignacion;
end;
$$;

create function public.guardar_disponibilidad(p_fecha date, p_bloqueado boolean)
returns public.disponibilidad language plpgsql security definer set search_path = '' as $$
declare v_usuario uuid := lince_privado.usuario_actual(); v_resultado public.disponibilidad;
    v_anterior boolean;
begin
    if v_usuario is null or p_fecha is null or p_bloqueado is null then
        raise exception 'Sesión, fecha o estado inválido.';
    end if;
    perform 1 from public.perfiles where id = v_usuario for update;
    select bloqueado into v_anterior from public.disponibilidad where usuario_id = v_usuario and fecha = p_fecha;
    insert into public.disponibilidad(usuario_id, fecha, bloqueado)
        values(v_usuario, p_fecha, p_bloqueado)
        on conflict(usuario_id, fecha) do update set bloqueado = excluded.bloqueado, actualizado_en = clock_timestamp()
        returning * into v_resultado;
    if v_anterior is distinct from p_bloqueado then
        insert into public.historial(usuario_id, accion, detalle)
            values(v_usuario, 'DISPONIBILIDAD', p_fecha::text || case when p_bloqueado then ': día bloqueado' else ': día disponible' end);
    end if;
    return v_resultado;
end;
$$;

create function public.registrar_check_in(p_asignacion uuid, p_observaciones text default '')
returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare v_usuario uuid := lince_privado.usuario_actual(); v_asignacion public.asignaciones; v_resultado public.ejecuciones;
begin
    if v_usuario is null then raise exception 'Debes iniciar sesión con un perfil activo.'; end if;
    select * into v_asignacion from public.asignaciones where id = p_asignacion for update;
    if not found or v_asignacion.usuario_id <> v_usuario then raise exception 'Asignación no autorizada.'; end if;
    if exists(select 1 from public.ejecuciones where asignacion_id = p_asignacion) then
        raise exception 'Ya registraste el check-in.';
    end if;
    insert into public.ejecuciones(asignacion_id, inicio_en, observaciones_inicio)
        values(p_asignacion, clock_timestamp(), coalesce(p_observaciones, '')) returning * into v_resultado;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CHECK_IN', 'Inicio del servicio registrado');
    return v_resultado;
end;
$$;

create function public.registrar_check_out(p_asignacion uuid, p_observaciones text default '')
returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare v_usuario uuid := lince_privado.usuario_actual(); v_asignacion public.asignaciones; v_resultado public.ejecuciones;
begin
    if v_usuario is null then raise exception 'Debes iniciar sesión con un perfil activo.'; end if;
    select * into v_asignacion from public.asignaciones where id = p_asignacion for update;
    if not found or v_asignacion.usuario_id <> v_usuario then raise exception 'Asignación no autorizada.'; end if;
    update public.ejecuciones set termino_en = clock_timestamp(), observaciones_termino = coalesce(p_observaciones, '')
        where asignacion_id = p_asignacion and termino_en is null returning * into v_resultado;
    if not found then raise exception 'Primero registra check-in; solo se permite un check-out.'; end if;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CHECK_OUT', 'Término del servicio registrado');
    return v_resultado;
end;
$$;

create function public.simular_sincronizacion() returns public.sincronizaciones
language plpgsql security definer set search_path = '' as $$
declare v_resultado public.sincronizaciones;
begin
    if not lince_privado.es_coordinador() then raise exception 'Solo coordinación puede simular sincronización.'; end if;
    insert into public.sincronizaciones(usuario_id) values(lince_privado.usuario_actual()) returning * into v_resultado;
    return v_resultado;
end;
$$;

create policy perfiles_lectura on public.perfiles for select to authenticated
    using (id = (select lince_privado.usuario_actual()) or (select lince_privado.es_coordinador()));
create policy asignaciones_lectura on public.asignaciones for select to authenticated
    using (usuario_id = (select lince_privado.usuario_actual()) or (select lince_privado.es_coordinador()));
create policy servicios_lectura on public.servicios for select to authenticated
    using ((select lince_privado.es_coordinador()) or exists(select 1 from public.asignaciones a
        where a.servicio_id = servicios.id and a.usuario_id = (select lince_privado.usuario_actual())));
create policy disponibilidad_lectura on public.disponibilidad for select to authenticated
    using (usuario_id = (select lince_privado.usuario_actual()) or (select lince_privado.es_coordinador()));
create policy ejecuciones_lectura on public.ejecuciones for select to authenticated
    using ((select lince_privado.es_coordinador()) or exists(select 1 from public.asignaciones a
        where a.id = ejecuciones.asignacion_id and a.usuario_id = (select lince_privado.usuario_actual())));
create policy historial_lectura on public.historial for select to authenticated
    using (usuario_id = (select lince_privado.usuario_actual()) or (select lince_privado.es_coordinador()));
create policy sincronizaciones_lectura on public.sincronizaciones for select to authenticated
    using ((select lince_privado.es_coordinador()));

grant select on public.perfiles, public.servicios, public.asignaciones, public.disponibilidad,
    public.ejecuciones, public.historial, public.sincronizaciones to authenticated;
revoke all on all functions in schema lince_privado from public, anon, authenticated;
grant execute on function lince_privado.usuario_actual(), lince_privado.es_coordinador() to authenticated;
revoke all on function public.actualizar_mi_perfil(text, text),
    public.responder_asignacion(uuid, boolean, text), public.guardar_disponibilidad(date, boolean),
    public.registrar_check_in(uuid, text), public.registrar_check_out(uuid, text),
    public.simular_sincronizacion() from public, anon, authenticated;
grant execute on function public.actualizar_mi_perfil(text, text),
    public.responder_asignacion(uuid, boolean, text), public.guardar_disponibilidad(date, boolean),
    public.registrar_check_in(uuid, text), public.registrar_check_out(uuid, text),
    public.simular_sincronizacion() to authenticated;

commit;
