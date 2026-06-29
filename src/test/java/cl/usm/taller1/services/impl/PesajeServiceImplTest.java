package cl.usm.taller1.services.impl;

import cl.usm.taller1.documents.RegistroPesaje;
import cl.usm.taller1.documents.TransicionEstado;
import cl.usm.taller1.dto.ActualizarEstadoRequestDTO;
import cl.usm.taller1.dto.PesajeRequestDTO;
import cl.usm.taller1.dto.PesajeResponseDTO;
import cl.usm.taller1.enums.CategoriaPeso;
import cl.usm.taller1.enums.EstadoPesaje;
import cl.usm.taller1.exceptions.BalanzaPrimaRestrictionException;
import cl.usm.taller1.exceptions.IllegalWeighingStateException;
import cl.usm.taller1.exceptions.RestriccionHorarioNocturnoException;
import cl.usm.taller1.repositories.PesajeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PesajeServiceImpl - Lógica de negocio end-to-end")
class PesajeServiceImplTest {

    @Mock
    private PesajeRepository repository;

    @InjectMocks
    private PesajeServiceImpl service;

    private MockedStatic<LocalDate> localDateMock;
    private MockedStatic<LocalTime> localTimeMock;

    @BeforeEach
    void setUp() {
        // Por defecto: día par y hora diurna → no se disparan las restricciones
        localDateMock = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
        localTimeMock = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        if (localDateMock != null) localDateMock.close();
        if (localTimeMock != null) localTimeMock.close();
    }

    private void stubNow(LocalDate fecha, LocalTime hora) {
        localDateMock.when(LocalDate::now).thenReturn(fecha);
        localTimeMock.when(LocalTime::now).thenReturn(hora);
    }

    // ============== crearRegistro ==============

    @Test
    @DisplayName("crearRegistro - LIVIANO: éxito sin disparar restricciones")
    void crear_liviano_exito() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("5"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("LIVIANO");
        assertThat(resp.estadoActual()).isEqualTo("INGRESADO");
        assertThat(resp.pesoEnKilogramos()).isEqualByComparingTo(new BigDecimal("6.685000"));
        assertThat(resp.historialTransiciones()).hasSize(1);
        assertThat(resp.historialTransiciones().get(0).estadoAnterior()).isNull();
        assertThat(resp.historialTransiciones().get(0).estadoNuevo()).isEqualTo("INGRESADO");
        verify(repository, times(1)).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("crearRegistro - MEDIANO: éxito sin disparar restricciones")
    void crear_mediano_exito() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("25"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("MEDIANO");
        assertThat(resp.estadoActual()).isEqualTo("INGRESADO");
    }

    @Test
    @DisplayName("crearRegistro - PESADO en día par y hora diurna: éxito")
    void crear_pesado_diaPar_horarioDiurno_exito() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("PESADO");
        assertThat(resp.estadoActual()).isEqualTo("INGRESADO");
    }

