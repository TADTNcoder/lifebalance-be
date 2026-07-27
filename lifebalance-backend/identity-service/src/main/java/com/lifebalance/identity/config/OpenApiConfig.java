package com.lifebalance.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI identityOpenAPI() {
                return new OpenAPI()

                                .info(new Info()
                                                .title("LifeBalance Identity Service API")
                                                .description("REST API for Identity, User, Role and Permission Management")
                                                .version("v1.0.0")
                                                .contact(new Contact()
                                                                .name("LifeBalance Team")
                                                                .email("team@lifebalance.com"))
                                                .license(new License()
                                                                .name("LifeBalance")))

                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))

                                .components(new Components()
                                                .addSecuritySchemes(
                                                                "Bearer Authentication",
                                                                new SecurityScheme()
                                                                                .name("Authorization")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}
