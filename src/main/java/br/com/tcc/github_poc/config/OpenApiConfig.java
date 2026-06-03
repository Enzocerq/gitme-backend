package br.com.tcc.github_poc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TCC Dashboard — GitHub Metrics API")
                        .description("""
                                API REST para métricas de produtividade individual e de equipe baseadas em dados do GitHub.
                                Os dados são ingeridos via ETL (GraphQL + REST) e persistidos no Supabase/PostgreSQL.

                                **Repositório de testes:** axios/axios (repoId=23088740)
                                **Usuário de referência:** DigitalBrainJS
                                **Período padrão:** from=2025-05-19 &amp; to=2026-05-19
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Felipe Sousa")
                                .email("lipesousa136@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development")
                ));
    }
}
