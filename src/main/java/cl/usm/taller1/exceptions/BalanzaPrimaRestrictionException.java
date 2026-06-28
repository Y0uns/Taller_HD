package cl.usm.taller1.exceptions;

public class BalanzaPrimaRestrictionException extends RuntimeException {

    public BalanzaPrimaRestrictionException(int idBalanza, int diaDelMes) {
        super("La balanza con ID primo " + idBalanza + " no puede registrar paquetes PESADO en días impares (día " + diaDelMes + ")");
    }
}
