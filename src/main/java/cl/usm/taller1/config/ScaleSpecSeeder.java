package cl.usm.taller1.config;

import cl.usm.taller1.dto.ScaleSpecificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScaleSpecSeeder implements ApplicationRunner {

    private final RedisTemplate<String, ScaleSpecificationDTO> redisTemplate;

    public static final String DEFAULT_SCALE_ID = "-1";
    private static final String REDIS_KEY_PREFIX = "scale:";

    @Override
    public void run(ApplicationArguments args) {
        String defaultKey = REDIS_KEY_PREFIX + DEFAULT_SCALE_ID;
        
        if (Boolean.FALSE.equals(redisTemplate.hasKey(defaultKey))) {
            log.info("Sembrando la especificación por defecto de la balanza ('-1') en Redis...");
            ScaleSpecificationDTO defaultSpec = new ScaleSpecificationDTO(
                    DEFAULT_SCALE_ID,
                    "Balanza Default Fallback",
                    "Sansa-Safe",
                    9999.0,
                    1.0,
                    0.0
            );
            // No le ponemos TTL para que viva siempre
            redisTemplate.opsForValue().set(defaultKey, defaultSpec);
            log.info("Especificación por defecto sembrada exitosamente.");
        } else {
            log.info("La especificación por defecto ('-1') ya existe en Redis.");
        }
    }
}
