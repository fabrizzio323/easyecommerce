package com.fabrizio.easyecommerce.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Easy E-commerce API",
                version = "1.0",
                description = "API for managing products, categories, and user authentication in an e-commerce application"
        )
)
public class OpenApiConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiConfig.class);

    @Bean
    public OpenAPI customOpenAPI() {
        logger.info("Configuring OpenAPI/Swagger documentation");
        OpenAPI openAPI = new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
        logger.info("OpenAPI configuration completed with JWT Bearer authentication");
        return openAPI;
    }
}
