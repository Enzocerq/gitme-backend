package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.metrics.PeriodResolver;
import br.com.tcc.github_poc.metrics.dto.RepoMetricsResponse;
import br.com.tcc.github_poc.metrics.dto.RepoMetricsResponse.RepoSummary;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.GithubRepositoryRepository;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoMetricsService {

    private final GithubRepositoryRepository repoRepo;
    private final CommitRepository commitRepo;
    private final PullRequestRepository prRepo;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public RepoMetricsResponse compute(String authorLogin, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = periodResolver.resolveFrom(from);
        LocalDateTime toDt   = periodResolver.resolveTo(to);

        List<GithubRepository> repos = repoRepo.findReposByAuthorLoginAndPeriod(authorLogin, fromDt, toDt);

        List<RepoSummary> summaries = repos.stream()
                .map(r -> buildSummary(r, authorLogin, fromDt, toDt))
                .sorted((a, b) -> Long.compare(b.userCommits(), a.userCommits()))
                .toList();

        return new RepoMetricsResponse(periodResolver.toDto(from, to), summaries);
    }

    private RepoSummary buildSummary(GithubRepository repo, String login,
                                     LocalDateTime from, LocalDateTime to) {
        long totalCommits = commitRepo.countByRepoPeriod(repo.getId(), from, to);
        long userCommits  = commitRepo.countByRepoAuthorPeriod(repo.getId(), login, from, to);
        long totalPrs     = prRepo.countByRepoPeriod(repo.getId(), from, to);
        long userPrs      = prRepo.countByRepoAuthorPeriod(repo.getId(), login, from, to);
        Double participation = totalCommits > 0 ? (double) userCommits / totalCommits : null;

        return new RepoSummary(
                repo.getId(), repo.getName(),
                totalCommits, userCommits, participation,
                totalPrs, userPrs
        );
    }
}