    @Test
    @DisplayName("crearRegistro - PESADO en horario nocturno: lanza RestriccionHorarioNocturnoException")
    void crear_pesado_horarioNocturno_lanzaExcepcion() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(23, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        assertThatThrownBy(() -> service.crearRegistro(req))
                .isInstanceOf(RestriccionHorarioNocturnoException.class);
        verify(repository, never()).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("crearRegistro - PESADO en horario 00:00 (cruza medianoche): lanza excepción")
    void crear_pesado_madrugada_lanzaExcepcion() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(0, 30));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("100"));

        assertThatThrownBy(() -> service.crearRegistro(req))
                .isInstanceOf(RestriccionHorarioNocturnoException.class);
        verify(repository, never()).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("crearRegistro - PESADO con id balanza primo en día impar: lanza BalanzaPrimaRestrictionException")
    void crear_pesado_balanzaPrima_diaImpar_lanzaExcepcion() {
        stubNow(LocalDate.of(2026, 6, 3), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("7", "P-1", new BigDecimal("100"));

        assertThatThrownBy(() -> service.crearRegistro(req))
                .isInstanceOf(BalanzaPrimaRestrictionException.class)
                .hasMessageContaining("7")
                .hasMessageContaining("3");
        verify(repository, never()).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("crearRegistro - PESADO con id balanza primo en día par: permitido")
    void crear_pesado_balanzaPrima_diaPar_exito() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("7", "P-1", new BigDecimal("100"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("PESADO");
    }

    @Test
    @DisplayName("crearRegistro - PESADO con id balanza NO primo en día impar: permitido")
    void crear_pesado_balanzaNoPrima_diaImpar_exito() {
        stubNow(LocalDate.of(2026, 6, 3), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("8", "P-1", new BigDecimal("100"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("PESADO");
    }

    @Test
    @DisplayName("crearRegistro - PESADO con id balanza no numérico: salta la regla (no se puede parsear)")
    void crear_pesado_balanzaNoNumerica_saltaRegla() {
        stubNow(LocalDate.of(2026, 6, 3), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("abc", "P-1", new BigDecimal("100"));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO resp = service.crearRegistro(req);

        assertThat(resp.categoriaPeso()).isEqualTo("PESADO");
    }

    @Test
    @DisplayName("crearRegistro - categoría LIVIANO/MEDIANO nunca dispara restricciones horarias ni de balanza")
    void crear_livianoOMediano_nuncaDisparaRestricciones() {
        stubNow(LocalDate.of(2026, 6, 3), LocalTime.of(23, 0)); // día impar y nocturno
        PesajeRequestDTO reqL = new PesajeRequestDTO("7", "P-L", new BigDecimal("5"));   // LIVIANO
        PesajeRequestDTO reqM = new PesajeRequestDTO("11", "P-M", new BigDecimal("30")); // MEDIANO
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        PesajeResponseDTO respL = service.crearRegistro(reqL);
        PesajeResponseDTO respM = service.crearRegistro(reqM);

        assertThat(respL.categoriaPeso()).isEqualTo("LIVIANO");
        assertThat(respM.categoriaPeso()).isEqualTo("MEDIANO");
        verify(repository, times(2)).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("crearRegistro - persiste pesoEnKilogramos calculado correctamente")
    void crear_calculaKgCorrectamente() {
        stubNow(LocalDate.of(2026, 6, 2), LocalTime.of(12, 0));
        PesajeRequestDTO req = new PesajeRequestDTO("100", "P-1", new BigDecimal("10"));
        ArgumentCaptor<RegistroPesaje> captor = ArgumentCaptor.forClass(RegistroPesaje.class);
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        service.crearRegistro(req);

        verify(repository).save(captor.capture());
        RegistroPesaje guardado = captor.getValue();
        assertThat(guardado.getPesoEnKilogramos()).isEqualByComparingTo(new BigDecimal("13.370000"));
        assertThat(guardado.getPesoEnSansas()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(guardado.getCreatedAt()).isEqualTo(guardado.getUpdatedAt());
    }

    // ============== actualizarEstado ==============

    @Test
    @DisplayName("actualizarEstado - transición válida: éxito, agrega entrada al historial")
    void actualizar_transicionValida_exito() {
        RegistroPesaje reg = RegistroPesaje.builder()
                .id("reg-1")
                .idBalanza("100")
                .idPaquete("P-1")
                .pesoEnSansas(new BigDecimal("10"))
                .pesoEnKilogramos(new BigDecimal("13.37"))
                .categoriaPeso(CategoriaPeso.LIVIANO)
                .estadoActual(EstadoPesaje.INGRESADO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.findById("reg-1")).thenReturn(Optional.of(reg));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("PESADO");
        PesajeResponseDTO resp = service.actualizarEstado("reg-1", req);

        assertThat(resp.estadoActual()).isEqualTo("PESADO");
        assertThat(resp.historialTransiciones()).hasSize(1);
        assertThat(resp.historialTransiciones().get(0).estadoAnterior()).isEqualTo("INGRESADO");
        assertThat(resp.historialTransiciones().get(0).estadoNuevo()).isEqualTo("PESADO");
    }

    @Test
    @DisplayName("actualizarEstado - transición inválida: lanza IllegalWeighingStateException")
    void actualizar_transicionInvalida_lanzaExcepcion() {
        RegistroPesaje reg = RegistroPesaje.builder()
                .id("reg-1")
                .idBalanza("100")
                .idPaquete("P-1")
                .pesoEnSansas(new BigDecimal("10"))
                .pesoEnKilogramos(new BigDecimal("13.37"))
                .categoriaPeso(CategoriaPeso.LIVIANO)
                .estadoActual(EstadoPesaje.INGRESADO)
                .build();
        when(repository.findById("reg-1")).thenReturn(Optional.of(reg));

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("APROBADO");

        assertThatThrownBy(() -> service.actualizarEstado("reg-1", req))
                .isInstanceOf(IllegalWeighingStateException.class);
        verify(repository, never()).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("actualizarEstado - registro no encontrado: lanza NoSuchElementException")
    void actualizar_noEncontrado_lanzaExcepcion() {
        when(repository.findById("no-existe")).thenReturn(Optional.empty());

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("PESADO");

        assertThatThrownBy(() -> service.actualizarEstado("no-existe", req))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessageContaining("no-existe");
        verify(repository, never()).save(any(RegistroPesaje.class));
    }

    @Test
    @DisplayName("actualizarEstado - normaliza el estado a mayúsculas")
    void actualizar_normalizaMayusculas() {
        RegistroPesaje reg = RegistroPesaje.builder()
                .id("reg-1")
                .idBalanza("100")
                .idPaquete("P-1")
                .pesoEnSansas(new BigDecimal("10"))
                .pesoEnKilogramos(new BigDecimal("13.37"))
                .categoriaPeso(CategoriaPeso.LIVIANO)
                .estadoActual(EstadoPesaje.INGRESADO)
                .build();
        when(repository.findById("reg-1")).thenReturn(Optional.of(reg));
        when(repository.save(any(RegistroPesaje.class))).thenAnswer(inv -> inv.getArgument(0));

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("pesado");
        PesajeResponseDTO resp = service.actualizarEstado("reg-1", req);

        assertThat(resp.estadoActual()).isEqualTo("PESADO");
    }

    @Test
    @DisplayName("actualizarEstado - estado desconocido: lanza IllegalArgumentException por Enum.valueOf")
    void actualizar_estadoInvalido_lanzaExcepcion() {
        RegistroPesaje reg = RegistroPesaje.builder()
                .id("reg-1")
                .idBalanza("100")
                .idPaquete("P-1")
                .pesoEnSansas(new BigDecimal("10"))
                .pesoEnKilogramos(new BigDecimal("13.37"))
                .categoriaPeso(CategoriaPeso.LIVIANO)
                .estadoActual(EstadoPesaje.INGRESADO)
                .build();
        when(repository.findById("reg-1")).thenReturn(Optional.of(reg));

        ActualizarEstadoRequestDTO req = new ActualizarEstadoRequestDTO("VOLANDO");

        assertThatThrownBy(() -> service.actualizarEstado("reg-1", req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ============== obtenerPorFiltros ==============

    @Test
    @DisplayName("obtenerPorFiltros - con rango de fechas: usa findByCreatedAtBetween")
    void obtener_conRango_llamaFindByCreatedAtBetween() {
        LocalDateTime desde = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 6, 30, 23, 59);
        RegistroPesaje r1 = RegistroPesaje.builder().id("a").categoriaPeso(CategoriaPeso.LIVIANO)
                .estadoActual(EstadoPesaje.INGRESADO).build();
        RegistroPesaje r2 = RegistroPesaje.builder().id("b").categoriaPeso(CategoriaPeso.MEDIANO)
                .estadoActual(EstadoPesaje.PESADO).build();
        when(repository.findByCreatedAtBetween(desde, hasta)).thenReturn(List.of(r1, r2));

        List<PesajeResponseDTO> resultado = service.obtenerPorFiltros(desde, hasta);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).id()).isEqualTo("a");
        assertThat(resultado.get(1).id()).isEqualTo("b");
        verify(repository).findByCreatedAtBetween(desde, hasta);
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("obtenerPorFiltros - sin filtros: usa findAll")
    void obtener_sinFiltros_llamaFindAll() {
        when(repository.findAll()).thenReturn(List.of());

        List<PesajeResponseDTO> resultado = service.obtenerPorFiltros(null, null);

        assertThat(resultado).isEmpty();
        verify(repository).findAll();
        verify(repository, never()).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("obtenerPorFiltros - solo desde: usa findAll (ambos nulos o uno solo)")
    void obtener_soloDesde_llamaFindAll() {
        when(repository.findAll()).thenReturn(List.of());

        service.obtenerPorFiltros(LocalDateTime.of(2026, 6, 1, 0, 0), null);

        verify(repository).findAll();
    }

    @Test
    @DisplayName("obtenerPorFiltros - solo hasta: usa findAll")
    void obtener_soloHasta_llamaFindAll() {
        when(repository.findAll()).thenReturn(List.of());

        service.obtenerPorFiltros(null, LocalDateTime.of(2026, 6, 30, 0, 0));

        verify(repository).findAll();
    }
}