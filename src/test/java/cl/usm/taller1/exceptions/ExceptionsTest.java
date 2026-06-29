package cl.usm.taller1.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Excepciones custom - mensajes y constructores")
class ExceptionsTest {

    // ============== IllegalWeighingStateException ==============

    @Test
    @DisplayName("IllegalWeighingStateException - mensaje incluye ambos estados")
    void illegalWeighingState_mensaje() {
        IllegalWeighingStateException ex =
                new IllegalWeighingStateException("INGRESADO", "DESPACHADO");

        assertThat(ex.getMessage())
                .contains("INGRESADO")
                .contains("DESPACHADO")
                .contains("no permitida");
    }

    @Test
    @DisplayName("IllegalWeighingStateException - es RuntimeException")
    void illegalWeighingState_tipo() {
        IllegalWeighingStateException ex =
                new IllegalWeighingStateException("PESADO", "INGRESADO");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("IllegalWeighingStateException - constructor alternativo con mensaje completo")
    void illegalWeighingState_mensajeCompleto() {
        IllegalWeighingStateException ex =
                new IllegalWeighingStateException("Transición no permitida: X → Y");

        assertThat(ex.getMessage()).isEqualTo("Transición no permitida: X → Y");
    }

    // ============== BalanzaPrimaRestrictionException ==============

    @Test
    @DisplayName("BalanzaPrimaRestrictionException - mensaje incluye id y día")
    void balanzaPrima_mensaje() {
        BalanzaPrimaRestrictionException ex = new BalanzaPrimaRestrictionException(7, 15);

        assertThat(ex.getMessage())
                .contains("7")
                .contains("15")
                .contains("primo");
    }

    @Test
    @DisplayName("BalanzaPrimaRestrictionException - es RuntimeException")
    void balanzaPrima_tipo() {
        BalanzaPrimaRestrictionException ex = new BalanzaPrimaRestrictionException(2, 1);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ============== RestriccionHorarioNocturnoException ==============

    @Test
    @DisplayName("RestriccionHorarioNocturnoException - mensaje incluye la hora")
    void horarioNocturno_mensaje() {
        RestriccionHorarioNocturnoException ex =
                new RestriccionHorarioNocturnoException(LocalTime.of(23, 30));

        assertThat(ex.getMessage())
                .contains("23:30")
                .contains("nocturno")
                .contains("PESADO");
    }

    @Test
    @DisplayName("RestriccionHorarioNocturnoException - es RuntimeException")
    void horarioNocturno_tipo() {
        RestriccionHorarioNocturnoException ex =
                new RestriccionHorarioNocturnoException(LocalTime.of(0, 0));

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ============== ExternalServiceUnavailableException ==============

    @Test
    @DisplayName("ExternalServiceUnavailableException - mensaje incluye el servicio")
    void externalService_mensaje() {
        ExternalServiceUnavailableException ex =
                new ExternalServiceUnavailableException("scale-api");

        assertThat(ex.getMessage()).contains("scale-api");
    }

    @Test
    @DisplayName("ExternalServiceUnavailableException - con causa encadena mensaje")
    void externalService_conCausa() {
        Throwable causa = new RuntimeException("timeout");
        ExternalServiceUnavailableException ex =
                new ExternalServiceUnavailableException("scale-api", causa);

        assertThat(ex.getMessage()).contains("scale-api");
        assertThat(ex.getCause()).isEqualTo(causa);
    }

    @Test
    @DisplayName("ExternalServiceUnavailableException - es RuntimeException")
    void externalService_tipo() {
        ExternalServiceUnavailableException ex =
                new ExternalServiceUnavailableException("x");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    // ============== ErrorResponse ==============

    @Test
    @DisplayName("ErrorResponse - record expone los 4 campos correctamente")
    void errorResponse_campos() {
        LocalDateTime ahora = LocalDateTime.now();
        ErrorResponse er = new ErrorResponse(400, "Bad Request", "campo X inválido", ahora);

        assertThat(er.status()).isEqualTo(400);
        assertThat(er.error()).isEqualTo("Bad Request");
        assertThat(er.message()).isEqualTo("campo X inválido");
        assertThat(er.timestamp()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("ErrorResponse - record tiene igualdad por valor")
    void errorResponse_igualdad() {
        LocalDateTime ahora = LocalDateTime.of(2026, 6, 29, 10, 0);
        ErrorResponse a = new ErrorResponse(500, "Internal Server Error", "boom", ahora);
        ErrorResponse b = new ErrorResponse(500, "Internal Server Error", "boom", ahora);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("ErrorResponse - record diferente no es igual")
    void errorResponse_desigualdad() {
        LocalDateTime ahora = LocalDateTime.now();
        ErrorResponse a = new ErrorResponse(400, "Bad Request", "x", ahora);
        ErrorResponse b = new ErrorResponse(500, "Internal Server Error", "x", ahora);

        assertThat(a).isNotEqualTo(b);
    }
}