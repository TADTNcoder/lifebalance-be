package com.lifebalance.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI identityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LifeBalance Identity Service API")
                        .description("REST API for Identity Service")
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
