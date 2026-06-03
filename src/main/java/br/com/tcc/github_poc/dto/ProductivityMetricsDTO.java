package br.com.tcc.github_poc.dto;

public record ProductivityMetricsDTO(
        int commits,
        int prsCriados,
        int prsMergeados,
        double cycleTimeMedio,
        int reviewsRealizadas,
        int diasAtivos
) {
}
