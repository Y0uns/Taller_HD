package cl.usm.taller1.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ConversorSansa - Conversión entre Sansas y Kilogramos")
class ConversorSansaTest {

    private static final BigDecimal FACTOR = new BigDecimal("1.337");

    @Test
    @DisplayName("sansasAKilogramos - caso base 1 Sansa = 1.337 kg")
    void sansasAKilogramos_casoBase() {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal("1"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("1.337000"));
    }

    @Test
    @DisplayName("sansasAKilogramos - 0 Sansas = 0 kg")
    void sansasAKilogramos_cero() {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(BigDecimal.ZERO);

        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("sansasAKilogramos - 100 Sansas = 133.7 kg")
    void sansasAKilogramos_cien() {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal("100"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("133.700000"));
    }

    @Test
    @DisplayName("sansasAKilogramos - 1337 Sansas = 1787.569 kg")
    void sansasAKilogramos_valorDecimal() {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal("1337"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("1787.569000"));
    }

    @Test
    @DisplayName("sansasAKilogramos - redondeo HALF_UP a 6 decimales")
    void sansasAKilogramos_redondeo() {
        // 1/3 Sansas ≈ 0.333667 kg (con factor 1.337 → 0.333... redondeado HALF_UP a 6 decimales)
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal("0.25"));

        // 0.25 * 1.337 = 0.33425 → HALF_UP 6 decimales = 0.334250
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("0.334250"));
        assertThat(resultado.scale()).isEqualTo(6);
    }

    @Test
    @DisplayName("sansasAKilogramos - escala siempre 6 decimales")
    void sansasAKilogramos_escalaSeis() {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal("10"));

        assertThat(resultado.scale()).isEqualTo(6);
    }

    @Test
    @DisplayName("sansasAKilogramos - null lanza IllegalArgumentException")
    void sansasAKilogramos_null_lanzaExcepcion() {
        assertThatThrownBy(() -> ConversorSansa.sansasAKilogramos(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sansas");
    }

    @Test
    @DisplayName("kilogramosASansas - caso base 1.337 kg = 1 Sansa")
    void kilogramosASansas_casoBase() {
        BigDecimal resultado = ConversorSansa.kilogramosASansas(new BigDecimal("1.337"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("1.000000"));
    }

    @Test
    @DisplayName("kilogramosASansas - 0 kg = 0 Sansas")
    void kilogramosASansas_cero() {
        BigDecimal resultado = ConversorSansa.kilogramosASansas(BigDecimal.ZERO);

        assertThat(resultado).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("kilogramosASansas - 133.7 kg = 100 Sansas")
    void kilogramosASansas_cienSansas() {
        BigDecimal resultado = ConversorSansa.kilogramosASansas(new BigDecimal("133.7"));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal("100.000000"));
    }

    @Test
    @DisplayName("kilogramosASansas - escala 6 decimales HALF_UP")
    void kilogramosASansas_escalaSeis() {
        BigDecimal resultado = ConversorSansa.kilogramosASansas(new BigDecimal("2.5"));

        assertThat(resultado.scale()).isEqualTo(6);
    }

    @Test
    @DisplayName("kilogramosASansas - null lanza IllegalArgumentException")
    void kilogramosASansas_null_lanzaExcepcion() {
        assertThatThrownBy(() -> ConversorSansa.kilogramosASansas(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kilogramos");
    }

    @Test
    @DisplayName("FACTOR_CONVERSION - exactamente 1.337 (corrección OCR)")
    void factorConversion_valorExacto() {
        assertThat(ConversorSansa.FACTOR_CONVERSION).isEqualByComparingTo(new BigDecimal("1.337"));
        assertThat(ConversorSansa.FACTOR_CONVERSION.toPlainString()).isEqualTo("1.337");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1.337000",
            "2, 2.674000",
            "10, 13.370000",
            "50, 66.850000",
            "100, 133.700000"
    })
    @DisplayName("sansasAKilogramos - parametrizado")
    void sansasAKilogramos_parametrizado(String sansas, String esperado) {
        BigDecimal resultado = ConversorSansa.sansasAKilogramos(new BigDecimal(sansas));

        assertThat(resultado).isEqualByComparingTo(new BigDecimal(esperado));
    }

    @Test
    @DisplayName("Round-trip - Sansa -> kg -> Sansa mantiene el valor")
    void roundTrip_sansaKgSansa() {
        BigDecimal original = new BigDecimal("42");

        BigDecimal enKg = ConversorSansa.sansasAKilogramos(original);
        BigDecimal deVuelta = ConversorSansa.kilogramosASansas(enKg);

        assertThat(deVuelta).isEqualByComparingTo(original);
    }

    @Test
    @DisplayName("Constructor privado - clase utilitaria no instanciable")
    void constructorPrivado() throws Exception {
        var ctor = ConversorSansa.class.getDeclaredConstructor();
        assertThat(ctor.canAccess(null)).isFalse();
        ctor.setAccessible(true);
        // No debe lanzar nada, simplemente verificamos que existe y es privado
        Object instancia = ctor.newInstance();
        assertThat(instancia).isNotNull();
    }
}