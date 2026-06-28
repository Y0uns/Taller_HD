package cl.usm.taller1.controllers;

import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;
import cl.usm.taller1.services.PesajeService;
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
public class PesajeController {

    private final PesajeService pesajeService;

    @PostMapping
    public ResponseEntity<PesajeResponseDTO> crearRegistro(@Valid @RequestBody PesajeRequestDTO request) {
        PesajeResponseDTO response = pesajeService.crearRegistro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PesajeResponseDTO> actualizarEstado(
            @PathVariable String id,
            @Valid @RequestBody ActualizarEstadoRequestDTO request) {
        PesajeResponseDTO response = pesajeService.actualizarEstado(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PesajeResponseDTO>> obtenerPorFiltros(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        List<PesajeResponseDTO> registros = pesajeService.obtenerPorFiltros(fechaDesde, fechaHasta);
        return ResponseEntity.ok(registros);
    }
}
