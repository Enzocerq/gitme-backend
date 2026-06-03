package br.com.tcc.github_poc.metrics.dto;

import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;

import java.util.List;

public record FlowMetricsResponse(
        PeriodDto period,
        FlowMetrics individual,
        FlowMetrics team,
        List<DailyDurationPoint> timeInReviewSeries,
        List<RecentActivityItem> recent
) {
    public record FlowMetrics(
            Double cycleTimeHours,
            Double leadTimeHours,
            Double tcm,
            Double timeInReviewHours,
            Long activeDays
    ) {}

    public record DailyDurationPoint(java.time.LocalDate date, Double avgHours) {}

    public record RecentActivityItem(
            String kind,
            String sha,
            Integer number,
            String title,
            String state,
            String date,
            String authorLogin,
            Integer additions,
            Integer deletions
    ) {}
}
