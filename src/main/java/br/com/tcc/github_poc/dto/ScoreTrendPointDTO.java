package br.com.tcc.github_poc.dto;

import java.time.LocalDate;

/**
 * Um ponto da série temporal do Score de Produtividade.
 * O score é calculado sobre uma janela móvel encerrada em {@code date}, permitindo
 * acompanhar a evolução (a tendência importa mais que o valor absoluto).
 */
public record ScoreTrendPointDTO(
        LocalDate date,
        double score
) {
}
