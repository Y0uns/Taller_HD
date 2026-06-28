package cl.usm.taller1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PesajeRequestDTO(
        @NotBlank(message = "El ID de la balanza es obligatorio")
        String idBalanza,

        @NotBlank(message = "El ID del paquete es obligatorio")
        String idPaquete,

        @NotNull(message = "El peso en Sansas es obligatorio")
        @Positive(message = "El peso en Sansas debe ser positivo")
        BigDecimal pesoEnSansas
) {}
