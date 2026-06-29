package cl.usm.taller1;

import org.junit.jupiter.api.Test;

/**
 * Smoke test mínimo de la aplicación principal.
 *
 * <p>Las pruebas de integración completas con Mongo/Redis requieren
 * Testcontainers (ver plan estratégico, Fase 7). Aquí solo verificamos
 * que la clase principal existe y tiene método main — sin levantar el
 * contexto Spring (lo cual requiere Mongo+Redis reales).</p>
 */
class Taller1ApplicationTests {

    @Test
    void mainClassExistsAndHasMainMethod() throws Exception {
        var clazz = Taller1Application.class;
        var mainMethod = clazz.getMethod("main", String[].class);
        assert mainMethod != null;
        // Verificar que @SpringBootApplication está presente
        assert clazz.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class);
    }

}