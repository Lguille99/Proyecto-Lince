begin;

delete from public.calificaciones
where asignacion_id in (
    select a.id
    from public.asignaciones a
    join public.servicios s on s.id = a.servicio_id
    where s.codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005', 'LIN-006')
);

delete from public.ejecuciones
where asignacion_id in (
    select a.id
    from public.asignaciones a
    join public.servicios s on s.id = a.servicio_id
    where s.codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005', 'LIN-006')
);

delete from public.historial
where asignacion_id in (
    select a.id
    from public.asignaciones a
    join public.servicios s on s.id = a.servicio_id
    where s.codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005', 'LIN-006')
)
or usuario_id in (
    select id from public.perfiles
    where codigo in ('DRV-001', 'DRV-002', 'GUI-002', 'COO-003')
);

delete from public.disponibilidad
where usuario_id in (
    select id from public.perfiles
    where codigo in ('DRV-001', 'DRV-002', 'GUI-002', 'COO-003')
);

delete from public.sincronizaciones
where usuario_id in (
    select id from public.perfiles
    where codigo in ('DRV-001', 'DRV-002', 'GUI-002', 'COO-003')
);

delete from public.asignaciones
where servicio_id in (
    select id from public.servicios
    where codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005', 'LIN-006')
);

delete from public.servicios
where codigo in ('LIN-001', 'LIN-002', 'LIN-003', 'LIN-004', 'LIN-005', 'LIN-006');

commit;
