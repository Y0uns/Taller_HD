package cl.usm.taller1.documents;

import cl.usm.taller1.enums.EstadoPesaje;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransicionEstado {

    private EstadoPesaje estadoAnterior;

    private EstadoPesaje estadoNuevo;

    private LocalDateTime timestamp;
}
