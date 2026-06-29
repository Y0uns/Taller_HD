package cl.usm.taller1.controllers;

import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;
import cl.usm.taller1.services.PesajeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pesajes")
@RequiredArgsConstructor
@Tag(name = "Pesajes", description = "API para la gestión de ciclo de vida de pesajes")
public class PesajeController {

    private final PesajeService pesajeService;

    @PostMapping
    @Operation(summary = "Crear un nuevo registro de pesaje")
    public ResponseEntity<PesajeResponseDTO> crearRegistro(@Valid @RequestBody PesajeRequestDTO request) {
        PesajeResponseDTO response = pesajeService.crearRegistro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar el estado de un pesaje")
    public ResponseEntity<PesajeResponseDTO> actualizarEstado(
            @PathVariable String id,
            @Valid @RequestBody ActualizarEstadoRequestDTO request) {
        PesajeResponseDTO response = pesajeService.actualizarEstado(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtener registros de pesaje, opcionalmente filtrados por rango de fechas")
    public ResponseEntity<List<PesajeResponseDTO>> obtenerPorFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        List<PesajeResponseDTO> registros = pesajeService.obtenerPorFiltros(fechaDesde, fechaHasta);
        return ResponseEntity.ok(registros);
    }
}
