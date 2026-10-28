begin;

alter table public.perfiles
    add column if not exists telefono text not null default '',
    add column if not exists zona text not null default '',
    add column if not exists idiomas text[] not null default array['ESPANOL']::text[],
    add column if not exists estado_disponibilidad text not null default 'DISPONIBLE';

alter table public.perfiles
    add constraint perfiles_estado_disponibilidad_check
    check (estado_disponibilidad in ('DISPONIBLE', 'EN_SERVICIO', 'NO_DISPONIBLE'));
alter table public.perfiles
    add constraint perfiles_idiomas_check
    check (cardinality(idiomas) > 0 and idiomas <@ array['ESPANOL', 'INGLES']::text[]);

alter table public.servicios
    add column if not exists idioma_requerido text not null default 'ESPANOL',
    add column if not exists modalidad text not null default 'TRANSFER',
    add column if not exists almuerzo_incluido boolean not null default false,
    add column if not exists itinerario jsonb not null default '[]'::jsonb,
    add column if not exists resena_historica text not null default '',
    add column if not exists hotel_recogida text not null default '',
    add column if not exists tarifa integer not null default 0,
    add column if not exists pasajeros integer not null default 1,
    add column if not exists dias_tour integer not null default 1;

alter table public.servicios
    add constraint servicios_idioma_requerido_check
    check (idioma_requerido in ('ESPANOL', 'INGLES'));
alter table public.servicios
    add constraint servicios_modalidad_check
    check (modalidad in ('TRANSFER', 'MEDIO_DIA', 'DIA_COMPLETO'));
alter table public.servicios
    add constraint servicios_itinerario_check
    check (jsonb_typeof(itinerario) = 'array');
alter table public.servicios
    add constraint servicios_tarifa_check check (tarifa >= 0);
alter table public.servicios
    add constraint servicios_pasajeros_check check (pasajeros > 0);
alter table public.servicios
    add constraint servicios_dias_tour_check check (dias_tour > 0);
alter table public.servicios
    add constraint servicios_resena_check check (char_length(resena_historica) <= 1000);

update public.servicios
set hotel_recogida = recogida
where btrim(hotel_recogida) = '';

alter table public.asignaciones
    add column if not exists acompanante_id uuid references public.perfiles(id) on delete set null,
    add column if not exists reasignado_por uuid references public.perfiles(id) on delete set null,
    add column if not exists reasignado_en timestamptz;

alter table public.asignaciones
    add constraint asignaciones_acompanante_distinto_check
    check (acompanante_id is null or acompanante_id <> usuario_id);

create index if not exists asignaciones_acompanante_idx
    on public.asignaciones(acompanante_id);

alter table public.ejecuciones
    add column if not exists ubicacion_inicio text not null default '';

alter table public.ejecuciones
    add constraint ejecuciones_ubicacion_check
    check (char_length(ubicacion_inicio) between 1 and 200);

alter table public.historial
    drop constraint if exists historial_accion_check;
alter table public.historial
    add constraint historial_accion_check
    check (accion in ('CONFIRMACION', 'RECHAZO', 'CHECK_IN', 'CHECK_OUT', 'DISPONIBILIDAD', 'REASIGNACION', 'CALIFICACION'));

create table if not exists public.calificaciones (
    id uuid primary key default gen_random_uuid(),
    asignacion_id uuid not null unique references public.asignaciones(id) on delete cascade,
    puntaje integer not null check (puntaje between 1 and 5),
    calificado_por uuid not null references public.perfiles(id) on delete restrict,
    calificado_en timestamptz not null default now()
);

alter table public.calificaciones enable row level security;

create or replace function lince_privado.validar_asignacion() returns trigger
language plpgsql security definer set search_path = '' as $$
declare
    v_reasignacion boolean := false;
