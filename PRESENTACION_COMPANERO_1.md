# PRESENTACIÓN PROYECTO-LINCE — COMPAÑERO 1

## Tiempo asignado: 5–6 minutos (aprox. min 4:30 a 10:30 de la presentación)

---

## TEMAS QUE CUBRES
1. ViewModel y StateFlow
2. Persistencia local con Room v3
3. Integración con Supabase
4. Monitor de conexión en tiempo real

---

## GUIÓN DETALLADO (minuto a minuto)

### Minuto 4:30–5:30 | ViewModel y StateFlow

**Qué decir:**
> "Pasando a la capa de presentación, nuestros ViewModels utilizan `StateFlow` para manejar el estado de la UI. Esto nos brinda estado inmutable y predecible, recolección segura con el ciclo de vida de la pantalla, y separación clara entre lógica de negocio y presentación.
>
> Por ejemplo, `LinceViewModel` expone `uiState` que contiene usuario, error, carga, y lista de servicios. Cada pantalla se suscribe a ese flujo con `collectAsStateWithLifecycle()`, así la UI siempre reacciona a cambios de estado de forma reactiva y segura."

**Qué mostrar en pantalla:**
- Abrir `app/src/main/java/cl/apiavengers/lince/viewmodel/LinceViewModel.kt`
- Señalar la declaración: `private val _uiState = MutableStateFlow(LinceUiState())` y `val uiState: StateFlow<LinceUiState> = _uiState.asStateFlow()`
- Abrir una pantalla (ej. `ServiciosScreen.kt`) y señalar: `val estado by vm.uiState.collectAsStateWithLifecycle()`

**Puntos clave para recordar:**
- `StateFlow` = flujo caliente que emite el último valor a nuevos suscriptores
- `collectAsStateWithLifecycle()` = recolección consciente del ciclo de vida de Compose
- `uiState` es fuente única de verdad por pantalla

---

### Minuto 5:30–6:30 | Persistencia local con Room v3

**Qué decir:**
> "Para la capa de datos usamos Room como base de datos local. Implementamos migraciones controladas de versión 1→2 y 2→3, y exportamos esquemas JSON para cada versión. Esto permite actualizar el modelo de datos sin perder información del usuario.
>
> Las tablas clave son: Usuario, Servicio, Disponibilidad, y EstadoRoom para persistencia del estado de ejecución. Cada entidad tiene su DAO correspondiente para consultas y escrituras."

**Qué mostrar en pantalla:**
- Abrir `app/src/main/java/cl/apiavengers/lince/datos/local/LinceDatabase.kt`
- Señalar: `@Database(entities = [...], version = 3)` y las migraciones `Migration(1,2)` y `Migration(2,3)`
- Abrir `app/schemas/cl.apiavengers.lince.datos.local.LinceDatabase/3.json` (esquema exportado)
- Abrir un DAO (ej. `DisponibilidadDao.kt`) y mostrar `@Query` y `@Insert`

**Puntos clave para recordar:**
- Room versión 3 con migraciones versionadas
- Esquemas JSON exportados por Room para auditoría
- DAOs como capa de acceso a datos tipado

---

### Minuto 6:30–7:30 | Integración con Supabase

**Qué decir:**
> "Supabase actúa como backend remoto. Integramos autenticación con cliente Supabase, manejo de token y perfil de usuario, y un sistema de sesiones. Cuando hay conexión, los datos se pueden sincronizar entre la base de datos local y el servidor remoto."

**Qué mostrar en pantalla:**
- Abrir `app/src/main/java/cl/apiavengers/lince/datos/ClienteSupabase.kt`
- Señalar `object SesionSupabase` con `var cliente` y `val activa`
- Abrir `app/src/main/java/cl/apiavengers/lince/datos/AutenticacionSupabase.kt` (si existe) o buscar dónde se usa Supabase en el ViewModel/Repositorio
- Mostrar `supabase/migrations/202610280001_mvp_supabase.sql` (estructura de tablas remotas)

**Puntos clave para recordar:**
- `SesionSupabase.activa` indica si hay sesión iniciada
- Cliente Supabase con token y `perfilUuid`
- Migraciones SQL de Supabase para tablas remotas

