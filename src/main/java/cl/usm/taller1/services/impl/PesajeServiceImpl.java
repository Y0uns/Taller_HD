package cl.usm.taller1.services.impl;

import cl.usm.taller1.documents.RegistroPesaje;
import cl.usm.taller1.documents.TransicionEstado;
import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;
import cl.usm.taller1.dto.TransicionEstadoDTO;
import cl.usm.taller1.enums.CategoriaPeso;
import cl.usm.taller1.enums.EstadoPesaje;
import cl.usm.taller1.exceptions.BalanzaPrimaRestrictionException;
import cl.usm.taller1.exceptions.RestriccionHorarioNocturnoException;
import cl.usm.taller1.repositories.PesajeRepository;
import cl.usm.taller1.services.PesajeService;
import cl.usm.taller1.utils.ConversorSansa;
import cl.usm.taller1.utils.ValidadorHorario;
import cl.usm.taller1.utils.ValidadorPrimo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesajeServiceImpl implements PesajeService {

    private final PesajeRepository repository;

    @Override
    public PesajeResponseDTO crearRegistro(PesajeRequestDTO request) {
        CategoriaPeso categoria = CategoriaPeso.clasificar(request.pesoEnSansas());
        
        validarReglaBalanzaPrima(request.idBalanza(), categoria);
        validarRestriccionHoraria(categoria);

        BigDecimal pesoEnKg = ConversorSansa.sansasAKilogramos(request.pesoEnSansas());
        LocalDateTime now = LocalDateTime.now();

        RegistroPesaje registro = RegistroPesaje.builder()
                .idBalanza(request.idBalanza())
                .idPaquete(request.idPaquete())
                .pesoEnSansas(request.pesoEnSansas())
                .pesoEnKilogramos(pesoEnKg)
                .categoriaPeso(categoria)
                .estadoActual(EstadoPesaje.INGRESADO)
                .createdAt(now)
                .updatedAt(now)
                .build();

        registro.getHistorialTransiciones().add(
                TransicionEstado.builder()
                        .estadoAnterior(null)
                        .estadoNuevo(EstadoPesaje.INGRESADO)
                        .timestamp(now)
                        .build()
        );

        RegistroPesaje guardado = repository.save(registro);
        return mapToDTO(guardado);
    }

    @Override
    public PesajeResponseDTO actualizarEstado(String id, ActualizarEstadoRequestDTO request) {
        RegistroPesaje registro = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Registro no encontrado con ID: " + id));

        EstadoPesaje nuevoEstado = EstadoPesaje.valueOf(request.nuevoEstado().toUpperCase());
        EstadoPesaje actual = registro.getEstadoActual();

        EstadoPesaje.validarTransicion(actual, nuevoEstado);

        LocalDateTime now = LocalDateTime.now();
        registro.setEstadoActual(nuevoEstado);
        registro.setUpdatedAt(now);
        registro.getHistorialTransiciones().add(
                TransicionEstado.builder()
                        .estadoAnterior(actual)
                        .estadoNuevo(nuevoEstado)
                        .timestamp(now)
                        .build()
        );

        RegistroPesaje actualizado = repository.save(registro);
        return mapToDTO(actualizado);
    }

    @Override
    public List<PesajeResponseDTO> obtenerPorFiltros(LocalDateTime desde, LocalDateTime hasta) {
        List<RegistroPesaje> registros;
        if (desde != null && hasta != null) {
            registros = repository.findByCreatedAtBetween(desde, hasta);
        } else {
            registros = repository.findAll();
        }
        return registros.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private void validarReglaBalanzaPrima(String idBalanza, CategoriaPeso categoria) {
        if (categoria != CategoriaPeso.PESADO) {
            return;
        }
        
        int idInt;
        try {
            idInt = Integer.parseInt(idBalanza);
        } catch (NumberFormatException e) {
            // Si el ID no es parseable a entero, no puede ser un número primo
            return;
        }

        if (ValidadorPrimo.esPrimo(idInt)) {
            LocalDate hoy = LocalDate.now();
            if (ValidadorHorario.esDiaImparDelMes(hoy)) {
                throw new BalanzaPrimaRestrictionException(idInt, hoy.getDayOfMonth());
            }
        }
    }

    private void validarRestriccionHoraria(CategoriaPeso categoria) {
        if (categoria != CategoriaPeso.PESADO) {
            return;
        }
        
        LocalTime ahora = LocalTime.now();
        if (ValidadorHorario.esHorarioNocturnoRestringido(ahora)) {
            throw new RestriccionHorarioNocturnoException(ahora);
        }
    }

    private PesajeResponseDTO mapToDTO(RegistroPesaje entity) {
        return new PesajeResponseDTO(
                entity.getId(),
                entity.getIdBalanza(),
                entity.getIdPaquete(),
                entity.getPesoEnSansas(),
                entity.getPesoEnKilogramos(),
                entity.getCategoriaPeso().name(),
                entity.getEstadoActual().name(),
                entity.getHistorialTransiciones().stream()
                        .map(t -> new TransicionEstadoDTO(
                                t.getEstadoAnterior() != null ? t.getEstadoAnterior().name() : null,
                                t.getEstadoNuevo().name(),
                                t.getTimestamp()
                        ))
                        .collect(Collectors.toList()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
