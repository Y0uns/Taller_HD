package cl.usm.taller1.exceptions;

public class IllegalWeighingStateException extends RuntimeException {

    public IllegalWeighingStateException(String estadoActual, String estadoDestino) {
        super("Transición de estado no permitida: " + estadoActual + " → " + estadoDestino);
    }

    public IllegalWeighingStateException(String mensajeCompleto) {
        super(mensajeCompleto);
    }
}
