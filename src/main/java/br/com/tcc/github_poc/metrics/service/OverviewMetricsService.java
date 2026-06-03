package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.metrics.PeriodResolver;
import br.com.tcc.github_poc.metrics.dto.OverviewMetricsResponse;
import br.com.tcc.github_poc.metrics.dto.OverviewMetricsResponse.VolumeMetrics;
import br.com.tcc.github_poc.metrics.dto.common.DailyActivityPoint;
import br.com.tcc.github_poc.metrics.dto.common.DailyCountProjection;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverviewMetricsService {

    private final CommitRepository commitRepo;
    private final PullRequestRepository prRepo;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public OverviewMetricsResponse compute(Long repoId, String authorLogin, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = periodResolver.resolveFrom(from);
        LocalDateTime toDt   = periodResolver.resolveTo(to);

        long userCommits    = commitRepo.countByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        long userPrsOpened  = prRepo.countByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        long userPrsMerged  = prRepo.countMergedByRepoAuthorPeriod(repoId, authorLogin, fromDt, toDt);
        Double userRate     = userPrsOpened > 0 ? (double) userPrsMerged / userPrsOpened : null;

        long teamCommits    = commitRepo.countByRepoPeriod(repoId, fromDt, toDt);
        long teamPrsOpened  = prRepo.countByRepoPeriod(repoId, fromDt, toDt);
        long teamPrsMerged  = prRepo.countMergedByRepoPeriod(repoId, fromDt, toDt);
        Double teamRate     = teamPrsOpened > 0 ? (double) teamPrsMerged / teamPrsOpened : null;

        List<DailyCountProjection> userCommitsByDay = commitRepo.countByDayAndAuthor(repoId, authorLogin, fromDt, toDt);
        List<DailyCountProjection> teamCommitsByDay = commitRepo.countByDay(repoId, fromDt, toDt);
        List<DailyCountProjection> userPrsByDay     = prRepo.countByDayAndAuthor(repoId, authorLogin, fromDt, toDt);
        List<DailyCountProjection> teamPrsByDay     = prRepo.countByDay(repoId, fromDt, toDt);

        List<DailyActivityPoint> series = buildSeries(
                fromDt.toLocalDate(), toDt.toLocalDate(),
                userCommitsByDay, teamCommitsByDay, userPrsByDay, teamPrsByDay
        );

        return new OverviewMetricsResponse(
                periodResolver.toDto(from, to),
                new VolumeMetrics(userCommits, userPrsOpened, userPrsMerged, userRate),
                new VolumeMetrics(teamCommits, teamPrsOpened, teamPrsMerged, teamRate),
                series
        );
    }

    private List<DailyActivityPoint> buildSeries(
            LocalDate start, LocalDate end,
            List<DailyCountProjection> userCommits,
            List<DailyCountProjection> teamCommits,
            List<DailyCountProjection> userPrs,
            List<DailyCountProjection> teamPrs) {

        Map<LocalDate, Long> ucMap  = toMap(userCommits);
        Map<LocalDate, Long> tcMap  = toMap(teamCommits);
        Map<LocalDate, Long> upMap  = toMap(userPrs);
        Map<LocalDate, Long> tpMap  = toMap(teamPrs);

        List<DailyActivityPoint> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            result.add(new DailyActivityPoint(
                    d,
                    ucMap.getOrDefault(d, 0L),
                    upMap.getOrDefault(d, 0L),
                    tcMap.getOrDefault(d, 0L),
                    tpMap.getOrDefault(d, 0L)
            ));
        }
        return result;
    }

    private Map<LocalDate, Long> toMap(List<DailyCountProjection> projections) {
        return projections.stream()
                .collect(Collectors.toMap(
                        DailyCountProjection::getDay,
                        DailyCountProjection::getCnt
                ));
    }
}
