package br.com.tcc.github_poc.controller;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GithubRepoResponse;
import br.com.tcc.github_poc.dto.GithubCommitResponse;
import br.com.tcc.github_poc.dto.GithubContributorResponse;
import br.com.tcc.github_poc.dto.GithubPullRequestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.tcc.github_poc.dto.*;
import java.util.List;

@RestController
@RequestMapping("/api/poc/github")
public class GithubController {

    private final GithubClient githubClient;

    public GithubController(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    @GetMapping("/repositorios")
    public ResponseEntity<List<GithubRepoResponse>> buscarRepositorios(
            @RequestHeader("Authorization") String token) {

        List<GithubRepoResponse> repos = githubClient.getUserRepos(token);
        return ResponseEntity.ok(repos);
    }

    @GetMapping("/{owner}/{repo}/commits")
    public ResponseEntity<List<GithubCommitResponse>> buscarCommits(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo) {

        List<GithubCommitResponse> commits = githubClient.getCommits(token, owner, repo, null, 1, 100);
        return ResponseEntity.ok(commits);
    }

    @GetMapping("/{owner}/{repo}/contribuidores")
    public ResponseEntity<List<GithubContributorResponse>> buscarContribuidores(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo) {

        List<GithubContributorResponse> contribuidores = githubClient.getContributors(token, owner, repo, 1, 100);
        return ResponseEntity.ok(contribuidores);
    }

    @GetMapping("/{owner}/{repo}/prs")
    public ResponseEntity<List<GithubPullRequestResponse>> buscarPullRequests(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo) {

        List<GithubPullRequestResponse> prs = githubClient.getPullRequests(token, owner, repo, "all", "created", "desc", 1, 100);
        return ResponseEntity.ok(prs);
    }

    @GetMapping("/{owner}/{repo}/issues")
    public ResponseEntity<List<GithubIssueResponse>> buscarIssues(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo) {

        List<GithubIssueResponse> issues = githubClient.getIssues(token, owner, repo, "all", null, 1, 100);
        return ResponseEntity.ok(issues);
    }

    @GetMapping("/{owner}/{repo}/prs/{pullNumber}/reviews")
    public ResponseEntity<List<GithubReviewResponse>> buscarReviewsDePr(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable int pullNumber) {

        List<GithubReviewResponse> reviews = githubClient.getPullRequestReviews(token, owner, repo, pullNumber, 1, 100);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{owner}/{repo}/commits/stats")
    public ResponseEntity<GraphQLCommitStatsResponse> buscarEstatisticasCommits(
            @RequestHeader("Authorization") String token,
            @PathVariable String owner,
            @PathVariable String repo) {

        String query = String.format("""
            query {
              repository(owner: "%s", name: "%s") {
                defaultBranchRef {
                  target {
                    ... on Commit {
                      history(first: 50) {
                        nodes {
                          oid
                          messageHeadline
                          additions
                          deletions
                          author {
                            date
                            user { login }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """, owner, repo);

        GraphQLRequest request = new GraphQLRequest(query);
        GraphQLCommitStatsResponse response = githubClient.executeGraphQL(token, request);

        return ResponseEntity.ok(response);
    }
}