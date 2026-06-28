package cl.usm.taller1.exceptions;

import java.time.LocalTime;

public class RestriccionHorarioNocturnoException extends RuntimeException {

    public RestriccionHorarioNocturnoException(LocalTime hora) {
        super("No se pueden procesar paquetes PESADO en horario nocturno (20:00-06:00). Hora actual: " + hora);
    }
}
