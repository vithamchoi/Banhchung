package com.quannhabaninh.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Quán Nhà Bà Ninh - API Documentation", version = "1.0.0", description = "REST API cho hệ thống bán bánh chưng Tết - Quán Nhà Bà Ninh", contact = @Contact(name = "Support Team", email = "support@quannhabaninh.com")), servers = {
        @Server(url = "http://localhost:8080", description = "Development Server")
})
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer", description = "JWT token authentication. Format: Bearer {token}")
public class OpenApiConfig {
    // Configuration class for OpenAPI/Swagger documentation
    // SpringDoc will automatically scan all @RestController classes
    // and generate API documentation based on annotations
}
