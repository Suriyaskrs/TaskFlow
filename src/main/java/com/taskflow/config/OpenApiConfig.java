package com.taskflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 configuration.
 *
 * Adds JWT bearer auth support to the Swagger UI.
 * Access Swagger at: http://localhost:8080/swagger-ui.html
 *
 * To test protected endpoints in Swagger:
 *   1. Call /auth/login to get a token
 *   2. Click "Authorize" button in Swagger UI
 *   3. Enter: Bearer <your-token>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TaskFlow API",
        version = "1.0.0",
        description = "Smart Task Management System — REST API documentation",
        contact = @Contact(name = "TaskFlow", email = "support@taskflow.com")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Enter your JWT token (obtained from /auth/login)"
)
public class OpenApiConfig {
    // Configuration is annotation-driven — no method body needed
}