package com.organizame.reportes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    String moduleName;

    @Value("${server.servlet.context-path}")
    String apiVersion;

    @Bean
    OpenAPI customOpenAPI() {
        final String apiTitle = String.format("%s API", StringUtils.capitalize(moduleName));
        return new OpenAPI()
                .info(new Info().title(apiTitle).version(apiVersion)
                        .contact(new Contact().email("vivadagon@gmail.com")));
    }

}