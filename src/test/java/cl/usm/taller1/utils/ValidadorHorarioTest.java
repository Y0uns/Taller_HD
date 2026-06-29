package cl.usm.taller1.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ValidadorHorario - RestricciÃ³n nocturna y dÃ­as impares")
class ValidadorHorarioTest {

    // ============== esHorarioNocturnoRestringido ==============

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 19:59 NO estÃ¡ restringido")
    void horario_19_59_noRestringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(19, 59))).isFalse();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 20:00 SÃ estÃ¡ restringido (lÃ­mite inferior)")
    void horario_20_00_restringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(20, 0))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {20, 21, 22, 23})
    @DisplayName("esHorarioNocturnoRestringido - horas 20:00-23:59 estÃ¡n restringidas")
    void horario_noche(int hora) {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(hora, 30))).isTrue();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 23:59 estÃ¡ restringido")
    void horario_23_59_restringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(23, 59))).isTrue();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 00:00 SÃ estÃ¡ restringido (cruza medianoche)")
    void horario_00_00_restringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(0, 0))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5})
    @DisplayName("esHorarioNocturnoRestringido - horas 00:00-05:59 estÃ¡n restringidas")
    void horario_madrugada(int hora) {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(hora, 30))).isTrue();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 05:59 estÃ¡ restringido")
    void horario_05_59_restringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(5, 59))).isTrue();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - 06:00 NO estÃ¡ restringido (lÃ­mite superior)")
    void horario_06_00_noRestringido() {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(6, 0))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19})
    @DisplayName("esHorarioNocturnoRestringido - horas diurnas 06:00-19:59 NO restringidas")
    void horario_diurno_noRestringido(int hora) {
        assertThat(ValidadorHorario.esHorarioNocturnoRestringido(LocalTime.of(hora, 0))).isFalse();
    }

    @Test
    @DisplayName("esHorarioNocturnoRestringido - null lanza IllegalArgumentException")
    void horario_null_lanzaExcepcion() {
        assertThatThrownBy(() -> ValidadorHorario.esHorarioNocturnoRestringido(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hora");
    }

    // ============== esDiaImparDelMes ==============

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31})
    @DisplayName("esDiaImparDelMes - dÃ­as impares retorna true")
    void diaImpar_true(int dia) {
        LocalDate fecha = LocalDate.of(2026, 7, dia);
        assertThat(ValidadorHorario.esDiaImparDelMes(fecha)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30})
    @DisplayName("esDiaImparDelMes - dÃ­as pares retorna false")
    void diaImpar_false(int dia) {
        LocalDate fecha = LocalDate.of(2026, 7, dia);
        assertThat(ValidadorHorario.esDiaImparDelMes(fecha)).isFalse();
    }

    @Test
    @DisplayName("esDiaImparDelMes - null lanza IllegalArgumentException")
    void diaImpar_null_lanzaExcepcion() {
        assertThatThrownBy(() -> ValidadorHorario.esDiaImparDelMes(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha");
    }

    @Test
    @DisplayName("Constructor privado - clase utilitaria no instanciable")
    void constructorPrivado() throws Exception {
        var ctor = ValidadorHorario.class.getDeclaredConstructor();
        assertThat(ctor.canAccess(null)).isFalse();
        ctor.setAccessible(true);
        Object instancia = ctor.newInstance();
        assertThat(instancia).isNotNull();
    }
}
