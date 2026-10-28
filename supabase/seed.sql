begin;

insert into public.perfiles(
    codigo, nombre, correo, rol, telefono, zona, idiomas, estado_disponibilidad
) values
    ('DRV-001', 'Martín Conductor', 'conductor@gmail.com', 'CONDUCTOR',
        '+56 9 1234 5678', 'Santiago Centro', array['ESPANOL', 'INGLES'], 'DISPONIBLE'),
    ('GUI-002', 'Valentina Guía', 'guia@gmail.com', 'GUIA',
        '+56 9 8765 4321', 'Providencia', array['ESPANOL', 'INGLES'], 'EN_SERVICIO'),
    ('COO-003', 'Camila Coordinadora', 'coordinador@gmail.com', 'COORDINADOR',
        '+56 9 1111 2222', 'Oficina central', array['ESPANOL', 'INGLES'], 'DISPONIBLE'),
    ('DRV-002', 'Diego Ruta', 'diego.ruta@gmail.com', 'CONDUCTOR',
        '+56 9 3333 4444', 'Maipú', array['ESPANOL', 'INGLES'], 'NO_DISPONIBLE')
on conflict(codigo) do update set
    nombre = excluded.nombre,
    correo = excluded.correo,
    rol = excluded.rol,
    telefono = excluded.telefono,
    zona = excluded.zona,
    idiomas = excluded.idiomas,
    estado_disponibilidad = excluded.estado_disponibilidad,
    activo = true;

insert into public.servicios(
    codigo, titulo, tipo, fecha, hora, recogida, destino,
    idioma_requerido, modalidad, almuerzo_incluido, itinerario,
    resena_historica, hotel_recogida, tarifa, pasajeros, dias_tour
) values
    ('LIN-001', 'Transfer de hoy', 'Transfer',
        (now() at time zone 'America/Santiago')::date,
        greatest(time '00:01', (now() at time zone 'America/Santiago')::time - interval '1 hour'),
        'Hotel Plaza', 'Terminal de prueba', 'ESPANOL', 'TRANSFER', false,
        '["Hotel Plaza", "Terminal de prueba"]'::jsonb,
        'Traslado directo desde el hotel al terminal, sin paradas intermedias.',
        'Hotel Plaza', 25000, 2, 1),
    ('LIN-002', 'Excursión al mirador', 'Excursión',
        (now() at time zone 'America/Santiago')::date + 1, '14:00',
        'Hotel Plaza', 'Mirador de prueba', 'INGLES', 'MEDIO_DIA', true,
        '["Hotel Plaza", "Mirador de prueba", "Restaurante de prueba"]'::jsonb,
        'El mirador fue habilitado en 1998 y conserva senderos de la ruta original.',
        'Hotel Plaza', 45000, 4, 1),
    ('LIN-003', 'Transfer de regreso', 'Transfer',
        (now() at time zone 'America/Santiago')::date + 2, '10:00',
        'Terminal de prueba', 'Hotel Plaza', 'ESPANOL', 'TRANSFER', false,
        '["Terminal de prueba", "Hotel Plaza"]'::jsonb,
        'Retorno al hotel con equipaje; se recomienda confirmar el número de maletas.',
        'Hotel Plaza', 25000, 2, 1),
    ('LIN-004', 'Recorrido guiado', 'Servicio guiado',
        (now() at time zone 'America/Santiago')::date - 1, '09:00',
        'Plaza de prueba', 'Museo de prueba', 'ESPANOL', 'MEDIO_DIA', false,
        '["Plaza de prueba", "Museo de prueba"]'::jsonb,
        'La plaza fue el centro cívico de la ciudad durante el siglo XIX.',
        'Plaza de prueba', 40000, 3, 1),
    ('LIN-005', 'Visita cultural', 'Servicio guiado',
        (now() at time zone 'America/Santiago')::date + 2, '11:00',
        'Museo de prueba', 'Centro de prueba', 'INGLES', 'MEDIO_DIA', false,
        '["Museo de prueba", "Centro de prueba"]'::jsonb,
        'El centro histórico conserva fachadas restauradas y murales del puerto.',
        'Museo de prueba', 42000, 5, 1),
    ('LIN-006', 'Transfer aeropuerto', 'Transfer',
        (now() at time zone 'America/Santiago')::date + 1, '07:00',
        'Hotel de prueba', 'Aeropuerto de prueba', 'INGLES', 'TRANSFER', false,
        '["Hotel de prueba", "Aeropuerto de prueba"]'::jsonb,
        'Salida temprana; considerar margen adicional por tráfico de la mañana.',
        'Hotel de prueba', 30000, 2, 1)
on conflict(codigo) do update set
    titulo = excluded.titulo,
    tipo = excluded.tipo,
    fecha = excluded.fecha,
    hora = excluded.hora,
    recogida = excluded.recogida,
    destino = excluded.destino,
    idioma_requerido = excluded.idioma_requerido,
    modalidad = excluded.modalidad,
    almuerzo_incluido = excluded.almuerzo_incluido,
    itinerario = excluded.itinerario,
    resena_historica = excluded.resena_historica,
    hotel_recogida = excluded.hotel_recogida,
    tarifa = excluded.tarifa,
    pasajeros = excluded.pasajeros,
    dias_tour = excluded.dias_tour;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, p.id, null, 'CONFIRMADO', null, now() - interval '2 hours'
from public.servicios s
join public.perfiles p on p.codigo = 'DRV-001'
where s.codigo = 'LIN-001'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, p.id, null, 'RECHAZADO', 'No disponible por viaje', now() - interval '1 day'
from public.servicios s
join public.perfiles p on p.codigo = 'DRV-001'
where s.codigo = 'LIN-002'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, p.id, null, 'PENDIENTE', null, null
from public.servicios s
join public.perfiles p on p.codigo = 'DRV-001'
where s.codigo = 'LIN-003'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, titular.id, acompanante.id, 'CONFIRMADO', null, now() - interval '2 days'
from public.servicios s
join public.perfiles titular on titular.codigo = 'GUI-002'
join public.perfiles acompanante on acompanante.codigo = 'DRV-001'
where s.codigo = 'LIN-004'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, p.id, null, 'PENDIENTE', null, null
from public.servicios s
join public.perfiles p on p.codigo = 'GUI-002'
where s.codigo = 'LIN-005'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.asignaciones(
    servicio_id, usuario_id, acompanante_id, estado, motivo_rechazo, respondido_en
)
select s.id, p.id, null, 'PENDIENTE', null, null
from public.servicios s
join public.perfiles p on p.codigo = 'DRV-001'
where s.codigo = 'LIN-006'
on conflict(servicio_id, usuario_id) do nothing;

insert into public.ejecuciones(
    asignacion_id, inicio_en, termino_en, observaciones_inicio, observaciones_termino, ubicacion_inicio
)
select a.id, now() - interval '1 day 6 hours', now() - interval '1 day 2 hours',
    'Inicio puntual', 'Servicio completado', 'Plaza de prueba'
from public.asignaciones a
join public.servicios s on s.id = a.servicio_id
where s.codigo = 'LIN-004'
on conflict(asignacion_id) do nothing;

insert into public.calificaciones(asignacion_id, puntaje, calificado_por, calificado_en)
select a.id, 5, c.id, now() - interval '1 day'
from public.asignaciones a
join public.servicios s on s.id = a.servicio_id
join public.perfiles c on c.codigo = 'COO-003'
where s.codigo = 'LIN-004'
on conflict(asignacion_id) do nothing;

commit;
