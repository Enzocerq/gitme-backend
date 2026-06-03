package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GithubCommitResponse;
import br.com.tcc.github_poc.entities.Commit;
import br.com.tcc.github_poc.entities.PullRequest;
import br.com.tcc.github_poc.entities.PullRequestCommit;
import br.com.tcc.github_poc.entities.PullRequestCommitId;
import br.com.tcc.github_poc.etl.SeedJobState;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.PullRequestCommitRepository;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
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
public class PullRequestCommitIngestionService {

    @Value("${etl.seed.batch-size:50}")
    private int batchSize;

    private final GithubClient githubClient;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestCommitRepository prCommitRepository;
    private final CommitRepository commitRepository;
    private final SeedJobState jobState;

    @Transactional
    public void ingest(String token, String owner, String repo, Long repositoryId) {
        log.info("Ingerindo PR commits de {}/{}", owner, repo);

        List<PullRequest> prs = pullRequestRepository.findPullRequestsByRepositoryId(repositoryId);
        log.info("PRs a processar para PR commits: {}", prs.size());

        List<PullRequestCommit> batch = new ArrayList<>();
        int saved = 0;

        for (PullRequest pr : prs) {
            int page = 1;
            while (true) {
                List<GithubCommitResponse> commits = githubClient.getPullRequestCommits(
                        token, owner, repo, pr.getNumber(), page, 100
                );
                if (commits == null || commits.isEmpty()) break;

                List<String> shas = commits.stream().map(GithubCommitResponse::sha).toList();
                Set<String> existingCommitShas = commitRepository.findAllById(shas)
                        .stream().map(Commit::getSha).collect(Collectors.toSet());

                for (GithubCommitResponse commitDto : commits) {
                    if (!existingCommitShas.contains(commitDto.sha())) continue;

                    batch.add(new PullRequestCommit(pr.getId(), commitDto.sha()));
                    if (batch.size() >= batchSize) {
                        saved += saveNew(batch);
                        batch.clear();
                    }
                }

                if (commits.size() < 100) break;
                page++;
            }
        }

        if (!batch.isEmpty()) saved += saveNew(batch);

        jobState.addPrCommits(saved);
        log.info("PR commits persistidos: {} (repo={})", saved, repo);
    }

    private int saveNew(List<PullRequestCommit> batch) {
        Set<PullRequestCommitId> existing = prCommitRepository.findAllById(
                batch.stream().map(PullRequestCommit::getId).toList()
        ).stream().map(PullRequestCommit::getId).collect(Collectors.toSet());

        List<PullRequestCommit> toSave = batch.stream()
                .filter(prc -> !existing.contains(prc.getId()))
                .toList();

        if (!toSave.isEmpty()) prCommitRepository.saveAll(toSave);
        return toSave.size();
    }
}
