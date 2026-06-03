package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.dto.GraphQLCommitStatsResponse.Node;
import br.com.tcc.github_poc.entities.Commit;
import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.etl.SeedJobState;
import br.com.tcc.github_poc.etl.extraction.GraphQLCommitFetcher;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.CommitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommitIngestionService {

    @Value("${etl.seed.batch-size:50}")
    private int batchSize;

    private final GraphQLCommitFetcher graphQLCommitFetcher;
    private final CommitRepository commitRepository;
    private final DtoToEntityMapper mapper;
    private final SeedJobState jobState;

    @Transactional
    public void ingest(String token, String owner, String repo, String since, GithubRepository repository) {
        log.info("Ingerindo commits via GraphQL de {}/{} desde {}", owner, repo, since);

        String sinceTimestamp = since.contains("T") ? since : since + "T00:00:00Z";
        List<Node> nodes = graphQLCommitFetcher.fetchAll(token, owner, repo, sinceTimestamp);
        log.info("Total de commits obtidos via GraphQL: {}", nodes.size());

        List<Commit> batch = new ArrayList<>();
        int saved = 0;

        for (Node node : nodes) {
            batch.add(mapper.toCommit(node, repository));
            if (batch.size() >= batchSize) {
                saved += saveNew(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            saved += saveNew(batch);
        }

        jobState.addCommits(saved);
        log.info("Commits persistidos: {} (repo={})", saved, repo);
    }

    private int saveNew(List<Commit> batch) {
        Set<String> existing = commitRepository.findAllById(
                batch.stream().map(Commit::getSha).toList()
        ).stream().map(Commit::getSha).collect(Collectors.toSet());

        List<Commit> toSave = batch.stream()
                .filter(c -> !existing.contains(c.getSha()))
                .toList();

        if (!toSave.isEmpty()) commitRepository.saveAll(toSave);
        return toSave.size();
    }
}
