package pe.edu.upc.fiadito.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI (http://localhost:8080/swagger-ui.html) con autenticación Bearer JWT.
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI fiaditoOpenApi() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info()
                        .title("Fiadito API")
                        .description("""
                                API REST de Fiadito: registro del negocio, socios, productos, ventas al fiado en pago único
                                o en cuotas quincenales iguales, reporte de corte, pagos con interés moratorio y bitácora.
                                Inicia sesión en POST /api/v1/auth/login y usa el token en el botón Authorize.
                                Proyecto académico SI642 Finanzas e Ingeniería Económica – UPC.
                                """)
                        .version("v1.0.0"));
    }
}
