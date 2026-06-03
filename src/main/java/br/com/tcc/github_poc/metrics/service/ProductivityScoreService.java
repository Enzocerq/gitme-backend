package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.dto.ProductivityGoalsDTO;
import br.com.tcc.github_poc.dto.ProductivityMetricsDTO;
import br.com.tcc.github_poc.dto.ProductivityScoreResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class ProductivityScoreService {

    public ProductivityScoreResponseDTO calculate(
            ProductivityMetricsDTO metrics,
            ProductivityGoalsDTO goals
    ) {
        double entrega = calculateEntrega(metrics, goals);
        double eficiencia = calculateEficiencia(metrics, goals);
        double qualidade = calculateQualidade(metrics, goals);
        double colaboracao = calculateColaboracao(metrics, goals);
        double consistencia = calculateConsistencia(metrics, goals);

        double scoreFinal =
                entrega * 0.35
                        + eficiencia * 0.25
                        + qualidade * 0.20
                        + colaboracao * 0.10
                        + consistencia * 0.10;

        return new ProductivityScoreResponseDTO(
                round(clamp(scoreFinal)),
                round(entrega),
                round(eficiencia),
                round(qualidade),
                round(colaboracao),
                round(consistencia)
        );
    }

    private double calculateEntrega(ProductivityMetricsDTO metrics, ProductivityGoalsDTO goals) {
        double entregaBruta = metrics.commits() + (metrics.prsMergeados() * 3.0);

        return normalizePositive(entregaBruta, goals.metaEntrega());
    }

    private double calculateEficiencia(ProductivityMetricsDTO metrics, ProductivityGoalsDTO goals) {
        if (metrics.prsMergeados() <= 0) {
            return 0.0;
        }

        if (metrics.cycleTimeMedio() <= 0) {
            return 0.0;
        }

        if (goals.metaCycleTime() <= 0) {
            return 0.0;
        }

        return clamp((goals.metaCycleTime() / metrics.cycleTimeMedio()) * 100.0);
    }

    private double calculateQualidade(ProductivityMetricsDTO metrics, ProductivityGoalsDTO goals) {
        if (metrics.prsCriados() <= 0) {
            return 0.0;
        }

        if (goals.metaQualidade() <= 0) {
            return 0.0;
        }

        double taxaQualidade = (double) metrics.prsMergeados() / metrics.prsCriados();

        return clamp((taxaQualidade / goals.metaQualidade()) * 100.0);
    }

    private double calculateColaboracao(ProductivityMetricsDTO metrics, ProductivityGoalsDTO goals) {
        return normalizePositive(metrics.reviewsRealizadas(), goals.metaReviews());
    }

    private double calculateConsistencia(ProductivityMetricsDTO metrics, ProductivityGoalsDTO goals) {
        return normalizePositive(metrics.diasAtivos(), goals.metaDiasAtivos());
    }

    private double normalizePositive(double value, double goal) {
        if (goal <= 0) {
            return 0.0;
        }

        return clamp((value / goal) * 100.0);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}