begin
    if not exists(select 1 from public.perfiles
        where id = new.usuario_id and activo and rol in ('CONDUCTOR', 'GUIA')) then
        raise exception 'Solo se asignan servicios a conductores o guías activos.';
    end if;
    if new.acompanante_id is not null and not exists(select 1 from public.perfiles
        where id = new.acompanante_id and activo and rol in ('CONDUCTOR', 'GUIA')) then
        raise exception 'El acompañante debe ser conductor o guía activo.';
    end if;
    if new.acompanante_id is not null and new.acompanante_id = new.usuario_id then
        raise exception 'El titular y el acompañante deben ser personas distintas.';
    end if;
    if tg_op = 'UPDATE' then
        v_reasignacion := (new.reasignado_en is distinct from old.reasignado_en);
        if new.id <> old.id or new.servicio_id <> old.servicio_id then
            raise exception 'La identidad de una asignación no se puede cambiar.';
        end if;
        if new.usuario_id <> old.usuario_id
            and not (v_reasignacion and lince_privado.es_coordinador()) then
            raise exception 'El titular de una asignación no se puede cambiar directamente.';
        end if;
        if old.estado <> 'PENDIENTE'
            and row(new.estado, new.motivo_rechazo, new.respondido_en)
                is distinct from row(old.estado, old.motivo_rechazo, old.respondido_en)
            and not (v_reasignacion and lince_privado.es_coordinador()) then
            raise exception 'La asignación ya fue respondida.';
        end if;
    end if;
    return new;
end;
$$;

create or replace function lince_privado.validar_ejecucion() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if not exists(select 1 from public.asignaciones a
        join public.perfiles p on p.id = a.usuario_id
        where a.id = new.asignacion_id
          and a.estado = 'CONFIRMADO'
          and p.rol in ('CONDUCTOR', 'GUIA')
          and p.activo) then
        raise exception 'La ejecución requiere una asignación confirmada de un titular activo.';
    end if;
    if tg_op = 'UPDATE' then
        if new.asignacion_id <> old.asignacion_id or new.inicio_en <> old.inicio_en
            or new.observaciones_inicio <> old.observaciones_inicio
            or new.ubicacion_inicio <> old.ubicacion_inicio then
            raise exception 'El check-in no se puede modificar.';
        end if;
        if old.termino_en is not null then raise exception 'El servicio ya finalizó.'; end if;
    end if;
    return new;
end;
$$;

create or replace function public.guardar_disponibilidad(p_fecha date, p_bloqueado boolean)
returns public.disponibilidad language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_resultado public.disponibilidad;
    v_anterior boolean;
    v_ocupado boolean;
begin
    if v_usuario is null or p_fecha is null or p_bloqueado is null then
        raise exception 'Sesión, fecha o estado inválido.';
    end if;
    if p_fecha < (current_timestamp at time zone 'America/Santiago')::date then
        raise exception 'No se puede modificar la disponibilidad de una fecha pasada.';
    end if;
    if p_bloqueado then
        if p_fecha < (current_timestamp at time zone 'America/Santiago')::date + 7 then
            raise exception 'Debes avisar con al menos 7 días de anticipación.';
        end if;
        select exists(
            select 1
            from public.asignaciones a
            join public.servicios s on s.id = a.servicio_id
            left join public.ejecuciones e on e.asignacion_id = a.id
            where (a.usuario_id = v_usuario or a.acompanante_id = v_usuario)
              and s.fecha = p_fecha
              and a.estado <> 'RECHAZADO'
              and e.termino_en is null
        ) into v_ocupado;
        if v_ocupado then
            raise exception 'No puedes bloquear un día con un servicio asignado.';
        end if;
    end if;
    perform 1 from public.perfiles where id = v_usuario for update;
    select bloqueado into v_anterior
        from public.disponibilidad
        where usuario_id = v_usuario and fecha = p_fecha;
    insert into public.disponibilidad(usuario_id, fecha, bloqueado)
        values(v_usuario, p_fecha, p_bloqueado)
        on conflict(usuario_id, fecha) do update
            set bloqueado = excluded.bloqueado, actualizado_en = clock_timestamp()
        returning * into v_resultado;
    if v_anterior is distinct from p_bloqueado then
        insert into public.historial(usuario_id, accion, detalle)
            values(v_usuario, 'DISPONIBILIDAD',
                p_fecha::text || case when p_bloqueado then ': día bloqueado' else ': día disponible' end);
    end if;
    return v_resultado;
end;
$$;

drop function if exists public.registrar_check_in(uuid, text);

create function public.registrar_check_in(
    p_asignacion uuid,
    p_ubicacion text,
    p_observaciones text default ''
) returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_asignacion public.asignaciones;
    v_resultado public.ejecuciones;
    v_ubicacion text := btrim(coalesce(p_ubicacion, ''));
