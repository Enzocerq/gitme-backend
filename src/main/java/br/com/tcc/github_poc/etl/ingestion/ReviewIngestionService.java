package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GithubReviewResponse;
import br.com.tcc.github_poc.entities.PullRequest;
import br.com.tcc.github_poc.entities.Review;
import br.com.tcc.github_poc.etl.SeedJobState;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import br.com.tcc.github_poc.repositories.ReviewRepository;
import feign.FeignException;
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
public class ReviewIngestionService {

    @Value("${etl.seed.batch-size:50}")
    private int batchSize;

    private final GithubClient githubClient;
    private final PullRequestRepository pullRequestRepository;
    private final ReviewRepository reviewRepository;
    private final DtoToEntityMapper mapper;
    private final SeedJobState jobState;

    @Transactional
    public void ingest(String token, String owner, String repo, Long repositoryId) {
        log.info("Ingerindo reviews de {}/{}", owner, repo);

        List<PullRequest> prs = pullRequestRepository.findPullRequestsByRepositoryId(repositoryId);
        log.info("PRs a processar para reviews: {}", prs.size());

        List<Review> batch = new ArrayList<>();
        int saved = 0;

        for (PullRequest pr : prs) {
            int page = 1;
            while (true) {
                List<GithubReviewResponse> reviews;
                try {
                    reviews = githubClient.getPullRequestReviews(
                            token, owner, repo, pr.getNumber(), page, 100
                    );
                } catch (FeignException.NotFound e) {
                    log.warn("PR #{} não encontrada na GitHub API (pode ter sido deletada). Pulando reviews.", pr.getNumber());
                    break;
                }
                if (reviews == null || reviews.isEmpty()) break;

                for (GithubReviewResponse dto : reviews) {
                    batch.add(mapper.toReview(dto, pr));
                    if (batch.size() >= batchSize) {
                        saved += saveNew(batch);
                        batch.clear();
                    }
                }
                if (reviews.size() < 100) break;
                page++;
            }
        }

        if (!batch.isEmpty()) {
            saved += saveNew(batch);
        }

        jobState.addReviews(saved);
        log.info("Reviews persistidas: {} (repo={})", saved, repo);
    }

    private int saveNew(List<Review> batch) {
        Set<Long> existing = reviewRepository.findAllById(
                batch.stream().map(Review::getId).toList()
        ).stream().map(Review::getId).collect(Collectors.toSet());

        List<Review> toSave = batch.stream()
                .filter(r -> !existing.contains(r.getId()))
                .toList();

        if (!toSave.isEmpty()) reviewRepository.saveAll(toSave);
        return toSave.size();
    }
}
