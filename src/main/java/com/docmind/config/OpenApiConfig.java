package com.docmind.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI knowledgeHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DocMind AI API")
                        .version("1.0")
                        .description("RAG Application using Spring Boot, Ollama, and Qdrant"));
    }
}