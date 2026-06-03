package br.com.tcc.github_poc.metrics.service;

import br.com.tcc.github_poc.metrics.PeriodResolver;
import br.com.tcc.github_poc.metrics.dto.InsightsMetricsResponse;
import br.com.tcc.github_poc.metrics.dto.InsightsMetricsResponse.ClassificationWrapper;
import br.com.tcc.github_poc.metrics.dto.InsightsMetricsResponse.CommitClassification;
import br.com.tcc.github_poc.metrics.support.ConventionalCommitClassifier;
import br.com.tcc.github_poc.repositories.CommitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightsMetricsService {

    private final CommitRepository commitRepo;
    private final ConventionalCommitClassifier classifier;
    private final PeriodResolver periodResolver;

    @Transactional(readOnly = true)
    public InsightsMetricsResponse compute(Long repoId, String authorLogin, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = periodResolver.resolveFrom(from);
        LocalDateTime toDt   = periodResolver.resolveTo(to);

        var userCommits = commitRepo
                .findCommitsByRepositoryIdAndAuthorLogin(repoId, authorLogin)
                .stream()
                .filter(c -> c.getCommitDate() != null
                        && !c.getCommitDate().isBefore(fromDt)
                        && !c.getCommitDate().isAfter(toDt))
                .toList();

        List<String> userHeadlines = userCommits.stream()
                .map(c -> c.getMessageHeadline())
                .toList();

        List<String> teamHeadlines = commitRepo
                .findCommitsByRepositoryId(repoId)
                .stream()
                .filter(c -> c.getCommitDate() != null
                        && !c.getCommitDate().isBefore(fromDt)
                        && !c.getCommitDate().isAfter(toDt))
                .map(c -> c.getMessageHeadline())
                .toList();

        CommitClassification userClass = classifier.classify(userHeadlines);
        CommitClassification teamClass = classifier.classify(teamHeadlines);

        long[][] grid = new long[7][24];
        userCommits.forEach(c -> {
            int day = c.getCommitDate().getDayOfWeek().getValue() - 1; // Mon=0..Sun=6
            int hour = c.getCommitDate().getHour();
            grid[day][hour]++;
        });
        List<List<Long>> heatmap = Arrays.stream(grid)
                .map(row -> Arrays.stream(row).boxed().collect(Collectors.toList()))
                .collect(Collectors.toList());

        return new InsightsMetricsResponse(
                periodResolver.toDto(from, to),
                new ClassificationWrapper(userClass),
                new ClassificationWrapper(teamClass),
                heatmap
        );
    }
}
