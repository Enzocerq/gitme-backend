package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.dto.GithubIssueResponse;
import br.com.tcc.github_poc.dto.OwnerInfo;
import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.entities.Issue;
import br.com.tcc.github_poc.entities.IssueAssignee;
import br.com.tcc.github_poc.entities.IssueLabel;
import br.com.tcc.github_poc.etl.SeedJobState;
import br.com.tcc.github_poc.etl.extraction.PaginatedRestFetcher;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.IssueAssigneeRepository;
import br.com.tcc.github_poc.repositories.IssueLabelRepository;
import br.com.tcc.github_poc.repositories.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class IssueIngestionService {

    @Value("${etl.seed.batch-size:50}")
    private int batchSize;

    private final PaginatedRestFetcher fetcher;
    private final IssueRepository issueRepository;
    private final IssueAssigneeRepository assigneeRepository;
    private final IssueLabelRepository labelRepository;
    private final DtoToEntityMapper mapper;
    private final SeedJobState jobState;

    @Transactional
    public void ingest(String token, String owner, String repo, String since, GithubRepository repository) {
        log.info("Ingerindo issues de {}/{} desde {}", owner, repo, since);

        String path = "/repos/" + owner + "/" + repo + "/issues";
        Map<String, String> params = since != null && !since.isBlank()
                ? Map.of("state", "all", "since", since)
                : Map.of("state", "all");

        List<GithubIssueResponse> all = fetcher.fetchAll(
                token, path, params,
                new ParameterizedTypeReference<>() {}
        );

        int saved = 0;
        List<Issue> issueBatch = new ArrayList<>();
        List<IssueAssignee> assigneeBatch = new ArrayList<>();
        List<IssueLabel> labelBatch = new ArrayList<>();

        for (GithubIssueResponse dto : all) {
            if (dto.pullRequest() != null) continue;

            Issue issue = mapper.toIssue(dto, repository);
            issueBatch.add(issue);

            if (dto.assignees() != null) {
                for (OwnerInfo a : dto.assignees()) {
                    assigneeBatch.add(mapper.toAssignee(a, issue));
                }
            }
            if (dto.labels() != null) {
                for (GithubIssueResponse.LabelInfo l : dto.labels()) {
                    labelBatch.add(mapper.toLabel(l, issue));
                }
            }

            if (issueBatch.size() >= batchSize) {
                saved += flushBatch(issueBatch, assigneeBatch, labelBatch);
                issueBatch.clear();
                assigneeBatch.clear();
                labelBatch.clear();
            }
        }
        if (!issueBatch.isEmpty()) {
            saved += flushBatch(issueBatch, assigneeBatch, labelBatch);
        }

        jobState.addIssues(saved);
        log.info("Issues persistidas: {} (repo={})", saved, repo);
    }

    // 1 SELECT em lote para Issue; assignees/labels só são salvos para issues novas
    private int flushBatch(List<Issue> issues, List<IssueAssignee> assignees, List<IssueLabel> labels) {
        Set<Long> existing = issueRepository.findAllById(
                issues.stream().map(Issue::getId).toList()
        ).stream().map(Issue::getId).collect(Collectors.toSet());

        List<Issue> newIssues = issues.stream()
                .filter(i -> !existing.contains(i.getId()))
                .toList();

        if (newIssues.isEmpty()) return 0;

        Set<Long> newIds = newIssues.stream().map(Issue::getId).collect(Collectors.toSet());
        issueRepository.saveAll(newIssues);

        // IssueAssignee/IssueLabel usam @GeneratedValue; só salva os vinculados a issues novas
        List<IssueAssignee> newAssignees = assignees.stream()
                .filter(a -> newIds.contains(a.getIssue().getId()))
                .toList();
        if (!newAssignees.isEmpty()) assigneeRepository.saveAll(newAssignees);

        List<IssueLabel> newLabels = labels.stream()
                .filter(l -> newIds.contains(l.getIssue().getId()))
                .toList();
        if (!newLabels.isEmpty()) labelRepository.saveAll(newLabels);

        return newIssues.size();
    }
}
