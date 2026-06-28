package cl.usm.taller1.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utilidad para conversión entre Sansas y Kilogramos.
 * 1 Sansa = 1.337 kg.
 */
public final class ConversorSansa {

    public static final BigDecimal FACTOR_CONVERSION = new BigDecimal("1.337");

    private ConversorSansa() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Convierte una cantidad en Sansas a Kilogramos.
     *
     * @param sansas cantidad en Sansas
     * @return equivalente en Kilogramos (escala 6, HALF_UP)
     * @throws IllegalArgumentException si sansas es null
     */
    public static BigDecimal sansasAKilogramos(BigDecimal sansas) {
        if (sansas == null) {
            throw new IllegalArgumentException("El valor en sansas no puede ser null");
        }
        return sansas.multiply(FACTOR_CONVERSION).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Convierte una cantidad en Kilogramos a Sansas.
     *
     * @param kilogramos cantidad en Kilogramos
     * @return equivalente en Sansas (escala 6, HALF_UP)
     * @throws IllegalArgumentException si kilogramos es null
     */
    public static BigDecimal kilogramosASansas(BigDecimal kilogramos) {
        if (kilogramos == null) {
            throw new IllegalArgumentException("El valor en kilogramos no puede ser null");
        }
        return kilogramos.divide(FACTOR_CONVERSION, 6, RoundingMode.HALF_UP);
    }
}
