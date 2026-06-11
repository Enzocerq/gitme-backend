package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.entities.Commit;
import br.com.tcc.github_poc.entities.PullRequest;
import br.com.tcc.github_poc.metrics.PeriodResolver;
import br.com.tcc.github_poc.metrics.dto.FlowMetricsResponse;
import br.com.tcc.github_poc.metrics.dto.FlowMetricsResponse.DailyDurationPoint;
import br.com.tcc.github_poc.metrics.dto.FlowMetricsResponse.FlowMetrics;
import br.com.tcc.github_poc.metrics.dto.FlowMetricsResponse.RecentActivityItem;
import br.com.tcc.github_poc.metrics.dto.common.DailyDurationProjection;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.IssueRepository;
import br.com.tcc.github_poc.repositories.PullRequestCommitRepository;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import br.com.tcc.github_poc.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowMetricsService {

    private final PullRequestCommitRepository prCommitRepo;
    private final IssueRepository issueRepo;
    private final CommitRepository commitRepo;
    private final PullRequestRepository prRepo;
    private final ReviewRepository reviewRepo;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public FlowMetricsResponse compute(Long repoId, String authorLogin, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = periodResolver.resolveFrom(from);
        LocalDateTime toDt   = periodResolver.resolveTo(to);

        Double userCycleTime    = prCommitRepo.avgCycleTimeHoursByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        Double userLeadTime     = issueRepo.avgLeadTimeHoursByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        Double userTcm          = tcm(commitRepo.sumTotalChangesByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt),
                                      commitRepo.countByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt));
        Double userTimeInReview = reviewRepo.avgTimeInReviewHoursByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        Long   userActiveDays   = commitRepo.countDistinctActiveDays(repoId, authorLogin, fromDt, toDt);

        Double teamCycleTime    = prCommitRepo.avgCycleTimeHoursByRepoPeriod(repoId, fromDt, toDt);
        Double teamLeadTime     = issueRepo.avgLeadTimeHoursByRepoPeriod(repoId, fromDt, toDt);
        Double teamTcm          = tcm(commitRepo.sumTotalChangesByRepoPeriod(repoId, fromDt, toDt),
                                      commitRepo.countByRepoPeriod(repoId, fromDt, toDt));
        Double teamTimeInReview = reviewRepo.avgTimeInReviewHoursByRepoPeriod(repoId, fromDt, toDt);

        List<DailyDurationPoint> reviewSeries = buildReviewSeries(
                reviewRepo.avgTimeInReviewByDay(repoId, fromDt, toDt),
                fromDt.toLocalDate(), toDt.toLocalDate()
        );

        List<RecentActivityItem> recent = buildRecent(repoId, authorLogin);

        return new FlowMetricsResponse(
                periodResolver.toDto(from, to),
                new FlowMetrics(userCycleTime, userLeadTime, userTcm, userTimeInReview, userActiveDays),
                new FlowMetrics(teamCycleTime, teamLeadTime, teamTcm, teamTimeInReview, null),
                reviewSeries,
                recent
        );
    }

    private Double tcm(Long sumChanges, Long count) {
        return (count != null && count > 0) ? (double) sumChanges / count : null;
    }

    private List<DailyDurationPoint> buildReviewSeries(
            List<DailyDurationProjection> projections, LocalDate start, LocalDate end) {
        var map = projections.stream()
                .collect(java.util.stream.Collectors.toMap(
                        DailyDurationProjection::getDay,
                        DailyDurationProjection::getAvgHours
                ));
        List<DailyDurationPoint> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.add(new DailyDurationPoint(d, map.get(d)));
        }
        return result;
    }

    private List<RecentActivityItem> buildRecent(Long repoId, String authorLogin) {
        List<RecentActivityItem> items = new ArrayList<>();

        commitRepo.findRecentByRepoAndAuthor(repoId, authorLogin, PageRequest.of(0, 10))
                .forEach(c -> items.add(commitItem(c)));

        prRepo.findRecentByRepoAndAuthor(repoId, authorLogin, PageRequest.of(0, 10))
                .forEach(p -> items.add(prItem(p)));

        items.sort((a, b) -> b.date().compareTo(a.date()));
        return items;
    }

    private RecentActivityItem commitItem(Commit c) {
        return new RecentActivityItem(
                "commit", c.getSha(), null,
                c.getMessageHeadline(), null,
                c.getCommitDate() != null ? c.getCommitDate().toString() : null,
                c.getAuthorLogin(),
                c.getAdditions(),
                c.getDeletions()
        );
    }

    private RecentActivityItem prItem(PullRequest p) {
        return new RecentActivityItem(
                "pr", null, p.getNumber(),
                p.getTitle(), p.getState(),
                p.getCreatedAt() != null ? p.getCreatedAt().toString() : null,
                p.getAuthorLogin(),
                null,
                null
        );
    }
}
