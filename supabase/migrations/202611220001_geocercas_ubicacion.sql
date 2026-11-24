begin;

alter table public.servicios
    add column if not exists latitud_recogida double precision,
    add column if not exists longitud_recogida double precision,
    add column if not exists radio_geocerca_metros integer not null default 150;

alter table public.servicios
    drop constraint if exists servicios_geocerca_check;
alter table public.servicios
    add constraint servicios_geocerca_check check (
        (latitud_recogida is null and longitud_recogida is null)
        or (latitud_recogida between -90 and 90 and longitud_recogida between -180 and 180)
    );
alter table public.servicios
    drop constraint if exists servicios_radio_geocerca_check;
alter table public.servicios
    add constraint servicios_radio_geocerca_check
    check (radio_geocerca_metros between 25 and 5000);

alter table public.ejecuciones
    add column if not exists ubicacion_termino text not null default '',
    add column if not exists latitud_inicio double precision,
    add column if not exists longitud_inicio double precision,
    add column if not exists precision_inicio double precision,
    add column if not exists distancia_inicio double precision,
    add column if not exists latitud_termino double precision,
    add column if not exists longitud_termino double precision,
    add column if not exists precision_termino double precision,
    add column if not exists ultima_latitud double precision,
    add column if not exists ultima_longitud double precision,
    add column if not exists ultima_precision double precision,
    add column if not exists ultima_ubicacion_en timestamptz;

alter table public.ejecuciones
    drop constraint if exists ejecuciones_ubicacion_termino_check;
alter table public.ejecuciones
    add constraint ejecuciones_ubicacion_termino_check
    check (char_length(ubicacion_termino) <= 200);
alter table public.ejecuciones
    drop constraint if exists ejecuciones_coordenadas_check;
alter table public.ejecuciones
    add constraint ejecuciones_coordenadas_check check (
        (latitud_inicio is null or latitud_inicio between -90 and 90)
        and (longitud_inicio is null or longitud_inicio between -180 and 180)
        and (latitud_termino is null or latitud_termino between -90 and 90)
        and (longitud_termino is null or longitud_termino between -180 and 180)
        and (ultima_latitud is null or ultima_latitud between -90 and 90)
        and (ultima_longitud is null or ultima_longitud between -180 and 180)
    );

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
            or new.ubicacion_inicio <> old.ubicacion_inicio
            or new.latitud_inicio is distinct from old.latitud_inicio
            or new.longitud_inicio is distinct from old.longitud_inicio
            or new.precision_inicio is distinct from old.precision_inicio
            or new.distancia_inicio is distinct from old.distancia_inicio then
            raise exception 'El check-in no se puede modificar.';
        end if;
        if old.termino_en is not null then raise exception 'El servicio ya finalizó.'; end if;
    end if;
    return new;
end;
$$;

create or replace function public.registrar_check_in(
    p_asignacion uuid,
    p_ubicacion text,
    p_observaciones text,
    p_latitud double precision,
    p_longitud double precision,
    p_precision double precision
) returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_asignacion public.asignaciones;
    v_servicio public.servicios;
    v_resultado public.ejecuciones;
    v_ubicacion text := btrim(coalesce(p_ubicacion, ''));
    v_distancia double precision;
begin
    if v_usuario is null then
        raise exception 'Debes iniciar sesión con un perfil activo.';
    end if;
    if v_ubicacion = '' or char_length(v_ubicacion) > 200 then
        raise exception 'Indica una ubicación válida de hasta 200 caracteres.';
    end if;
    if (p_latitud is null) <> (p_longitud is null) then
        raise exception 'La ubicación GPS debe incluir latitud y longitud.';
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
    select * into v_servicio from public.servicios where id = v_asignacion.servicio_id;
    if p_latitud is not null then
        if v_servicio.latitud_recogida is null or v_servicio.longitud_recogida is null then
            raise exception 'La geocerca del hotel de recogida no está configurada.';
        end if;
        if p_latitud not between -90 and 90 or p_longitud not between -180 and 180 then
            raise exception 'La ubicación GPS recibida no es válida.';
        end if;
        v_distancia := 2 * 6371000 * asin(sqrt(
            power(sin(radians(p_latitud - v_servicio.latitud_recogida) / 2), 2)
            + cos(radians(v_servicio.latitud_recogida)) * cos(radians(p_latitud))
            * power(sin(radians(p_longitud - v_servicio.longitud_recogida) / 2), 2)
        ));
        if v_distancia > v_servicio.radio_geocerca_metros then
            raise exception 'Check-in fuera de la geocerca: estás a % m y el radio permitido es de % m.',
                round(v_distancia)::int, v_servicio.radio_geocerca_metros;
        end if;
    end if;
    insert into public.ejecuciones(
        asignacion_id, inicio_en, ubicacion_inicio, observaciones_inicio,
        latitud_inicio, longitud_inicio, precision_inicio, distancia_inicio,
        ultima_latitud, ultima_longitud, ultima_precision, ultima_ubicacion_en
    ) values (
        p_asignacion, clock_timestamp(), v_ubicacion, coalesce(p_observaciones, ''),
        p_latitud, p_longitud, p_precision, v_distancia,
        p_latitud, p_longitud, p_precision,
        case when p_latitud is null then null else clock_timestamp() end
    ) returning * into v_resultado;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CHECK_IN', 'Inicio del servicio registrado en ' || v_ubicacion);
    return v_resultado;
