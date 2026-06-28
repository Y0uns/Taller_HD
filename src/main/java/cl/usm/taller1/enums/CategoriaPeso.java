package cl.usm.taller1.enums;

import java.math.BigDecimal;

/**
 * Categorías de peso basadas en el peso expresado en Sansas.
 */
public enum CategoriaPeso {

    LIVIANO,
    MEDIANO,
    PESADO;

    private static final BigDecimal LIMITE_LIVIANO = new BigDecimal("10");
    private static final BigDecimal LIMITE_MEDIANO = new BigDecimal("50");

    /**
     * Clasifica un peso en Sansas en su categoría correspondiente.
     *
     * @param pesoEnSansas peso a clasificar (en Sansas)
     * @return la categoría de peso correspondiente
     * @throws IllegalArgumentException si el peso es null o negativo
     */
    public static CategoriaPeso clasificar(BigDecimal pesoEnSansas) {
        if (pesoEnSansas == null || pesoEnSansas.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El peso no puede ser null ni negativo");
        }

        if (pesoEnSansas.compareTo(LIMITE_LIVIANO) <= 0) {
            return LIVIANO;
        } else if (pesoEnSansas.compareTo(LIMITE_MEDIANO) <= 0) {
            return MEDIANO;
        } else {
            return PESADO;
        }
    }
}
