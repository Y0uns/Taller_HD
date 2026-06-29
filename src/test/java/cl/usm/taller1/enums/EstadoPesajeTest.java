package cl.usm.taller1.enums;

import cl.usm.taller1.exceptions.IllegalWeighingStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("EstadoPesaje - Máquina de estados y matriz de transiciones")
class EstadoPesajeTest {

    // ============== puedeTransicionarA ==============

    @Test
    @DisplayName("INGRESADO solo puede transicionar a PESADO")
    void ingresado_soloPuedeIrAPesado() {
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.PESADO)).isTrue();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isFalse();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isFalse();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isFalse();
        assertThat(EstadoPesaje.INGRESADO.puedeTransicionarA(EstadoPesaje.INGRESADO)).isFalse();
    }

    @Test
    @DisplayName("PESADO puede transicionar a APROBADO o RECHAZADO")
    void pesado_puedeIrAAprobadoORechazado() {
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isTrue();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isTrue();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.PESADO)).isFalse();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isFalse();
        assertThat(EstadoPesaje.PESADO.puedeTransicionarA(EstadoPesaje.INGRESADO)).isFalse();
    }

    @Test
    @DisplayName("APROBADO solo puede transicionar a DESPACHADO")
    void aprobado_soloPuedeIrADespachado() {
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isTrue();
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.PESADO)).isFalse();
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isFalse();
        assertThat(EstadoPesaje.APROBADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isFalse();
    }

    @Test
    @DisplayName("RECHAZADO solo puede transicionar a DESPACHADO")
    void rechazado_soloPuedeIrADespachado() {
        assertThat(EstadoPesaje.RECHAZADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isTrue();
        assertThat(EstadoPesaje.RECHAZADO.puedeTransicionarA(EstadoPesaje.PESADO)).isFalse();
        assertThat(EstadoPesaje.RECHAZADO.puedeTransicionarA(EstadoPesaje.APROBADO)).isFalse();
        assertThat(EstadoPesaje.RECHAZADO.puedeTransicionarA(EstadoPesaje.RECHAZADO)).isFalse();
    }

    @Test
    @DisplayName("DESPACHADO es estado terminal - no puede transicionar a ningún otro")
    void despachado_terminal() {
        for (EstadoPesaje destino : EstadoPesaje.values()) {
            assertThat(EstadoPesaje.DESPACHADO.puedeTransicionarA(destino))
                    .as("DESPACHADO → " + destino)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("DESPACHADO no puede transicionar a sí mismo")
    void despachado_noASiMismo() {
        assertThat(EstadoPesaje.DESPACHADO.puedeTransicionarA(EstadoPesaje.DESPACHADO)).isFalse();
    }

    // ============== validarTransicion (static) ==============

    @Test
    @DisplayName("validarTransicion - transición válida no lanza excepción")
    void validarTransicion_valida_noLanza() {
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.INGRESADO, EstadoPesaje.PESADO));
    }

    @ParameterizedTest
    @EnumSource(EstadoPesaje.class)
    @DisplayName("validarTransicion - DESPACHADO es terminal a cualquier destino")
    void validarTransicion_despachado_lanza(EstadoPesaje destino) {
        assertThatThrownBy(() -> EstadoPesaje.validarTransicion(EstadoPesaje.DESPACHADO, destino))
                .isInstanceOf(IllegalWeighingStateException.class);
    }

    @Test
    @DisplayName("validarTransicion - saltarse paso (INGRESADO → APROBADO) lanza excepción")
    void validarTransicion_saltarPaso_lanza() {
        assertThatThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.INGRESADO, EstadoPesaje.APROBADO))
                .isInstanceOf(IllegalWeighingStateException.class)
                .hasMessageContaining("INGRESADO")
                .hasMessageContaining("APROBADO");
    }

    @Test
    @DisplayName("validarTransicion - retroceso (APROBADO → PESADO) lanza excepción")
    void validarTransicion_retroceso_lanza() {
        assertThatThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.APROBADO, EstadoPesaje.PESADO))
                .isInstanceOf(IllegalWeighingStateException.class);
    }

    @Test
    @DisplayName("validarTransicion - todas las transiciones de la matriz son válidas")
    void validarTransicion_matrizCompleta_validas() {
        // INGRESADO → PESADO
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.INGRESADO, EstadoPesaje.PESADO));
        // PESADO → APROBADO
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.PESADO, EstadoPesaje.APROBADO));
        // PESADO → RECHAZADO
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.PESADO, EstadoPesaje.RECHAZADO));
        // APROBADO → DESPACHADO
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.APROBADO, EstadoPesaje.DESPACHADO));
        // RECHAZADO → DESPACHADO
        assertThatNoException().isThrownBy(() ->
                EstadoPesaje.validarTransicion(EstadoPesaje.RECHAZADO, EstadoPesaje.DESPACHADO));
    }

    @Test
    @DisplayName("valores del enum - exactamente 5 estados")
    void valores_enum() {
        assertThat(EstadoPesaje.values()).hasSize(5);
        assertThat(EstadoPesaje.values())
                .containsExactly(
                        EstadoPesaje.INGRESADO,
                        EstadoPesaje.PESADO,
                        EstadoPesaje.APROBADO,
                        EstadoPesaje.RECHAZADO,
                        EstadoPesaje.DESPACHADO);
    }

    @Test
    @DisplayName("valueOf - lookup por nombre funciona")
    void valueOf_lookup() {
        assertThat(EstadoPesaje.valueOf("INGRESADO")).isEqualTo(EstadoPesaje.INGRESADO);
        assertThat(EstadoPesaje.valueOf("PESADO")).isEqualTo(EstadoPesaje.PESADO);
        assertThat(EstadoPesaje.valueOf("APROBADO")).isEqualTo(EstadoPesaje.APROBADO);
        assertThat(EstadoPesaje.valueOf("RECHAZADO")).isEqualTo(EstadoPesaje.RECHAZADO);
        assertThat(EstadoPesaje.valueOf("DESPACHADO")).isEqualTo(EstadoPesaje.DESPACHADO);
    }
}