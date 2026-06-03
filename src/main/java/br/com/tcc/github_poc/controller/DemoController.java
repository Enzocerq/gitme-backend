package br.com.tcc.github_poc.controller;

import br.com.tcc.github_poc.dto.GithubRepoResponse;
import br.com.tcc.github_poc.dto.OwnerInfo;
import br.com.tcc.github_poc.repositories.CommitRepository;
import br.com.tcc.github_poc.repositories.GithubRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/poc/demo")
@RequiredArgsConstructor
public class DemoController {

    private final GithubRepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;

    private static final List<String> DEMO_OWNERS = List.of("axios", "vuejs");

    @GetMapping("/repos")
    public ResponseEntity<List<GithubRepoResponse>> getDemoRepos() {
        List<GithubRepoResponse> repos = repositoryRepository.findAllRepositories().stream()
            .filter(r -> DEMO_OWNERS.contains(extractOwner(r.getHtmlUrl())))
            .map(r -> new GithubRepoResponse(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getHtmlUrl(),
                r.getStargazersCount() != null ? r.getStargazersCount() : 0,
                new OwnerInfo(extractOwner(r.getHtmlUrl()))
            ))
            .toList();
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/top-contributor")
    public ResponseEntity<Map<String, String>> getTopContributor(@RequestParam List<Long> repoIds) {
        String login = commitRepository.findTopContributorByRepoIds(repoIds);
        return ResponseEntity.ok(Map.of("login", login != null ? login : ""));
    }

    private String extractOwner(String htmlUrl) {
        if (htmlUrl == null || htmlUrl.isBlank()) return "";
        // https://github.com/owner/repo → owner
        String[] parts = htmlUrl.split("/");
        return parts.length >= 4 ? parts[3] : "";
    }
}
