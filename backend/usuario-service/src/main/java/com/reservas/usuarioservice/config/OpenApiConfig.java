package com.reservas.usuarioservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Usuario Service API")
                        .version("1.0.0")
                        .description("Microservicio para la gestión y administración de usuarios del Sistema de Reservas")
                        .contact(new Contact()
                                .name("Emiliano Oviedo")
                                .url("https://github.com/OviedoEmiliano/sistema-reservas-microservicios")));
    }
}