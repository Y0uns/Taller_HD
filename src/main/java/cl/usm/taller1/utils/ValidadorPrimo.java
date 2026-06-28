package cl.usm.taller1.utils;

/**
 * Utilidad para validación de números primos.
 */
public final class ValidadorPrimo {

    private ValidadorPrimo() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Determina si un número es primo.
     * <p>
     * Nota: el número 1 NO se considera primo por convención matemática.
     * </p>
     *
     * @param numero número a evaluar
     * @return true si el número es primo, false en caso contrario
     */
    public static boolean esPrimo(int numero) {
        if (numero <= 1) {
            return false;
        }
        if (numero <= 3) {
            return true;
        }
        if (numero % 2 == 0) {
            return false;
        }
        for (int i = 3; i <= Math.sqrt(numero); i += 2) {
            if (numero % i == 0) {
                return false;
            }
        }
        return true;
    }
}
