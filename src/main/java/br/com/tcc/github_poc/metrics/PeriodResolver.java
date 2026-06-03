package br.com.tcc.github_poc.metrics;

import br.com.tcc.github_poc.metrics.dto.common.PeriodDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class PeriodResolver {

    public LocalDateTime resolveFrom(LocalDate from) {
        return (from != null ? from : LocalDate.now().minusYears(1)).atStartOfDay();
    }

    public LocalDateTime resolveTo(LocalDate to) {
        return (to != null ? to : LocalDate.now()).atTime(23, 59, 59);
    }

    public void validate(LocalDate from, LocalDate to) {
        if (resolveFrom(from).isAfter(resolveTo(to))) {
            throw new IllegalArgumentException("'from' date must not be after 'to' date");
        }
    }

    public PeriodDto toDto(LocalDate from, LocalDate to) {
        return new PeriodDto(
                from != null ? from : LocalDate.now().minusYears(1),
                to != null ? to : LocalDate.now()
        );
    }
}
