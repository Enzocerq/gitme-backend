package br.com.tcc.github_poc.dto;

public record ProductivityScoreResponseDTO(
        double scoreFinal,
        double entrega,
        double eficiencia,
        double qualidade,
        double colaboracao,
        double consistencia
) {
}
