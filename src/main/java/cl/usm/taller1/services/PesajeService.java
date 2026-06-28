package cl.usm.taller1.services;

import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface PesajeService {

    PesajeResponseDTO crearRegistro(PesajeRequestDTO request);

    PesajeResponseDTO actualizarEstado(String id, ActualizarEstadoRequestDTO request);

    List<PesajeResponseDTO> obtenerPorFiltros(LocalDateTime desde, LocalDateTime hasta);
}
