package cl.usm.taller1.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidadorPrimo - Determinación de números primos")
class ValidadorPrimoTest {

    @Test
    @DisplayName("esPrimo - 1 NO es primo (convención matemática documentada)")
    void esPrimo_unoNoEsPrimo() {
        assertThat(ValidadorPrimo.esPrimo(1)).isFalse();
    }

    @Test
    @DisplayName("esPrimo - 0 NO es primo")
    void esPrimo_ceroNoEsPrimo() {
        assertThat(ValidadorPrimo.esPrimo(0)).isFalse();
    }

    @Test
    @DisplayName("esPrimo - números negativos NO son primos")
    void esPrimo_negativosNoSonPrimos() {
        assertThat(ValidadorPrimo.esPrimo(-1)).isFalse();
        assertThat(ValidadorPrimo.esPrimo(-2)).isFalse();
        assertThat(ValidadorPrimo.esPrimo(-7)).isFalse();
        assertThat(ValidadorPrimo.esPrimo(-101)).isFalse();
    }

    @Test
    @DisplayName("esPrimo - 2 es primo")
    void esPrimo_dosEsPrimo() {
        assertThat(ValidadorPrimo.esPrimo(2)).isTrue();
    }

    @Test
    @DisplayName("esPrimo - 3 es primo")
    void esPrimo_tresEsPrimo() {
        assertThat(ValidadorPrimo.esPrimo(3)).isTrue();
    }

    @Test
    @DisplayName("esPrimo - 4 NO es primo (2*2)")
    void esPrimo_cuatroNoEsPrimo() {
        assertThat(ValidadorPrimo.esPrimo(4)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 97, 101, 103, 211, 997})
    @DisplayName("esPrimo - primos conocidos retorna true")
    void esPrimo_primosConocidos_true(int primo) {
        assertThat(ValidadorPrimo.esPrimo(primo)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30, 100, 1000})
    @DisplayName("esPrimo - compuestos conocidos retorna false")
    void esPrimo_compuestosConocidos_false(int compuesto) {
        assertThat(ValidadorPrimo.esPrimo(compuesto)).isFalse();
    }

    @Test
    @DisplayName("esPrimo - primo grande (997) - eficiencia O(sqrt(n))")
    void esPrimo_primoGrande() {
        assertThat(ValidadorPrimo.esPrimo(997)).isTrue();
    }

    @Test
    @DisplayName("esPrimo - 999999 NO es primo (3*333333)")
    void esPrimo_999999() {
        assertThat(ValidadorPrimo.esPrimo(999_999)).isFalse();
    }

    @Test
    @DisplayName("esPrimo - 1000003 es primo")
    void esPrimo_millonTres() {
        // 1000003 es primo (verificado)
        assertThat(ValidadorPrimo.esPrimo(1_000_003)).isTrue();
    }

    @Test
    @DisplayName("Constructor privado - clase utilitaria no instanciable")
    void constructorPrivado() throws Exception {
        var ctor = ValidadorPrimo.class.getDeclaredConstructor();
        assertThat(ctor.canAccess(null)).isFalse();
        ctor.setAccessible(true);
        Object instancia = ctor.newInstance();
        assertThat(instancia).isNotNull();
    }
}