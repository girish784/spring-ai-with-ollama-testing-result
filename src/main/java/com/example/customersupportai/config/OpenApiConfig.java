package com.example.customersupportai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customerSupportOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Support AI API")
                        .version("1.0.0")
                        .description("Routes customer support messages and creates SOP-based action plans using Spring AI and Ollama."));
    }
}
