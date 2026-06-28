package cl.usm.taller1.client;

import cl.usm.taller1.dto.ScaleSpecificationDTO;
import cl.usm.taller1.exceptions.ExternalServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalScaleClient {

    private final RestClient restClient;
    private final RedisTemplate<String, ScaleSpecificationDTO> redisTemplate;

    @Value("${external.scale.api.cache.ttl-seconds:120}")
    private long cacheTtlSeconds;

    private static final String REDIS_KEY_PREFIX = "scale:";
    public static final String DEFAULT_SCALE_ID = "-1";

    /**
     * Obtiene la especificación de una balanza llamando a la API externa.
     * Reintenta 3 veces ante errores transitorios (ResourceAccessException y 5xx).
     */
    @Retryable(
            retryFor = {ResourceAccessException.class, RestClientResponseException.class},
            maxAttemptsExpression = "${external.scale.api.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${external.scale.api.retry.initial-delay-ms:500}", multiplier = 2)
    )
    public ScaleSpecificationDTO getScaleSpecifications(String scaleId) {
        log.info("Consultando API externa para la balanza: {}", scaleId);

        try {
            ScaleSpecificationDTO response = restClient.get()
                    .uri("/{id}", scaleId)
                    .retrieve()
                    .body(ScaleSpecificationDTO.class);

            if (response != null) {
                // Guardar en caché con TTL
                String cacheKey = REDIS_KEY_PREFIX + scaleId;
                redisTemplate.opsForValue().set(cacheKey, response, Duration.ofSeconds(cacheTtlSeconds));
            }
            return response;

        } catch (RestClientResponseException ex) {
            // Si es un error 4xx (excepto tal vez 429), no deberíamos reintentar, 
            // pero el requerimiento dice "si responde error HTTP / inaccesible -> cache"
            // Dejaremos que Retryable lo intente. Si queremos aislar 4xx, lo podríamos atrapar aquí y no re-lanzar,
            // pero @Retryable lo maneja y luego cae al @Recover.
            log.warn("Error HTTP de la API externa (status {}): {}", ex.getStatusCode(), ex.getMessage());
            // Si es 4xx (Client Error), podríamos evitar el retry lanzando otra exception no incluida en retryFor,
            // pero vamos a re-lanzarlo para que se evalúe el retry y finalmente el @Recover.
            if (ex.getStatusCode().is4xxClientError()) {
                 // Throwing an exception not in retryFor stops the retries.
                 throw new ExternalScaleClientException(scaleId, ex);
            }
            throw ex; 
        }
    }

    /**
     * Método de fallback (cascada) que se ejecuta cuando se agotan los reintentos
     * o ante errores que no se reintentan (ej. 4xx).
     */
    @Recover
    public ScaleSpecificationDTO recoverScaleSpecifications(Exception ex, String scaleId) {
        log.warn("Se agotaron los reintentos o falló la API para scaleId {}. Buscando en caché...", scaleId);
        
        String cacheKey = REDIS_KEY_PREFIX + scaleId;
        ScaleSpecificationDTO cachedSpec = redisTemplate.opsForValue().get(cacheKey);

        if (cachedSpec != null) {
            log.info("Especificación obtenida desde la caché de Redis para scaleId {}", scaleId);
            return cachedSpec;
        }

        log.warn("No existe en caché la balanza {}. Cargando especificación por defecto ('-1')...", scaleId);
        String defaultKey = REDIS_KEY_PREFIX + DEFAULT_SCALE_ID;
        ScaleSpecificationDTO defaultSpec = redisTemplate.opsForValue().get(defaultKey);

        if (defaultSpec != null) {
            log.info("Retornando especificación por defecto ('-1')");
            return defaultSpec;
        }

        // Si tampoco existe la default (lo cual no debería pasar por el Seeder), lanzamos error crítico
        log.error("¡CRÍTICO! No se encontró la especificación por defecto en Redis.");
        throw new ExternalServiceUnavailableException("External API down y fallback cache miss para: " + scaleId, ex);
    }
    
    // Custom exception to avoid retrying 4xx errors
    public static class ExternalScaleClientException extends RuntimeException {
        public ExternalScaleClientException(String scaleId, Throwable cause) {
            super("Client error for scale: " + scaleId, cause);
        }
    }
}
