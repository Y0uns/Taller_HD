# Manual de Usuario (Guía Rápida)

Para iniciar el sistema de manera local necesitas Docker (recomendado) o instancias locales de MongoDB y Redis.

## Opción A — Docker Compose (recomendado)

Levanta MongoDB, Redis y un mock de la API externa de balanzas en una sola línea:

```bash
docker compose up -d
```

Verifica que los servicios están healthy:

```bash
docker compose ps
```

Los tres servicios (`mongodb`, `redis`, `mock-scale-api`) deben estar `running` y `(healthy)`.

## Opción B — Sin Docker

Instala MongoDB 7 y Redis 7 por tu cuenta, asegurate de que estén corriendo en `localhost:27017` y `localhost:6379` respectivamente, y salta a la sección siguiente.

## Iniciar la aplicación

Con el perfil `local` (apunta automáticamente a los servicios del compose):

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

O bien, sin perfil, con la configuración por default (`localhost:27017` y `localhost:6379` sin auth):

```bash
./mvnw spring-boot:run
```

La aplicación quedará escuchando en `http://localhost:8080`.

## Consumir la API

### Swagger UI

La documentación interactiva está disponible en:

```
http://localhost:8080/swagger-ui.html
```

Y el spec OpenAPI en JSON en:

```
http://localhost:8080/api-docs
```

### Crear un Pesaje

**POST** `/api/pesajes`
```json
{
  "idBalanza": "7",
  "idPaquete": "PKG-001",
  "pesoEnSansas": 12.5
}
```

Posibles respuestas:
- `201 Created` — registro creado.
- `400 Bad Request` — body inválido (peso negativo, campos faltantes).
- `409 Conflict` — `BalanzaPrimaRestrictionException` o `RestriccionHorarioNocturnoException`.

### Actualizar el Estado

**PATCH** `/api/pesajes/{id}`
```json
{
  "nuevoEstado": "PESADO"
}
```

Posibles respuestas:
- `200 OK` — transición aplicada.
- `400 Bad Request` — transición no permitida por la máquina de estados.

### Obtener Historial Filtrado

**GET** `/api/pesajes?fechaDesde=2024-01-01T00:00:00&fechaHasta=2024-01-31T23:59:59`

Si no se pasan fechas, devuelve todos los registros.

## Apagar los servicios

```bash
docker compose down
```

Si además quieres borrar los volúmenes persistentes (datos de Mongo y Redis):

```bash
docker compose down -v
```

## Troubleshooting

- **"Connection refused" a Mongo/Redis al arrancar**: los contenedores no terminaron de inicializar. Espera 20-30s y reintenta, o revisa `docker compose logs mongodb`.
- **El endpoint externo no responde**: el mock (`mock-scale-api`) puede caerse. La app seguirá funcionando — caerá al fallback de Redis y luego al spec default `"-1"`.
- **El cache de Redis no refleja los cambios**: el spec por defecto `-1` no tiene TTL (decisión de diseño). Las specs obtenidas de la API sí tienen TTL de 120s.