package br.com.tcc.github_poc.client;

import br.com.tcc.github_poc.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "githubClient", url = "https://api.github.com")
public interface GithubClient {

    @GetMapping("/user/repos")
    List<GithubRepoResponse> getUserRepos(
            @RequestHeader("Authorization") String token
    );

    @GetMapping("/repos/{owner}/{repo}")
    GithubRepoDetailResponse getRepo(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo
    );

    @GetMapping("/repos/{owner}/{repo}/commits")
    List<GithubCommitResponse> getCommits(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @GetMapping("/repos/{owner}/{repo}/contributors")
    List<GithubContributorResponse> getContributors(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @GetMapping("/repos/{owner}/{repo}/pulls")
    List<GithubPullRequestResponse> getPullRequests(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @RequestParam(value = "state", defaultValue = "all") String state,
            @RequestParam(value = "sort", defaultValue = "created") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @GetMapping("/repos/{owner}/{repo}/issues")
    List<GithubIssueResponse> getIssues(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @RequestParam(value = "state", defaultValue = "all") String state,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @GetMapping("/repos/{owner}/{repo}/pulls/{pullNumber}/reviews")
    List<GithubReviewResponse> getPullRequestReviews(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("pullNumber") int pullNumber,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @GetMapping("/repos/{owner}/{repo}/pulls/{pullNumber}/commits")
    List<GithubCommitResponse> getPullRequestCommits(
            @RequestHeader("Authorization") String token,
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("pullNumber") int pullNumber,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "100") int perPage
    );

    @PostMapping(value = "/graphql", consumes = "application/json")
    GraphQLCommitStatsResponse executeGraphQL(
            @RequestHeader("Authorization") String token,
            @RequestBody GraphQLRequest request
    );
}
