package cl.usm.taller1.exceptions;

public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String serviceName) {
        super("Servicio externo no disponible: " + serviceName);
    }

    public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
        super("Servicio externo no disponible: " + serviceName, cause);
    }
}
