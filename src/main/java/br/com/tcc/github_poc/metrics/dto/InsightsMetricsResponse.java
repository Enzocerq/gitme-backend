package br.com.tcc.github_poc.metrics.dto;

import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;

public record InsightsMetricsResponse(
        PeriodDto period,
        ClassificationWrapper individual,
        ClassificationWrapper team,
        java.util.List<java.util.List<Long>> productivityHeatmap
) {
    public record ClassificationWrapper(CommitClassification commitClassification) {}

    public record CommitClassification(
            Long feat,
            Long fix,
            Long other,
            Long totalConventional,
            Double featRatio,
            Double fixRatio
    ) {}
}
