package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.dto.GithubPullRequestResponse;
import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.entities.PullRequest;
import br.com.tcc.github_poc.etl.SeedJobState;
import br.com.tcc.github_poc.etl.extraction.PaginatedRestFetcher;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PullRequestIngestionService {

    @Value("${etl.seed.batch-size:50}")
    private int batchSize;

    private final PaginatedRestFetcher fetcher;
    private final PullRequestRepository pullRequestRepository;
    private final DtoToEntityMapper mapper;
    private final SeedJobState jobState;

    @Transactional
    public void ingest(String token, String owner, String repo, LocalDateTime since, GithubRepository repository) {
        log.info("Ingerindo PRs de {}/{} desde {}", owner, repo, since);

        String path = "/repos/" + owner + "/" + repo + "/pulls";
        Map<String, String> params = Map.of("state", "all", "sort", "created", "direction", "desc");

        List<GithubPullRequestResponse> all = fetcher.fetchAll(
                token, path, params,
                new ParameterizedTypeReference<>() {},
                last -> {
                    if (last.createdAt() == null) return false;
                    return mapper.parseDate(last.createdAt()).isBefore(since);
                }
        );

        List<PullRequest> batch = new ArrayList<>();
        int saved = 0;

        for (GithubPullRequestResponse dto : all) {
            LocalDateTime createdAt = mapper.parseDate(dto.createdAt());
            if (createdAt != null && createdAt.isBefore(since)) continue;

            batch.add(mapper.toPullRequest(dto, repository));
            if (batch.size() >= batchSize) {
                saved += saveNew(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            saved += saveNew(batch);
        }

        jobState.addPullRequests(saved);
        log.info("PRs persistidas: {} (repo={})", saved, repo);
    }

    private int saveNew(List<PullRequest> batch) {
        Set<Long> existing = pullRequestRepository.findAllById(
                batch.stream().map(PullRequest::getId).toList()
        ).stream().map(PullRequest::getId).collect(Collectors.toSet());

        List<PullRequest> toSave = batch.stream()
                .filter(pr -> !existing.contains(pr.getId()))
                .toList();

        if (!toSave.isEmpty()) pullRequestRepository.saveAll(toSave);
        return toSave.size();
    }
}
