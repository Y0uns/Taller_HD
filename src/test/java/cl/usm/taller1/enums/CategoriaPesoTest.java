package cl.usm.taller1.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CategoriaPeso - Clasificación por peso en Sansas")
class CategoriaPesoTest {

    @Test
    @DisplayName("clasificar - peso 0 es LIVIANO (límite inferior)")
    void clasificar_cero_liviano() {
        assertThat(CategoriaPeso.clasificar(BigDecimal.ZERO)).isEqualTo(CategoriaPeso.LIVIANO);
    }

    @Test
    @DisplayName("clasificar - peso 5 es LIVIANO")
    void clasificar_cinco_liviano() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("5"))).isEqualTo(CategoriaPeso.LIVIANO);
    }

    @Test
    @DisplayName("clasificar - peso 10.00 es LIVIANO (límite superior cerrado)")
    void clasificar_diezExacto_liviano() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("10.00"))).isEqualTo(CategoriaPeso.LIVIANO);
    }

    @Test
    @DisplayName("clasificar - peso 10.01 es MEDIANO (justo sobre el límite)")
    void clasificar_diezUno_mediano() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("10.01"))).isEqualTo(CategoriaPeso.MEDIANO);
    }

    @Test
    @DisplayName("clasificar - peso 25 es MEDIANO")
    void clasificar_veinticinco_mediano() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("25"))).isEqualTo(CategoriaPeso.MEDIANO);
    }

    @Test
    @DisplayName("clasificar - peso 50.00 es MEDIANO (límite superior cerrado)")
    void clasificar_cincuentaExacto_mediano() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("50.00"))).isEqualTo(CategoriaPeso.MEDIANO);
    }

    @Test
    @DisplayName("clasificar - peso 50.01 es PESADO (justo sobre el límite)")
    void clasificar_cincuentaUno_pesado() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("50.01"))).isEqualTo(CategoriaPeso.PESADO);
    }

    @Test
    @DisplayName("clasificar - peso 100 es PESADO")
    void clasificar_cien_pesado() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("100"))).isEqualTo(CategoriaPeso.PESADO);
    }

    @Test
    @DisplayName("clasificar - peso 10000 es PESADO (valor muy grande)")
    void clasificar_muyGrande_pesado() {
        assertThat(CategoriaPeso.clasificar(new BigDecimal("10000"))).isEqualTo(CategoriaPeso.PESADO);
    }

    @Test
    @DisplayName("clasificar - null lanza IllegalArgumentException")
    void clasificar_null_lanzaExcepcion() {
        assertThatThrownBy(() -> CategoriaPeso.clasificar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("peso");
    }

    @Test
    @DisplayName("clasificar - peso negativo lanza IllegalArgumentException")
    void clasificar_negativo_lanzaExcepcion() {
        assertThatThrownBy(() -> CategoriaPeso.clasificar(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("peso");
    }

    @Test
    @DisplayName("clasificar - peso negativo grande lanza IllegalArgumentException")
    void clasificar_negativoGrande_lanzaExcepcion() {
        assertThatThrownBy(() -> CategoriaPeso.clasificar(new BigDecimal("-9999.99")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, LIVIANO",
            "1, LIVIANO",
            "9.99, LIVIANO",
            "10, LIVIANO",
            "10.01, MEDIANO",
            "30, MEDIANO",
            "49.99, MEDIANO",
            "50, MEDIANO",
            "50.01, PESADO",
            "75, PESADO",
            "100, PESADO"
    })
    @DisplayName("clasificar - casos borde parametrizados")
    void clasificar_parametrizado(String peso, String categoriaEsperada) {
        assertThat(CategoriaPeso.clasificar(new BigDecimal(peso)))
                .isEqualTo(CategoriaPeso.valueOf(categoriaEsperada));
    }

    @Test
    @DisplayName("valores del enum - exactamente 3 categorías")
    void valores_enum() {
        assertThat(CategoriaPeso.values()).hasSize(3);
        assertThat(CategoriaPeso.values())
                .containsExactly(CategoriaPeso.LIVIANO, CategoriaPeso.MEDIANO, CategoriaPeso.PESADO);
    }
}