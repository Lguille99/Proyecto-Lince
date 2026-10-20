begin;

insert into public.perfiles(codigo, nombre, correo, rol) values
    ('DRV-001', 'Conductor Demo', 'conductor@example.com', 'CONDUCTOR'),
    ('GUI-002', 'Guía Demo', 'guia@example.com', 'GUIA'),
    ('COO-003', 'Coordinador Demo', 'coordinador@example.com', 'COORDINADOR')
on conflict(codigo) do nothing;

insert into public.servicios(codigo, titulo, tipo, fecha, hora, recogida, destino) values
    ('LIN-001', 'Transfer de mañana', 'Transfer', (now() at time zone 'America/Santiago')::date + 1, '08:30', 'Punto A de prueba', 'Terminal de prueba'),
    ('LIN-002', 'Excursión al mirador', 'Excursión', (now() at time zone 'America/Santiago')::date + 1, '14:00', 'Punto B de prueba', 'Mirador de prueba'),
    ('LIN-003', 'Transfer de regreso', 'Transfer', (now() at time zone 'America/Santiago')::date + 2, '10:00', 'Terminal de prueba', 'Punto A de prueba'),
    ('LIN-004', 'Recorrido guiado', 'Servicio guiado', (now() at time zone 'America/Santiago')::date + 1, '09:00', 'Plaza de prueba', 'Museo de prueba'),
    ('LIN-005', 'Visita cultural', 'Servicio guiado', (now() at time zone 'America/Santiago')::date + 2, '11:00', 'Museo de prueba', 'Centro de prueba')
on conflict(codigo) do nothing;

insert into public.asignaciones(servicio_id, usuario_id)
select s.id, p.id from public.servicios s join public.perfiles p
    on p.codigo = case when s.codigo in ('LIN-001', 'LIN-002', 'LIN-003') then 'DRV-001' else 'GUI-002' end
where s.codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005')
on conflict(servicio_id, usuario_id) do nothing;

commit;
