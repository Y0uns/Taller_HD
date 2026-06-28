package cl.usm.taller1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SansaWeigh API")
                        .version("1.0.0")
                        .description("API de Gestión de Pesaje - Microservicio SansaWeigh. "
                                + "Convierte pesos entre Sansas y kilogramos, clasifica paquetes, "
                                + "y gestiona el ciclo de vida del pesaje."));
    }
}
