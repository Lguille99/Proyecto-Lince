# Tablero de avance — Evaluación 2

Este tablero versionado en GitHub registra el estado técnico. Los responsables sugeridos y las fechas deben confirmarse por el equipo; no constituye evidencia de que una persona ejecutó una tarea que aún está pendiente. Los commits conservan la autoría real de cada cambio.

## Implementado

| Tarea | Evidencia |
|---|---|
| Persistencia Room de estado, perfiles y disponibilidad | `EstadoServicioEntity`, `PerfilEntity`, `DisponibilidadEntity`, DAOs y `LinceDatabase` v3 |
| Migraciones Room y esquema exportado | `MIGRACION_1_2`, `MIGRACION_2_3` y esquemas `1.json`–`3.json` |
| Importación única de datos locales anteriores | `AlmacenEstadoRoom` y `AlmacenDisponibilidadRoom` con respaldo JSON |
| Estado único y reactivo | `LinceUiState` + `StateFlow` observado por las pantallas |
| Confirmar/rechazar con motivo validado | Repositorio, detalle de servicio y persistencia Room |
| Perfil editable con validación inline | Nombre, correo, teléfono, zona e idiomas; ID y rol fijos |
| Agenda con selector nativo | Calendario mensual y `DatePickerDialog` desde el día actual |
| Estado de conexión | `MonitorConexion`, `ConexionViewModel` y banner de red |
| Actualización de listas, historial y resumen | `LinceViewModel`, `LinceUiState` y pantallas migradas |
| Documentación de ejecución y demostración | README actualizado |

## Verificación local

| Comprobación | Resultado |
|---|---|
| `:dominio:test` | 40 pruebas aprobadas |
| `:app:testDebugUnitTest` | 7 pruebas aprobadas: disponibilidad Room, estado/perfiles Room y conectividad |
| `:app:compileDebugKotlin` | Compilación correcta |
| Pruebas PGlite | 43 comprobaciones aprobadas antes de la integración; el SQL no cambió |
| `:app:lintDebug` | Pendiente de ejecutar sobre la versión integrada |
| `:app:assembleDebug` | Pendiente de generar el APK final |

## Por verificar antes de entregar

| Tarea | Responsable sugerido | Criterio de aceptación |
|---|---|---|
| Ensayar agenda y modo sin conexión | Luis | Abrir `DatePicker`, guardar, cerrar y reabrir; comprobar el banner al perder red |
| Ensayar confirmación y rechazo | Jeremy | Motivo obligatorio, estado persistente y respuesta duplicada bloqueada |
| Ensayar perfil | Jeremy | Ver errores inline y comprobar que los datos persisten al reabrir |
| Ensayar coordinación y navegación | Vicente | Conteos, historial y ficha reflejan las respuestas sin errores |
| Revisar la versión integrada | Los tres | Mismo commit, lint correcto, pruebas aprobadas y APK generado |
| Confirmar planificación y fecha AVA | Vicente con el equipo | Responsables, fechas y enlace al tablero acordados |
| Entregar código en AVA | Integrante que acuerde el equipo | Registrar commit entregado y no modificarlo después del cierre |

## Pendientes posteriores

| Tarea | Dependencia |
|---|---|
| Consumir la API del cliente | Documentación, acceso y contrato de autenticación del cliente |
| Sincronización bidireccional y offline avanzado | Resolución de conflictos y política de reintentos |
| Notificaciones de asignaciones y disponibilidad | Servicio de notificaciones y permisos |

## Registro de demostración física

Pendiente de completar por quien realice la prueba:

- Fecha y responsable:
- Emulador o modelo de teléfono y versión Android:
- Commit probado:
- Ingreso, búsqueda y navegación:
- Confirmación/rechazo y permanencia al reabrir:
- Agenda, `DatePicker` y permanencia al reabrir:
- Detección de pérdida de Internet:
- Validación inline del perfil:
- Incidencias y correcciones:
