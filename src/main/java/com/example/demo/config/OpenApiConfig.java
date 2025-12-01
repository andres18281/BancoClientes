package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define la información de la API (título, versión, descripción)
        Info info = new Info()
            .title("API Bancaria Hexagonal")
            .version("1.0.0")
            .description("Documentación de la API para la gestión de clientes, cuentas y transacciones bajo arquitectura hexagonal.");

        // Define la URL del servidor donde se ejecuta la API
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Servidor Local (Desarrollo)");

        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer)); // Incluye el servidor local en la documentación
    }
}