end;
$$;

create or replace function public.registrar_check_out(
    p_asignacion uuid,
    p_observaciones text,
    p_ubicacion_termino text,
    p_latitud double precision,
    p_longitud double precision,
    p_precision double precision
) returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_asignacion public.asignaciones;
    v_resultado public.ejecuciones;
    v_ubicacion text := btrim(coalesce(p_ubicacion_termino, ''));
begin
    if v_usuario is null then
        raise exception 'Debes iniciar sesión con un perfil activo.';
    end if;
    if (p_latitud is null) <> (p_longitud is null) then
        raise exception 'La ubicación GPS debe incluir latitud y longitud.';
    end if;
    if p_latitud is not null
        and (p_latitud not between -90 and 90 or p_longitud not between -180 and 180) then
        raise exception 'La ubicación GPS recibida no es válida.';
    end if;
    if char_length(v_ubicacion) > 200 then
        raise exception 'La ubicación de dropoff admite hasta 200 caracteres.';
    end if;
    select * into v_asignacion
        from public.asignaciones
        where id = p_asignacion
        for update;
    if not found or v_asignacion.usuario_id <> v_usuario then
        raise exception 'Asignación no autorizada.';
    end if;
    update public.ejecuciones
    set termino_en = clock_timestamp(),
        observaciones_termino = coalesce(p_observaciones, ''),
        ubicacion_termino = v_ubicacion,
        latitud_termino = p_latitud,
        longitud_termino = p_longitud,
        precision_termino = p_precision,
        ultima_latitud = coalesce(p_latitud, ultima_latitud),
        ultima_longitud = coalesce(p_longitud, ultima_longitud),
        ultima_precision = coalesce(p_precision, ultima_precision),
        ultima_ubicacion_en = case when p_latitud is null then ultima_ubicacion_en
            else clock_timestamp() end
    where asignacion_id = p_asignacion and termino_en is null
    returning * into v_resultado;
    if not found then
        raise exception 'Primero registra check-in; solo se permite un check-out.';
    end if;
    insert into public.historial(asignacion_id, usuario_id, accion, detalle)
        values(p_asignacion, v_usuario, 'CHECK_OUT',
            'Término del servicio registrado en ' || coalesce(nullif(v_ubicacion, ''), 'destino'));
    return v_resultado;
end;
$$;

create or replace function public.reportar_ubicacion(
    p_asignacion uuid,
    p_latitud double precision,
    p_longitud double precision,
    p_precision double precision
) returns public.ejecuciones language plpgsql security definer set search_path = '' as $$
declare
    v_usuario uuid := lince_privado.usuario_actual();
    v_asignacion public.asignaciones;
    v_resultado public.ejecuciones;
begin
    if v_usuario is null then
        raise exception 'Debes iniciar sesión con un perfil activo.';
    end if;
    if p_latitud is null or p_longitud is null
        or p_latitud not between -90 and 90 or p_longitud not between -180 and 180 then
        raise exception 'La ubicación GPS recibida no es válida.';
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
    update public.ejecuciones
    set ultima_latitud = p_latitud,
        ultima_longitud = p_longitud,
        ultima_precision = p_precision,
        ultima_ubicacion_en = clock_timestamp()
    where asignacion_id = p_asignacion and termino_en is null
    returning * into v_resultado;
    if not found then
        raise exception 'Solo se reporta la ubicación durante un servicio activo.';
    end if;
    return v_resultado;
end;
$$;

revoke all on function public.registrar_check_in(uuid, text, text, double precision, double precision, double precision),
    public.registrar_check_out(uuid, text, text, double precision, double precision, double precision),
    public.reportar_ubicacion(uuid, double precision, double precision, double precision)
    from public, anon, authenticated;
grant execute on function public.registrar_check_in(uuid, text, text, double precision, double precision, double precision),
    public.registrar_check_out(uuid, text, text, double precision, double precision, double precision),
    public.reportar_ubicacion(uuid, double precision, double precision, double precision)
    to authenticated;

commit;
