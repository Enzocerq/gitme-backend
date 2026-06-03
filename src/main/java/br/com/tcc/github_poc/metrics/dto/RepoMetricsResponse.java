package br.com.tcc.github_poc.metrics.dto;

import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;

import java.util.List;

public record RepoMetricsResponse(
        PeriodDto period,
        List<RepoSummary> repos
) {
    public record RepoSummary(
            Long repoId,
            String name,
            Long totalCommits,
            Long userCommits,
            Double participation,
            Long totalPrs,
            Long userPrs
    ) {}
}
