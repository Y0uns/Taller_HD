package cl.usm.taller1.config;

import cl.usm.taller1.dto.ScaleSpecificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScaleSpecSeeder - Siembra idempotente de la spec default '-1'")
class ScaleSpecSeederTest {

    @Mock
    private RedisTemplate<String, ScaleSpecificationDTO> redisTemplate;

    @Mock
    private ValueOperations<String, ScaleSpecificationDTO> valueOps;

    @Mock
    private ApplicationArguments args;

    @InjectMocks
    private ScaleSpecSeeder seeder;

    private static final String DEFAULT_KEY = "scale:" + ScaleSpecSeeder.DEFAULT_SCALE_ID;

    @Test
    @DisplayName("run - cuando la clave '-1' NO existe, siembra con valores default")
    void run_noExiste_sembrar() {
        when(redisTemplate.hasKey(DEFAULT_KEY)).thenReturn(false);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        seeder.run(args);

        verify(redisTemplate).hasKey(DEFAULT_KEY);
        verify(valueOps).set(eq(DEFAULT_KEY), any(ScaleSpecificationDTO.class));
        // SIN TTL (la default debe vivir siempre)
        verify(valueOps, never()).set(anyString(), any(ScaleSpecificationDTO.class), any(Duration.class));
    }

    @Test
    @DisplayName("run - cuando la clave '-1' YA existe, no sobrescribe (idempotente)")
    void run_yaExiste_noSobrescribe() {
        when(redisTemplate.hasKey(DEFAULT_KEY)).thenReturn(true);

        seeder.run(args);

        verify(redisTemplate).hasKey(DEFAULT_KEY);
        verify(redisTemplate, never()).opsForValue();
        verify(valueOps, never()).set(anyString(), any(ScaleSpecificationDTO.class));
    }

    @Test
    @DisplayName("DEFAULT_SCALE_ID - es '-1' (string, no numérico)")
    void defaultScaleId_valor() {
        assertThatStatic(ScaleSpecSeeder.DEFAULT_SCALE_ID).isEqualTo("-1");
    }

    // helper minimal para no importar AssertJ solo para un assert
    private static org.assertj.core.api.AbstractAssert<?, ?> assertThatStatic(String actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}