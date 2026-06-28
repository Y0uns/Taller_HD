package cl.usm.taller1.utils;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Utilidad para validaciones de horario y fechas.
 */
public final class ValidadorHorario {

    private static final LocalTime INICIO_RESTRICCION = LocalTime.of(20, 0);
    private static final LocalTime FIN_RESTRICCION = LocalTime.of(6, 0);

    private ValidadorHorario() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Determina si la hora corresponde al horario nocturno restringido (20:00 - 06:00).
     *
     * @param hora hora a evaluar
     * @return true si la hora está dentro del horario nocturno restringido
     * @throws IllegalArgumentException si hora es null
     */
    public static boolean esHorarioNocturnoRestringido(LocalTime hora) {
        if (hora == null) {
            throw new IllegalArgumentException("La hora no puede ser null");
        }
        return !hora.isBefore(INICIO_RESTRICCION) || hora.isBefore(FIN_RESTRICCION);
    }

    /**
     * Determina si la fecha corresponde a un día impar del mes.
     *
     * @param fecha fecha a evaluar
     * @return true si el día del mes es impar
     * @throws IllegalArgumentException si fecha es null
     */
    public static boolean esDiaImparDelMes(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }
        return fecha.getDayOfMonth() % 2 != 0;
    }
}
