package br.com.tcc.github_poc.etl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/poc/etl")
@Tag(name = "ETL", description = "Ingestão de dados do GitHub no Supabase. Executa seed assíncrono via GraphQL + REST paginado.")
public class SeedController {

    private final SeedOrchestrator orchestrator;
    private final SeedJobState jobState;

    @Operation(
        summary = "Iniciar seed do ETL",
        description = "Inicia a carga assíncrona dos dados do GitHub (commits, PRs, issues, reviews). " +
                      "Se o body for enviado com `repos`, sobrescreve `etl.seed.repos` do config. " +
                      "Caso contrário, usa a configuração padrão. Retorna 409 se já estiver em execução.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = false,
            description = "Lista opcional de repositórios no formato owner/repo. Sobrescreve etl.seed.repos quando enviada.",
            content = @Content(
                schema = @Schema(implementation = SeedRequest.class),
                examples = @ExampleObject(
                    name = "Lista de repos",
                    value = "{ \"repos\": [\"axios/axios\", \"owner/repo2\"] }"
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "ETL iniciado com sucesso"),
        @ApiResponse(responseCode = "409", description = "ETL já em execução")
    })
    @PostMapping("/seed")
    public ResponseEntity<String> seed(
            @Parameter(description = "Token do GitHub no formato 'Bearer ghp_...'", required = true, example = "Bearer ghp_xxxxxxxxxxxx")
            @RequestHeader("Authorization") String token,
            @RequestBody(required = false) SeedRequest body) {
        if (jobState.isRunning()) {
            return ResponseEntity.status(409).body("ETL já está em execução. Consulte GET /api/poc/etl/status.");
        }
        orchestrator.start(token, body != null ? body.repos() : null);
        return ResponseEntity.accepted().body("ETL iniciado. Acompanhe em GET /api/poc/etl/status.");
    }

    @Operation(
        summary = "Status do ETL",
        description = "Retorna o snapshot do estado atual do job de seed: progresso por entidade, erros e status geral."
    )
    @GetMapping("/status")
    public ResponseEntity<SeedJobState.Snapshot> status() {
        return ResponseEntity.ok(jobState.snapshot());
    }
}