begin
    if v_usuario is null then
        raise exception 'Debes iniciar sesión con un perfil activo.';
    end if;
    if v_ubicacion = '' or char_length(v_ubicacion) > 200 then
        raise exception 'Indica una ubicación válida de hasta 200 caracteres.';
    end if;
    select * into v_asignacion
        from public.asignaciones
        where id = p_asignacion
        for update;
    if not found or v_asignacion.usuario_id <> v_usuario then
        raise exception 'Asignación no autorizada.';
    end if;
    if v_asignacion.estado <> 'CONFIRMADO' then
        raise exception 'El servicio debe estar confirmado.';
    end if;
    if exists(select 1 from public.ejecuciones where asignacion_id = p_asignacion) then
        raise exception 'Ya registraste el check-in.';
    end if;
    insert into public.ejecuciones(asignacion_id, inicio_en, ubicacion_inicio, observaciones_inicio)
        values(p_asignacion, clock_timestamp(), v_ubicacion, coalesce(p_observaciones, ''))
        returning * into v_resultado;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CHECK_IN', 'Inicio del servicio registrado en ' || v_ubicacion);
    return v_resultado;
end;
$$;

drop function if exists public.actualizar_mi_perfil(text, text);

create function public.actualizar_mi_perfil(
    p_nombre text,
    p_correo text,
    p_telefono text,
    p_zona text,
    p_idiomas text[]
) returns public.perfiles language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_resultado public.perfiles;
    v_nombre text := btrim(coalesce(p_nombre, ''));
    v_correo text := btrim(coalesce(p_correo, ''));
    v_telefono text := btrim(coalesce(p_telefono, ''));
    v_zona text := btrim(coalesce(p_zona, ''));
begin
    if v_usuario is null then
        raise exception 'Debes iniciar sesión con un perfil activo.';
    end if;
    if char_length(v_nombre) not between 2 and 60 then
        raise exception 'El nombre debe tener entre 2 y 60 caracteres.';
    end if;
    if char_length(v_correo) > 254 or position('@' in v_correo) < 2
        or position('.' in split_part(v_correo, '@', 2)) < 2 then
        raise exception 'Ingresa un correo válido.';
    end if;
    if char_length(v_telefono) not between 8 and 30 then
        raise exception 'Ingresa un teléfono válido.';
    end if;
    if char_length(v_zona) not between 1 and 80 then
        raise exception 'Indica tu zona de trabajo.';
    end if;
    if p_idiomas is null or cardinality(p_idiomas) = 0
        or not (p_idiomas <@ array['ESPANOL', 'INGLES']::text[]) then
        raise exception 'Selecciona al menos un idioma válido.';
    end if;
    update public.perfiles
    set nombre = v_nombre,
        correo = v_correo,
        telefono = v_telefono,
        zona = v_zona,
        idiomas = p_idiomas
    where id = v_usuario
    returning * into v_resultado;
    return v_resultado;
end;
$$;

create or replace function public.calificar_asignacion(
    p_asignacion uuid,
    p_puntaje integer
) returns public.calificaciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_resultado public.calificaciones;
begin
    if v_usuario is null or not lince_privado.es_coordinador() then
        raise exception 'Solo coordinación califica los servicios.';
    end if;
    if p_puntaje is null or p_puntaje not between 1 and 5 then
        raise exception 'La calificación debe estar entre 1 y 5.';
    end if;
    if not exists(select 1 from public.asignaciones where id = p_asignacion) then
        raise exception 'Asignación no encontrada.';
    end if;
    if not exists(select 1 from public.ejecuciones
        where asignacion_id = p_asignacion and termino_en is not null) then
        raise exception 'Solo se califican servicios completados.';
    end if;
    insert into public.calificaciones(asignacion_id, puntaje, calificado_por)
        values(p_asignacion, p_puntaje, v_usuario)
        on conflict (asignacion_id) do update
            set puntaje = excluded.puntaje,
                calificado_por = excluded.calificado_por,
                calificado_en = clock_timestamp()
        returning * into v_resultado;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CALIFICACION', 'Calificación registrada: ' || p_puntaje);
    return v_resultado;
end;
$$;

create or replace function public.reasignar_asignacion(
    p_asignacion uuid,
    p_nuevo_usuario uuid
) returns public.asignaciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_asignacion public.asignaciones;
    v_actual public.perfiles;
    v_nuevo public.perfiles;
    v_idioma text;
