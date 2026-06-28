package cl.usm.taller1.enums;

import cl.usm.taller1.exceptions.IllegalWeighingStateException;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Estados posibles de un pesaje en el sistema SansaWeigh.
 * Define una máquina de estados con transiciones válidas.
 */
public enum EstadoPesaje {

    INGRESADO,
    PESADO,
    APROBADO,
    RECHAZADO,
    DESPACHADO;

    private static final Map<EstadoPesaje, Set<EstadoPesaje>> TRANSICIONES;

    static {
        Map<EstadoPesaje, Set<EstadoPesaje>> mapa = new EnumMap<>(EstadoPesaje.class);
        mapa.put(INGRESADO, EnumSet.of(PESADO));
        mapa.put(PESADO, EnumSet.of(APROBADO, RECHAZADO));
        mapa.put(APROBADO, EnumSet.of(DESPACHADO));
        mapa.put(RECHAZADO, EnumSet.of(DESPACHADO));
        mapa.put(DESPACHADO, EnumSet.noneOf(EstadoPesaje.class));
        TRANSICIONES = Collections.unmodifiableMap(mapa);
    }

    /**
     * Verifica si desde este estado se puede transicionar al estado destino.
     *
     * @param destino estado destino
     * @return true si la transición es válida
     */
    public boolean puedeTransicionarA(EstadoPesaje destino) {
        return TRANSICIONES.getOrDefault(this, Collections.emptySet()).contains(destino);
    }

    /**
     * Valida que la transición entre dos estados sea permitida.
     *
     * @param actual  estado actual
     * @param destino estado destino
     * @throws IllegalWeighingStateException si la transición no es válida
     */
    public static void validarTransicion(EstadoPesaje actual, EstadoPesaje destino) {
        if (!actual.puedeTransicionarA(destino)) {
            throw new IllegalWeighingStateException(
                    "Transición no permitida: " + actual + " → " + destino);
        }
    }
}
