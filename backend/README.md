# Lince Backend

API propia de escrituras (hitos, geocercas, disponibilidad, incidencias). Southbound se consulta solo desde la app en modo lectura.

## Ejecutar

```bash
cp backend/.env.example backend/.env
set -a; . backend/.env; set +a
./gradlew :backend:run
```

- Swagger UI: http://localhost:8080/docs
- OpenAPI 3.0: http://localhost:8080/openapi.yaml
- Salud: http://localhost:8080/health
- SQLite: `backend/data/lince.db` (ignorado por git)

## Usuarios demo

| Email | Rol |
|---|---|
| guia@lince.local | GUIA |
| transportista@lince.local | TRANSPORTISTA |
| coordinacion@lince.local | COORDINACION |

Clave: `DEMO_PASSWORD` (por defecto `Lince123`).

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"guia@lince.local","password":"Lince123"}'
```

## Variables

`PORT`, `HOST`, `DB_PATH`, `JWT_SECRET`, `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_TTL_MINUTES`, `DEMO_PASSWORD`, `SOUTHBOUND_BASE_URL`, `SOUTHBOUND_API_KEY`.

`SOUTHBOUND_*` queda como configuración de la app Android (la key se inyecta vía `local.properties`/`BuildConfig`); el backend no llama a Southbound.

## Reglas

- Check-in: geocerca (radio parametrizable, 150 m por defecto) + al menos 15 min antes del pickup. Guarda distancia Haversine y precisión.
- Check-out: requiere check-in previo y guarda dropoff.
- Bloqueo: mínimo 7 días de anticipación y sin servicios asignados.
- Sin rastreo continuo: solo hitos discretos; coordinación lee la última posición reportada.
