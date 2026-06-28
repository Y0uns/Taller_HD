package cl.usm.taller1.dto;

import java.time.LocalDateTime;

public record TransicionEstadoDTO(
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime timestamp
) {}