---

### Minuto 7:30–8:30 | Monitor de conexión en tiempo real

**Qué decir:**
> "El `ConexionViewModel` expone un flujo booleano `conectado`. El banner de `EstadoConexion` reacciona a este flujo y muestra mensajes contextuales: 'Sin Internet · trabajando en modo local' o 'Con Internet · conectado a Supabase'. Además, consumimos `WindowInsets.statusBars` para evitar doble padding con la barra de estado del sistema."

**Qué mostrar en pantalla:**
- Abrir `app/src/main/java/cl/apiavengers/lince/viewmodel/ConexionViewModel.kt`
- Señalar `val conectado: StateFlow<Boolean>`
- Abrir `app/src/main/java/cl/apiavengers/lince/ui/screens/EstadoConexion.kt`
- Señalar el `when` que cambia el texto según `conectado` y `modoRemoto`
- Abrir `app/src/main/java/cl/apiavengers/lince/navigation/AppNavigation.kt`
- Señalar `Box(Modifier.weight(1f).consumeWindowInsets(WindowInsets.statusBars))`

**Puntos clave para recordar:**
- `ConexionViewModel` expone estado de red como `StateFlow<Boolean>`
- Banner reacciona dinámicamente a conectividad
- `consumeWindowInsets(WindowInsets.statusBars)` evita solapamiento con barra de sistema

---

### Minuto 8:30–9:30 | Transición a UI

**Qué decir:**
> "Con la base de datos y conectividad listas, pasamos a la interfaz. [Nombre del Compañero 3] les mostrará cómo se ve esto en pantalla."

*(Entrega la palabra al Compañero 3)*

---

## CHECKLIST DE QUÉ TENER ABIERTO EN TU COMPUTADORA

Antes de presentar, ten abiertos estos archivos para navegar rápido:

| Archivo | Qué señalar |
|---------|-------------|
| `LinceViewModel.kt` | `uiState` como `StateFlow` |
| `LinceDatabase.kt` | `@Database(version = 3)` y migraciones |
| `DisponibilidadDao.kt` / `ServicioDao.kt` | Queries de Room |
| `ConexionViewModel.kt` | `conectado: StateFlow<Boolean>` |
| `EstadoConexion.kt` | Banner dinámico |
| `AppNavigation.kt` | `consumeWindowInsets(WindowInsets.statusBars)` |
| `ClienteSupabase.kt` | `SesionSupabase` y cliente remoto |
| `app/schemas/.../3.json` | Esquema exportado por Room |

---

## POSIBLES PREGUNTAS Y RESPUESTAS

**P: ¿Por qué usaron StateFlow en lugar de LiveData?**
> R: StateFlow es más ligero, integra mejor con Jetpack Compose, y permite recolección con `collectAsStateWithLifecycle()` que es consciente del ciclo de vida de la pantalla.

**P: ¿Qué pasa si no hay internet?**
> R: La app funciona offline con Room local. El monitor de conexión detecta la falta de internet y muestra el banner correspondiente. Cuando vuelve la conexión, puede sincronizarse con Supabase.

**P: ¿Cómo manejan las migraciones de base de datos?**
> R: Implementamos migraciones Room versión 1→2 y 2→3, con esquemas JSON exportados para cada versión. Esto evita perder datos del usuario al actualizar el modelo.

**P: ¿Supabase es obligatorio o pueden usarla sin él?**
> R: Pueden usarla sin Supabase en modo local. La app está diseñada para funcionar offline primero, y Supabase es una capa opcional de sincronización cuando hay conexión.

---

## TIMING SUGERIDO

| Sección | Tiempo |
|---------|--------|
| ViewModel y StateFlow | 1 min |
| Room v3 y migraciones | 1 min |
| Supabase | 1 min |
| Monitor de conexión | 1 min |
| Transición | 30 seg |
| **Total** | **~5 min** |

---

## TIP FINAL

Habla con calma, no leas el guion palabra por palabra. Usa los archivos abiertos como apoyo visual y explica con tus propias palabras qué hace cada pieza técnica.

¡Éxito en la presentación!
