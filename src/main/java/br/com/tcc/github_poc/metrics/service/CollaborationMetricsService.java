package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.metrics.PeriodResolver;
import br.com.tcc.github_poc.metrics.dto.CollaborationMetricsResponse;
import br.com.tcc.github_poc.metrics.dto.CollaborationMetricsResponse.ComparisonMetrics;
import br.com.tcc.github_poc.metrics.dto.CollaborationMetricsResponse.ReviewDistributionEntry;
import br.com.tcc.github_poc.metrics.dto.CollaborationMetricsResponse.TeamMetrics;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import br.com.tcc.github_poc.repositories.RepositoryContributorRepository;
import br.com.tcc.github_poc.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborationMetricsService {

    private final CommitRepository commitRepo;
    private final PullRequestRepository prRepo;
    private final ReviewRepository reviewRepo;
    private final RepositoryContributorRepository contributorRepo;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public CollaborationMetricsResponse compute(Long repoId, String authorLogin, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = periodResolver.resolveFrom(from);
        LocalDateTime toDt   = periodResolver.resolveTo(to);

        Long activeContributors = contributorRepo.countContributorsByRepositoryId(repoId);

        List<ReviewDistributionEntry> reviewDist = reviewRepo
                .countReviewsByAuthorGrouped(repoId, fromDt, toDt, 10)
                .stream()
                .map(row -> new ReviewDistributionEntry(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();

        long userCommits   = commitRepo.countByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        long userMerged    = prRepo.countMergedByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        Double userTcm     = tcm(commitRepo.sumTotalChangesByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt),
                                  commitRepo.countByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt));

        long teamCommits   = commitRepo.countByRepoPeriod(repoId, fromDt, toDt);
        long teamMerged    = prRepo.countMergedByRepoPeriod(repoId, fromDt, toDt);
        Double teamTcm     = tcm(commitRepo.sumTotalChangesByRepoPeriod(repoId, fromDt, toDt),
                                  commitRepo.countByRepoPeriod(repoId, fromDt, toDt));
        double totalContribs = Math.max(activeContributors, 1);
        TeamMetrics teamAvg = new TeamMetrics(
                teamCommits / totalContribs,
                teamMerged  / totalContribs,
                teamTcm
        );

        return new CollaborationMetricsResponse(
                periodResolver.toDto(from, to),
                activeContributors,
                reviewDist,
                new ComparisonMetrics(
                        new TeamMetrics((double) userCommits, (double) userMerged, userTcm),
                        teamAvg
                )
        );
    }

    private Double tcm(Long sumChanges, Long count) {
        return (count != null && count > 0) ? (double) sumChanges / count : null;
    }
}
