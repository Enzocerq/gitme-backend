package br.com.tcc.github_poc.metrics.dto.common;

import java.time.LocalDate;

public record DailyActivityPoint(
        LocalDate date,
        Long commits,
        Long prs,
        Long teamCommits,
        Long teamPrs
) {}
