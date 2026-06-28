package cl.usm.taller1.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PesajeResponseDTO(
        String id,
        String idBalanza,
        String idPaquete,
        BigDecimal pesoEnSansas,
        BigDecimal pesoEnKilogramos,
        String categoriaPeso,
        String estadoActual,
        List<TransicionEstadoDTO> historialTransiciones,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
