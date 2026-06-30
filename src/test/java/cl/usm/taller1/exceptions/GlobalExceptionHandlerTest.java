package cl.usm.taller1.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler - Mapeo de Excepciones a HTTP")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Debe manejar Exception genérica retornando 500 INTERNAL_SERVER_ERROR")
    void handleGenericException() {
        Exception ex = new Exception("Error inesperado en BD");
        
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("Error interno del servidor");
    }

    @Test
    @DisplayName("Debe manejar IllegalWeighingStateException retornando 400 BAD_REQUEST")
    void handleIllegalWeighingState() {
        IllegalWeighingStateException ex = new IllegalWeighingStateException("INGRESADO", "DESPACHADO");
        
        ResponseEntity<ErrorResponse> response = handler.handleIllegalWeighingState(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("Debe manejar BalanzaPrimaRestrictionException retornando 409 CONFLICT")
    void handleBalanzaPrimaRestriction() {
        BalanzaPrimaRestrictionException ex = new BalanzaPrimaRestrictionException(7, 15);
        
        ResponseEntity<ErrorResponse> response = handler.handleBalanzaPrimaRestriction(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("Debe manejar RestriccionHorarioNocturnoException retornando 409 CONFLICT")
    void handleRestriccionHorarioNocturno() {
        RestriccionHorarioNocturnoException ex = new RestriccionHorarioNocturnoException(LocalTime.of(23, 0));
        
        ResponseEntity<ErrorResponse> response = handler.handleRestriccionHorarioNocturno(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("Debe manejar ExternalServiceUnavailableException retornando 503 SERVICE_UNAVAILABLE")
    void handleExternalServiceUnavailable() {
        ExternalServiceUnavailableException ex = new ExternalServiceUnavailableException("API_Balanza");
        
        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceUnavailable(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().status()).isEqualTo(503);
        assertThat(response.getBody().message()).isEqualTo(ex.getMessage());
    }

    @Test
    @DisplayName("Debe manejar MethodArgumentNotValidException retornando 400 BAD_REQUEST y concatenando errores")
    void handleMethodArgumentNotValid() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "pesoEnSansas", "no debe ser nulo"),
                new FieldError("dto", "idBalanza", "no debe estar vacío")
        ));
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).contains("pesoEnSansas: no debe ser nulo");
        assertThat(response.getBody().message()).contains("idBalanza: no debe estar vacío");
    }
}
