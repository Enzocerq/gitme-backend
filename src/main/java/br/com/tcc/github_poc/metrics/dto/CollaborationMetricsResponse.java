package br.com.tcc.github_poc.metrics.dto;

import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;

import java.util.List;

public record CollaborationMetricsResponse(
        PeriodDto period,
        Long activeContributors,
        List<ReviewDistributionEntry> reviewDistribution,
        ComparisonMetrics comparison
) {
    public record ReviewDistributionEntry(String login, Long reviews) {}

    public record ComparisonMetrics(TeamMetrics individual, TeamMetrics teamAverage) {}

    public record TeamMetrics(Double commits, Double prsMerged, Double tcm) {}
}
