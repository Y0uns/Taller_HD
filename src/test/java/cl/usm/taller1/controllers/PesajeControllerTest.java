package cl.usm.taller1.controllers;

import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;
import cl.usm.taller1.exceptions.BalanzaPrimaRestrictionException;
import cl.usm.taller1.exceptions.ExternalServiceUnavailableException;
import cl.usm.taller1.exceptions.IllegalWeighingStateException;
import cl.usm.taller1.exceptions.RestriccionHorarioNocturnoException;
import cl.usm.taller1.services.PesajeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PesajeController.class)
@DisplayName("PesajeController - Endpoints REST y mapeo de excepciones")
class PesajeControllerTest {

    @Autowired
    private MockMvc mockMvc;


    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private PesajeService pesajeService;

    private PesajeResponseDTO sampleResponse() {
        return new PesajeResponseDTO(
                "reg-1", "100", "P-1",
                new BigDecimal("10"), new BigDecimal("13.37"),
                "LIVIANO", "INGRESADO",
                List.of(),
                LocalDateTime.of(2026, 6, 29, 12, 0),
                LocalDateTime.of(2026, 6, 29, 12, 0)
        );
    }

    // ============== POST /api/pesajes ==============

    @Test
    @DisplayName("POST - caso feliz retorna 201 Created")
    void post_exito_201() throws Exception {
        when(pesajeService.crearRegistro(any())).thenReturn(sampleResponse());

        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("10"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("reg-1"))
                .andExpect(jsonPath("$.estadoActual").value("INGRESADO"))
                .andExpect(jsonPath("$.categoriaPeso").value("LIVIANO"));
    }

    @Test
    @DisplayName("POST - body invÃƒÂ¡lido (peso null) retorna 400")
    void post_validacion_falla_400() throws Exception {
        String body = "{\"idBalanza\":\"100\",\"idPaquete\":\"P-1\"}"; // peso null

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST - body invÃƒÂ¡lido (peso negativo) retorna 400")
    void post_pesoNegativo_400() throws Exception {
        String body = "{\"idBalanza\":\"100\",\"idPaquete\":\"P-1\",\"pesoEnSansas\":-5}";

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST - body invÃƒÂ¡lido (idBalanza blank) retorna 400")
    void post_idBalanzaBlank_400() throws Exception {
        String body = "{\"idBalanza\":\"\",\"idPaquete\":\"P-1\",\"pesoEnSansas\":5}";

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST - BalanzaPrimaRestrictionException Ã¢â€ â€™ 409 Conflict")
    void post_balanzaPrima_409() throws Exception {
        when(pesajeService.crearRegistro(any()))
                .thenThrow(new BalanzaPrimaRestrictionException(7, 15));

        PesajeRequestDTO req = new PesajeRequestDTO("7", "P-1", new BigDecimal("100"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST - RestriccionHorarioNocturnoException Ã¢â€ â€™ 409 Conflict")
    void post_horarioNocturno_409() throws Exception {
        when(pesajeService.crearRegistro(any()))
                .thenThrow(new RestriccionHorarioNocturnoException(LocalDateTime.now().toLocalTime()));

        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST - IllegalArgumentException Ã¢â€ â€™ 400")
    void post_illegalArg_400() throws Exception {
        when(pesajeService.crearRegistro(any()))
                .thenThrow(new IllegalArgumentException("dato invÃƒÂ¡lido"));

        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST - ExcepciÃƒÂ³n genÃƒÂ©rica Ã¢â€ â€™ 500 con mensaje genÃƒÂ©rico (no stacktrace)")
    void post_excepcionGenerica_500() throws Exception {
        when(pesajeService.crearRegistro(any()))
                .thenThrow(new RuntimeException("boom"));

        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    // ============== PATCH /api/pesajes/{id} ==============

    @Test
    @DisplayName("PATCH - transiciÃƒÂ³n exitosa retorna 200")
    void patch_exito_200() throws Exception {
        when(pesajeService.actualizarEstado(anyString(), any())).thenReturn(sampleResponse());

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("PESADO");

        mockMvc.perform(patch("/api/pesajes/reg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reg-1"));
    }

    @Test
    @DisplayName("PATCH - IllegalWeighingStateException Ã¢â€ â€™ 400 Bad Request")
    void patch_transicionInvalida_400() throws Exception {
        when(pesajeService.actualizarEstado(anyString(), any()))
                .thenThrow(new IllegalWeighingStateException("INGRESADO", "APROBADO"));

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("APROBADO");

        mockMvc.perform(patch("/api/pesajes/reg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PATCH - nuevoEstado blank Ã¢â€ â€™ 400")
    void patch_nuevoEstadoBlank_400() throws Exception {
        String body = "{\"nuevoEstado\":\"\"}";

        mockMvc.perform(patch("/api/pesajes/reg-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ============== GET /api/pesajes ==============

    @Test
    @DisplayName("GET - sin filtros retorna 200 y lista")
    void get_sinFiltros_200() throws Exception {
        when(pesajeService.obtenerPorFiltros(null, null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/pesajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("reg-1"));
    }

    @Test
    @DisplayName("GET - con rango de fechas retorna 200")
    void get_conRangoFechas_200() throws Exception {
        when(pesajeService.obtenerPorFiltros(any(), any())).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/pesajes")
                        .param("fechaDesde", "2026-06-01T00:00:00")
                        .param("fechaHasta", "2026-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("reg-1"));
    }

    @Test
    @DisplayName("GET - lista vacÃƒÂ­a retorna 200 con array vacÃƒÂ­o")
    void get_listaVacia_200() throws Exception {
        when(pesajeService.obtenerPorFiltros(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/pesajes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ============== Mapeo de ExternalServiceUnavailableException ==============

    @Test
    @DisplayName("ExcepciÃƒÂ³n ExternalServiceUnavailableException Ã¢â€ â€™ 503")
    void externalService_503() throws Exception {
        when(pesajeService.crearRegistro(any()))
                .thenThrow(new ExternalServiceUnavailableException("scale-api"));

        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        mockMvc.perform(post("/api/pesajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}


