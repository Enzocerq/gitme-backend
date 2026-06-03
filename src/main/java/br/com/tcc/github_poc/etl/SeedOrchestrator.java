package br.com.tcc.github_poc.etl;

import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.etl.ingestion.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SeedOrchestrator {

    @Value("${etl.seed.repos:Enzocerq/iron-benzo}")
    private String reposConfig;

    @Value("${etl.seed.since:}")
    private String sinceConfig;

    private final SeedJobState jobState;
    private final RepositoryIngestionService repositoryIngestion;
    private final ContributorIngestionService contributorIngestion;
    private final CommitIngestionService commitIngestion;
    private final PullRequestIngestionService pullRequestIngestion;
    private final IssueIngestionService issueIngestion;
    private final ReviewIngestionService reviewIngestion;
    private final PullRequestCommitIngestionService pullRequestCommitIngestion;

    @Async
    public void start(String token) {
        start(token, null);
    }

    @Async
    public void start(String token, List<String> reposOverride) {
        List<String> repos = resolveRepos(reposOverride);
        String since = resolveSince();

        if (repos.isEmpty()) {
            log.warn("ETL seed iniciado sem repos para processar (override vazio e config vazia).");
            jobState.start("");
            jobState.markDone();
            return;
        }

        log.info("Iniciando ETL seed. Repos: {}. Since: {}", repos, since);
        jobState.start(repos.get(0));

        try {
            for (String repoSlug : repos) {
                String[] parts = repoSlug.trim().split("/");
                if (parts.length != 2) {
                    log.warn("Repo inválido (formato esperado owner/repo): {}", repoSlug);
                    continue;
                }
                String owner = parts[0];
                String repo = parts[1];
                jobState.markCurrentRepo(repoSlug.trim());

                seedRepo(token, owner, repo, since);
            }
            jobState.markDone();
            log.info("ETL seed concluído. {}", jobState.snapshot());
        } catch (Exception e) {
            log.error("ETL seed falhou", e);
            jobState.markError(e.getMessage());
        }
    }

    private void seedRepo(String token, String owner, String repo, String since) {
        log.info("==> Processando {}/{}", owner, repo);

        GithubRepository repository = repositoryIngestion.ingest(token, owner, repo);
        contributorIngestion.ingest(token, owner, repo, repository);
        commitIngestion.ingest(token, owner, repo, since, repository);

        LocalDateTime sinceDateTime = LocalDate.parse(since, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        pullRequestIngestion.ingest(token, owner, repo, sinceDateTime, repository);
        issueIngestion.ingest(token, owner, repo, since + "T00:00:00Z", repository);
        reviewIngestion.ingest(token, owner, repo, repository.getId());
        pullRequestCommitIngestion.ingest(token, owner, repo, repository.getId());
    }

    private List<String> resolveRepos(List<String> override) {
        if (override != null && !override.isEmpty()) {
            return override.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(String::trim)
                    .toList();
        }
        return java.util.Arrays.stream(reposConfig.split(","))
                .filter(r -> r != null && !r.isBlank())
                .map(String::trim)
                .toList();
    }

    private String resolveSince() {
        if (sinceConfig != null && !sinceConfig.isBlank()) {
            return sinceConfig.trim();
        }
        return LocalDate.now().minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
