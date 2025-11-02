package com.example.webdisgn.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${openapi.title:WebDisgn API}")
    private String title;

    @Value("${openapi.version:1.0}")
    private String version;

    @Value("${openapi.description:Documentazione delle API del progetto WebDisgn}")
    private String description;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description));
    }
}
