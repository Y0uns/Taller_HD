package cl.usm.taller1.documents;

import cl.usm.taller1.enums.CategoriaPeso;
import cl.usm.taller1.enums.EstadoPesaje;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "registros_pesaje")
public class RegistroPesaje {

    @Id
    private String id;

    private String idBalanza;

    private String idPaquete;

    private BigDecimal pesoEnSansas;

    private BigDecimal pesoEnKilogramos;

    private CategoriaPeso categoriaPeso;

    @Builder.Default
    private EstadoPesaje estadoActual = EstadoPesaje.INGRESADO;

    @Builder.Default
    private List<TransicionEstado> historialTransiciones = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
