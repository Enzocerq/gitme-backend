package br.com.tcc.github_poc.dto;

public interface ProductivityMetricsProjection {

    Integer getCommits();

    Integer getPrsCriados();

    Integer getPrsMergeados();

    Double getCycleTimeMedio();

    Integer getReviewsRealizadas();

    Integer getDiasAtivos();
}
