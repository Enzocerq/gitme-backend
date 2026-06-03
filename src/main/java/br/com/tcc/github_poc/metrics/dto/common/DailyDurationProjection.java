package br.com.tcc.github_poc.metrics.dto.common;

import java.time.LocalDate;

public interface DailyDurationProjection {
    LocalDate getDay();
    Double getAvgHours();
}
