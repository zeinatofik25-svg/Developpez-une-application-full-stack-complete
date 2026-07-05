package com.openclassrooms.mddapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String AUTH_COOKIE_SCHEME = "authCookie";

    @Bean
    public OpenAPI mddApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MDD API")
                .description("API REST de l'application MDD")
                .version("v1")
                .contact(new Contact().name("OpenClassrooms - MDD")))
            .addSecurityItem(new SecurityRequirement().addList(AUTH_COOKIE_SCHEME))
            .components(new Components().addSecuritySchemes(
                AUTH_COOKIE_SCHEME,
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .name("mdd-token")
                    .description("Cookie HttpOnly de session injecté automatiquement par le navigateur")
            ));
    }
}
