package cl.usm.taller1.repositories;

import cl.usm.taller1.documents.RegistroPesaje;
import cl.usm.taller1.enums.EstadoPesaje;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PesajeRepository extends MongoRepository<RegistroPesaje, String> {

    boolean existsByIdPaquete(String idPaquete);

    List<RegistroPesaje> findByCreatedAtBetween(LocalDateTime desde, LocalDateTime hasta);

    List<RegistroPesaje> findByEstadoActual(EstadoPesaje estado);

    List<RegistroPesaje> findByIdBalanza(String idBalanza);
}
