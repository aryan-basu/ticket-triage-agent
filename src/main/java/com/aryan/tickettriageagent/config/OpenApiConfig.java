package com.aryan.tickettriageagent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ticketTriageAgentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticket Triage Agent API")
                        .description("REST API for creating, reviewing, and AI-triaging support tickets.")
                        .version("v1"));
    }
}
