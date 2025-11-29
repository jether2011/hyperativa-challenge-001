package br.com.hyperativa.service.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for API documentation.
 * Configures Swagger UI with JWT authentication support.
 */
@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hyperativa Card Management API")
                        .version("1.0.0")
                        .description("""
                                API for secure card number storage and retrieval.
                                
                                Features:
                                - JWT Authentication
                                - Encrypted card number storage (AES-256-GCM)
                                - Batch file upload support
                                - Card lookup by number or unique identifier
                                
                                Security:
                                - All card data is encrypted at rest
                                - JWT tokens required for all operations except authentication
                                - Request/response logging enabled
                                """)
                        .contact(new Contact()
                                .name("Hyperativa Java Challenge")
                                .url("https://github.com/jether2011/hyperativa-challenge-001")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtained from /v1/auth/login endpoint")));
    }
}
