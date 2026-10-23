# Proyecto Lince

Aplicación Android académica para organizar la consulta y respuesta a asignaciones de servicios turísticos. El avance de la Evaluación 2 utiliza datos ficticios y almacenamiento local; no se conecta al sistema real del cliente.

**Api Avengers · Equipo 5 · Sección 3D**

| Integrante | Rol del equipo |
|---|---|
| Vicente Krausse | Coordinador |
| Luis Fermin | Diseñador |
| Jeremy Ibañez | Desarrollador |

## Ejecutar

1. Abrir la carpeta raíz del repositorio en Android Studio.
2. Usar JDK 17 e instalar Android SDK 35. El teléfono o emulador debe tener Android 8.0/API 26 o superior.
3. Sincronizar Gradle. La primera sincronización requiere Internet para descargar dependencias.
4. Ejecutar el módulo `app` con Run.
5. Entrar con una cuenta de demostración de la tabla siguiente. No hace falta configurar Supabase.

Android Studio configura `local.properties` con la ruta del SDK de cada equipo. Ese archivo no se sube al repositorio. No copiar la ruta del SDK de otro compañero.

| Rol | Cuenta | Contraseña de demostración |
|---|---|---|
| Conductor | `conductor.demo` | `Lince123` |
| Guía | `guia.demo` | `Lince123` |
| Coordinador | `coordinador.demo` | `Lince123` |

Estas cuentas y la contraseña son públicas y ficticias. Son un acceso de demostración, no un sistema de autenticación para producción. Al cerrar la aplicación hay que volver a ingresar; los registros de Room permanecen.

## Funciones del avance

- Pantallas con Jetpack Compose y Material 3, navegación por rol, listas, búsqueda, filtros y detalles.
- Inicio de sesión de demostración con campos obligatorios y mensaje de credenciales incorrectas.
- Confirmación o rechazo de un servicio pendiente por su responsable. El rechazo requiere un motivo de 1 a 300 caracteres; una respuesta guardada no se puede repetir.
- Servicios y respuestas guardados en Room, con usuario responsable, motivo y fecha/hora de respuesta. El historial incluye servicios anteriores y respuestas nuevas; el resumen actualiza sus conteos.
- Agenda por cuenta con selección de fecha y bloqueo de días desde hoy. Room conserva los días al reiniciar. Los antiguos archivos JSON se importan una sola vez por cuenta.
- Perfil con validación de nombre y correo. Identificador y rol no editables. Los cambios del perfil solo duran durante la sesión y la pantalla lo informa.
- Detección del estado de Internet con un aviso visible. Sin conexión se puede continuar el flujo local.

Los servicios de ejemplo se insertan sin sobrescribir los existentes. Sus fechas quedan fijadas al cargar la base por primera vez. El resumen de disponibilidad del equipo todavía utiliza valores de ejemplo; no representa los días de la agenda personal. La acción de actualizar coordinación recarga datos locales y no envía información al cliente.

## Estructura

- `dominio`: modelos, reglas de validación, repositorios e interfaces de almacenamiento Kotlin.
- `app/.../ui/screens`: pantallas Compose.
- `app/.../viewmodel`: estado de interfaz con ViewModel y StateFlow.
- `app/.../datos/local`: entidades, DAO, almacenamiento y `LinceDatabase` de Room.
- `app/.../datos/MonitorConexion.kt`: observación del estado de la red.
- `app/schemas`: versiones exportadas del esquema Room. La migración 1 → 2 agrega servicios y conserva la agenda.
- `supabase`: trabajo previo de base remota. Está conservado, pero no participa en el acceso ni en el flujo de esta entrega.

La UI observa StateFlow. El ViewModel ejecuta operaciones de disco fuera del hilo principal. El Repository valida las reglas y usa el almacenamiento Room mediante una interfaz. Cuando el cliente entregue su API, se implementará el acceso remoto según su contrato y autenticación; no se asumen endpoints ni credenciales.

## Pruebas

Con Java 17 y Android SDK configurados:

```sh
./gradlew :dominio:test :app:testDebugUnitTest :app:assembleDebug
```

Las pruebas JVM de Android usan Robolectric y pueden descargar su entorno en la primera ejecución. Comprueban respuestas y validaciones, aislamiento de cuentas, reapertura de Room, importación desde JSON y migración de la base. Las pruebas de UI verifican el flujo mediante Compose. No sustituyen el ensayo final en el dispositivo que se usará en la evaluación.

## Demostración para la evaluación

1. Entrar con contraseña incorrecta y mostrar el mensaje; luego ingresar como conductor.
2. Abrir Servicios, buscar una asignación pendiente y acceder a su detalle.
3. Intentar rechazar sin motivo; corregirlo y guardar. Mostrar estado, motivo y fecha/hora.
4. Volver al listado y al historial para comprobar la misma respuesta.
5. Abrir Agenda, elegir hoy o una fecha futura, bloquear el día y guardar.
6. Cerrar completamente la app, volver a entrar y comprobar respuesta y disponibilidad.
7. Ingresar como coordinador para consultar el resumen y el historial.
8. Desactivar Wi-Fi/datos o activar modo avión y mostrar el aviso sin Internet; continuar consultando los datos locales. Reactivar Internet y observar el cambio.
9. Abrir Perfil y demostrar el error de nombre vacío o correo inválido.

## Pendientes

- Ensayar el APK en un emulador o teléfono físico antes de la entrega y registrar el resultado.
- Integrar la API y la autenticación que proporcione el cliente.
- Implementar check-in/check-out, persistencia del perfil y disponibilidad real en el resumen de coordinación.
- Confirmar con el equipo responsables y fechas del [tablero de tareas](docs/PLANIFICACION.md).

## Diseño y evidencias

- [Flujo de usuario](docs/diseno/flujo-usuario-uml.png).
- [Interfaces propuestas](docs/diseno/interfaces).
- [Planificación y verificación](docs/PLANIFICACION.md).

Los diseños describen también funciones futuras: no deben confundirse con la lista de funciones implementadas. Cada integrante debe revisar, ejecutar y entender sus aportes para la defensa individual. Después del cierre de entrega, mantener la versión entregada sin cambios salvo indicación del docente.
