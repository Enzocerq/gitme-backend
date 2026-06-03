package br.com.tcc.github_poc.dto;

public record ProductivityGoalsDTO(
        double metaEntrega,
        double metaCycleTime,
        double metaQualidade,
        double metaReviews,
        double metaDiasAtivos
) {
}
