package cl.usm.taller1.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarEstadoRequestDTO(
        @NotBlank(message = "El nuevo estado es obligatorio")
        String nuevoEstado
) {}
