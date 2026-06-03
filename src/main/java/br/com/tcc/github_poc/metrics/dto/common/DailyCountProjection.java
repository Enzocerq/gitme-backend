package br.com.tcc.github_poc.metrics.dto.common;

import java.time.LocalDate;

public interface DailyCountProjection {
    LocalDate getDay();
    Long getCnt();
}
