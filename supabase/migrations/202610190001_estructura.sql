begin;

create table public.perfiles (
    id uuid primary key default gen_random_uuid(),
    auth_user_id uuid unique references auth.users(id) on delete set null,
    codigo text not null unique check (btrim(codigo) <> ''),
    nombre text not null check (btrim(nombre) <> ''),
    correo text not null check (position('@' in correo) > 1),
    rol text not null check (rol in ('CONDUCTOR', 'GUIA', 'COORDINADOR')),
    activo boolean not null default true,
    creado_en timestamptz not null default now()
);

create table public.servicios (
    id uuid primary key default gen_random_uuid(),
    codigo text not null unique check (btrim(codigo) <> ''),
    titulo text not null check (btrim(titulo) <> ''),
    tipo text not null check (btrim(tipo) <> ''),
    fecha date not null,
    hora time not null,
    zona_horaria text not null default 'America/Santiago'
        check (zona_horaria = 'America/Santiago'),
    recogida text not null check (btrim(recogida) <> ''),
    destino text not null check (btrim(destino) <> ''),
    creado_en timestamptz not null default now()
);

create table public.asignaciones (
    id uuid primary key default gen_random_uuid(),
    servicio_id uuid not null references public.servicios(id) on delete restrict,
    usuario_id uuid not null references public.perfiles(id) on delete restrict,
    estado text not null default 'PENDIENTE'
        check (estado in ('PENDIENTE', 'CONFIRMADO', 'RECHAZADO')),
    motivo_rechazo text,
    respondido_en timestamptz,
    creado_en timestamptz not null default now(),
    unique (servicio_id, usuario_id),
    check (
        (estado = 'PENDIENTE' and respondido_en is null and motivo_rechazo is null)
        or (estado = 'CONFIRMADO' and respondido_en is not null and motivo_rechazo is null)
        or (estado = 'RECHAZADO' and respondido_en is not null
            and motivo_rechazo is not null and char_length(btrim(motivo_rechazo)) between 1 and 300)
    )
);

create table public.disponibilidad (
    usuario_id uuid not null references public.perfiles(id) on delete restrict,
    fecha date not null,
    bloqueado boolean not null,
    actualizado_en timestamptz not null default now(),
    primary key (usuario_id, fecha)
);

create table public.ejecuciones (
    asignacion_id uuid primary key references public.asignaciones(id) on delete restrict,
    inicio_en timestamptz not null,
    termino_en timestamptz,
    observaciones_inicio text not null default '' check (char_length(observaciones_inicio) <= 500),
    observaciones_termino text not null default '' check (char_length(observaciones_termino) <= 500),
    check (termino_en is null or termino_en >= inicio_en)
);

create table public.historial (
    id uuid primary key default gen_random_uuid(),
    asignacion_id uuid references public.asignaciones(id) on delete restrict,
    usuario_id uuid not null references public.perfiles(id) on delete restrict,
    accion text not null check (accion in ('CONFIRMACION', 'RECHAZO', 'CHECK_IN', 'CHECK_OUT', 'DISPONIBILIDAD')),
    detalle text not null,
    fecha_hora timestamptz not null default now(),
    check ((accion = 'DISPONIBILIDAD' and asignacion_id is null)
        or (accion <> 'DISPONIBILIDAD' and asignacion_id is not null))
);

create table public.sincronizaciones (
    id uuid primary key default gen_random_uuid(),
    usuario_id uuid not null references public.perfiles(id) on delete restrict,
    fecha_hora timestamptz not null default now(),
    simulada boolean not null default true check (simulada),
    detalle text not null default 'Simulación académica; sin conexión a un ERP.'
);

create index asignaciones_usuario_idx on public.asignaciones(usuario_id);
create index servicios_fecha_idx on public.servicios(fecha, hora);
create index historial_asignacion_idx on public.historial(asignacion_id, fecha_hora);
create index historial_usuario_idx on public.historial(usuario_id, fecha_hora);
create index sincronizaciones_usuario_idx on public.sincronizaciones(usuario_id);

alter table public.perfiles enable row level security;
alter table public.servicios enable row level security;
alter table public.asignaciones enable row level security;
alter table public.disponibilidad enable row level security;
alter table public.ejecuciones enable row level security;
alter table public.historial enable row level security;
alter table public.sincronizaciones enable row level security;

revoke all on public.perfiles, public.servicios, public.asignaciones,
    public.disponibilidad, public.ejecuciones, public.historial,
    public.sincronizaciones from anon, authenticated;

commit;