begin
    if v_usuario is null or not lince_privado.es_coordinador() then
        raise exception 'Solo coordinación reasigna servicios.';
    end if;
    select * into v_asignacion
        from public.asignaciones
        where id = p_asignacion
        for update;
    if not found then
        raise exception 'Asignación no encontrada.';
    end if;
    select * into v_actual from public.perfiles where id = v_asignacion.usuario_id;
    select * into v_nuevo from public.perfiles
        where id = p_nuevo_usuario and activo and rol in ('CONDUCTOR', 'GUIA');
    if not found then
        raise exception 'La persona seleccionada no está disponible.';
    end if;
    if v_nuevo.id = v_asignacion.usuario_id or v_nuevo.id = v_asignacion.acompanante_id then
        raise exception 'La persona ya está asignada a este servicio.';
    end if;
    if v_nuevo.rol <> v_actual.rol then
        raise exception 'El reemplazo debe tener el mismo rol que el titular.';
    end if;
    select idioma_requerido into v_idioma from public.servicios where id = v_asignacion.servicio_id;
    if not (v_idioma = any(v_nuevo.idiomas)) then
        raise exception 'La persona no maneja el idioma requerido.';
    end if;
    delete from public.calificaciones where asignacion_id = p_asignacion;
    delete from public.ejecuciones where asignacion_id = p_asignacion;
    update public.asignaciones
    set usuario_id = p_nuevo_usuario,
        estado = 'PENDIENTE',
        motivo_rechazo = null,
        respondido_en = null,
        reasignado_por = v_usuario,
        reasignado_en = clock_timestamp()
    where id = p_asignacion
    returning * into v_asignacion;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'REASIGNACION',
            'Asignación reasignada a ' || v_nuevo.nombre);
    return v_asignacion;
end;
$$;

drop policy if exists perfiles_lectura on public.perfiles;
create policy perfiles_lectura on public.perfiles for select to authenticated
    using (
        id = (select lince_privado.usuario_actual())
        or (select lince_privado.es_coordinador())
        or exists(
            select 1 from public.asignaciones a
            where (a.usuario_id = perfiles.id or a.acompanante_id = perfiles.id)
              and (a.usuario_id = (select lince_privado.usuario_actual())
                   or a.acompanante_id = (select lince_privado.usuario_actual()))
        )
    );

drop policy if exists asignaciones_lectura on public.asignaciones;
create policy asignaciones_lectura on public.asignaciones for select to authenticated
    using (
        usuario_id = (select lince_privado.usuario_actual())
        or acompanante_id = (select lince_privado.usuario_actual())
        or (select lince_privado.es_coordinador())
    );

drop policy if exists servicios_lectura on public.servicios;
create policy servicios_lectura on public.servicios for select to authenticated
    using (
        (select lince_privado.es_coordinador())
        or exists(
            select 1 from public.asignaciones a
            where a.servicio_id = servicios.id
              and (a.usuario_id = (select lince_privado.usuario_actual())
                   or a.acompanante_id = (select lince_privado.usuario_actual()))
        )
    );

drop policy if exists ejecuciones_lectura on public.ejecuciones;
create policy ejecuciones_lectura on public.ejecuciones for select to authenticated
    using (
        (select lince_privado.es_coordinador())
        or exists(
            select 1 from public.asignaciones a
            where a.id = ejecuciones.asignacion_id
              and (a.usuario_id = (select lince_privado.usuario_actual())
                   or a.acompanante_id = (select lince_privado.usuario_actual()))
        )
    );

drop policy if exists historial_lectura on public.historial;
create policy historial_lectura on public.historial for select to authenticated
    using (
        usuario_id = (select lince_privado.usuario_actual())
        or (select lince_privado.es_coordinador())
        or exists(
            select 1 from public.asignaciones a
            where a.id = historial.asignacion_id
              and (a.usuario_id = (select lince_privado.usuario_actual())
                   or a.acompanante_id = (select lince_privado.usuario_actual()))
        )
    );

drop policy if exists calificaciones_lectura on public.calificaciones;
create policy calificaciones_lectura on public.calificaciones for select to authenticated
    using (
        (select lince_privado.es_coordinador())
        or exists(
            select 1 from public.asignaciones a
            where a.id = calificaciones.asignacion_id
              and (a.usuario_id = (select lince_privado.usuario_actual())
                   or a.acompanante_id = (select lince_privado.usuario_actual()))
        )
    );

grant select on public.calificaciones to authenticated;

revoke all on function public.calificar_asignacion(uuid, integer),
    public.reasignar_asignacion(uuid, uuid),
    public.registrar_check_in(uuid, text, text),
    public.actualizar_mi_perfil(text, text, text, text, text[])
    from public, anon, authenticated;
grant execute on function public.calificar_asignacion(uuid, integer),
    public.reasignar_asignacion(uuid, uuid),
    public.registrar_check_in(uuid, text, text),
    public.actualizar_mi_perfil(text, text, text, text, text[])
    to authenticated;

commit;
