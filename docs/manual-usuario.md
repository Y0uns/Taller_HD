# Manual de Usuario (Guía Rápida)

Para iniciar el sistema de manera local, asegúrate de tener instancias de **MongoDB** y **Redis** ejecutándose.

## Iniciar el servidor

```bash
./mvnw spring-boot:run
```

## Consumir la API

### Crear un Pesaje

**POST** `/api/pesajes`
```json
{
  "idBalanza": "7",
  "idPaquete": "PKG-001",
  "pesoEnSansas": 12.5
}
```

### Actualizar el Estado

**PATCH** `/api/pesajes/{id}`
```json
{
  "nuevoEstado": "PESADO"
}
```

### Obtener Historial Filtrado

**GET** `/api/pesajes?fechaDesde=2024-01-01T00:00:00&fechaHasta=2024-01-31T23:59:59`
