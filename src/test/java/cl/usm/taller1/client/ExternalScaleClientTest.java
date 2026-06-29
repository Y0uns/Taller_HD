package cl.usm.taller1.client;

import cl.usm.taller1.dto.ScaleSpecificationDTO;
import cl.usm.taller1.exceptions.ExternalServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExternalScaleClient - API externa, retry y fallback en cascada")
class ExternalScaleClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RedisTemplate<String, ScaleSpecificationDTO> redisTemplate;

    @Mock
    private ValueOperations<String, ScaleSpecificationDTO> valueOps;

    @InjectMocks
    private ExternalScaleClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "cacheTtlSeconds", 120L);
    }

    private static final String CACHE_KEY_PREFIX = "scale:";
    private static final String DEFAULT_KEY = CACHE_KEY_PREFIX + ExternalScaleClient.DEFAULT_SCALE_ID;

    private ScaleSpecificationDTO sampleSpec() {
        return new ScaleSpecificationDTO("101", "Balanza Central", "SansaScale-Pro",
                150.0, 0.01, -0.05);
    }

    private ScaleSpecificationDTO defaultSpec() {
        return new ScaleSpecificationDTO(ExternalScaleClient.DEFAULT_SCALE_ID, "Default", "Sansa-Safe",
                9999.0, 1.0, 0.0);
    }

    // ============== getScaleSpecifications - ÃƒÂ©xito ==============

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getScaleSpecifications - ÃƒÂ©xito: retorna spec y guarda en Redis con TTL 120s")
    void get_exito_retornaYGuardaEnCache() {
        ScaleSpecificationDTO spec = sampleSpec();
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), (Object[]) any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(ScaleSpecificationDTO.class)).thenReturn(spec);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        ScaleSpecificationDTO resultado = client.getScaleSpecifications("101");

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo("101");
        verify(valueOps).set(eq(CACHE_KEY_PREFIX + "101"), eq(spec), eq(Duration.ofSeconds(120)));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getScaleSpecifications - respuesta null: no guarda en cache, retorna null")
    void get_respuestaNull_noGuarda() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), (Object[]) any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(ScaleSpecificationDTO.class)).thenReturn(null);

        ScaleSpecificationDTO resultado = client.getScaleSpecifications("101");

        assertThat(resultado).isNull();
        verify(valueOps, never()).set(anyString(), any(), any(Duration.class));
    }

    // ============== getScaleSpecifications - errores 4xx ==============

    @Test
    @DisplayName("getScaleSpecifications - error 4xx: lanza ExternalScaleClientException (NO se reintenta)")
    void get_error4xx_lanzaExcepcionCliente() {
        RestClientResponseException ex404 = new RestClientResponseException(
                "Not Found",
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Not Found",
                org.springframework.http.HttpHeaders.EMPTY,
                new byte[0],
                null);

        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), (Object[]) any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.body(ScaleSpecificationDTO.class)).thenThrow(ex404);

        assertThatThrownBy(() -> client.getScaleSpecifications("999"))
                .isInstanceOf(ExternalScaleClient.ExternalScaleClientException.class)
                .hasMessageContaining("999");
    }

    // ============== recoverScaleSpecifications - flujo de fallback ==============

    @Test
    @DisplayName("recover - hay cache para el scaleId: retorna cache (sin tocar default)")
    void recover_cacheExiste_retornaCache() {
        ScaleSpecificationDTO cached = sampleSpec();
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(CACHE_KEY_PREFIX + "101")).thenReturn(cached);

        ScaleSpecificationDTO resultado = client.recoverScaleSpecifications(
                new ResourceAccessException("timeout"), "101");

        assertThat(resultado).isEqualTo(cached);
        verify(valueOps, never()).get(DEFAULT_KEY);
    }

    @Test
    @DisplayName("recover - NO hay cache pero SÃƒÂ default: retorna default '-1'")
    void recover_sinCache_retornaDefault() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(CACHE_KEY_PREFIX + "101")).thenReturn(null);
        when(valueOps.get(DEFAULT_KEY)).thenReturn(defaultSpec());

        ScaleSpecificationDTO resultado = client.recoverScaleSpecifications(
                new ResourceAccessException("timeout"), "101");

        assertThat(resultado).isEqualTo(defaultSpec());
        verify(valueOps).get(CACHE_KEY_PREFIX + "101");
        verify(valueOps).get(DEFAULT_KEY);
    }

    @Test
    @DisplayName("recover - ni cache ni default: lanza ExternalServiceUnavailableException crÃƒÂ­tica")
    void recover_niCacheNiDefault_lanzaCritica() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(CACHE_KEY_PREFIX + "101")).thenReturn(null);
        when(valueOps.get(DEFAULT_KEY)).thenReturn(null);

        assertThatThrownBy(() -> client.recoverScaleSpecifications(
                new ResourceAccessException("timeout"), "101"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessageContaining("101");
    }

    @Test
    @DisplayName("recover - 4xx (ExternalScaleClientException): tambiÃƒÂ©n cae al fallback")
    void recover_4xx_tambienCaeAlFallback() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(CACHE_KEY_PREFIX + "404")).thenReturn(null);
        when(valueOps.get(DEFAULT_KEY)).thenReturn(defaultSpec());

        ScaleSpecificationDTO resultado = client.recoverScaleSpecifications(
                new ExternalScaleClient.ExternalScaleClientException("404", new RuntimeException()), "404");

        assertThat(resultado).isEqualTo(defaultSpec());
    }

    @Test
    @DisplayName("DEFAULT_SCALE_ID - constante es '-1'")
    void defaultScaleId_constante() {
        assertThat(ExternalScaleClient.DEFAULT_SCALE_ID).isEqualTo("-1");
    }
}

