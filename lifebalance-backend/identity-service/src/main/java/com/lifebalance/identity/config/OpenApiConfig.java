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
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI identityOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Keycloak access token using the Bearer authentication scheme")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .addServersItem(new Server()
                        .url("/")
                        .description("Current identity-service host"))
                .addTagsItem(new Tag()
                        .name("Authentication")
                        .description("Token-backed authentication and authorization inspection"))
                .addTagsItem(new Tag()
                        .name("Users")
                        .description("Current-user profile and user account administration"))
                .addTagsItem(new Tag()
                        .name("Roles")
                        .description("Role lifecycle and role-permission assignment"))
                .addTagsItem(new Tag()
                        .name("Permissions")
                        .description("Permission catalog management"))
                .addTagsItem(new Tag()
                        .name("Health")
                        .description("Service health and readiness endpoints"))
                .info(new Info()
                        .title("LifeBalance Identity Service API")
                        .description("""
                                REST API for LifeBalance identity, authentication checks, users, roles, and permissions.
                                Protected endpoints require a Keycloak JWT access token in the Authorization header.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("LifeBalance Team")
                                .email("team@lifebalance.com"))
                        .license(new License()
                                .name("LifeBalance")))
                .externalDocs(new ExternalDocumentation()
                        .description("LifeBalance Documentation"));
    }
}
