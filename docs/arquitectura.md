# Arquitectura

El microservicio **SansaWeigh** implementa una arquitectura limpia (Clean Architecture) dividida en capas para garantizar la escalabilidad, mantenibilidad y facilidad de prueba.

## Capas del Proyecto

1. **`controllers/`**: Expone los endpoints REST y maneja la entrada/salida HTTP. Se apoya en `@RestControllerAdvice` para el manejo centralizado de errores.
2. **`services/`**: Contiene la lógica de negocio pura (validación de restricciones, cálculos, etc.). Es agnóstica de los controladores.
3. **`client/`**: Encargada de comunicarse con la API Externa (Especificación de balanzas). Utiliza `RestClient`, `@Retryable` para la tolerancia a fallos de red, y `@Recover` para el fallback a la caché Redis.
4. **`repositories/` & `documents/`**: Capa de acceso a datos utilizando Spring Data MongoDB.
5. **`utils/` & `enums/`**: Clases auxiliares sin estado, como conversores de medida (Sansas a Kg) y validadores matemáticos (números primos).

## Flujo de Fallback (Resiliencia)

Cuando el servicio intenta comunicarse con la API de Balanzas Externas, sigue este flujo estricto:
1. Petición HTTP (`RestClient`).
2. En caso de error transitorio de red (ej. timeout), **reintenta hasta 3 veces** con *backoff exponencial*.
3. Si fallan los reintentos, busca la última especificación guardada en **Redis** (Caché).
4. Si no está en caché, usa la especificación **default (`-1`)**.
