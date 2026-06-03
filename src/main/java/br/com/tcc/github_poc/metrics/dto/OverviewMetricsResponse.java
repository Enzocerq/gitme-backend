package br.com.tcc.github_poc.metrics.dto;

import br.com.tcc.github_poc.metrics.dto.common.DailyActivityPoint;
import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;

import java.util.List;

public record OverviewMetricsResponse(
        PeriodDto period,
        VolumeMetrics individual,
        VolumeMetrics team,
        List<DailyActivityPoint> activityOverTime
) {
    public record VolumeMetrics(
            Long commits,
            Long prsOpened,
            Long prsMerged,
            Double acceptanceRate
    ) {}
}
