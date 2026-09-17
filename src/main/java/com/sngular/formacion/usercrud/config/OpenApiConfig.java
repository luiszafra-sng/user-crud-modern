package com.sngular.formacion.usercrud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userCrudOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("user-crud-modern")
                .version("0.1.0")
                .description("CRUD de usuarios (Spring Boot 4 + Java 21 + H2). "
                        + "Ejemplo formativo — contiene un bug intencional en POST /users. "
                        + "Referencia interna del formador: examples/user-crud-modern/BUG.md."));
    }
}
