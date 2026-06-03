package br.com.tcc.github_poc.etl.extraction;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GraphQLCommitStatsResponse;
import br.com.tcc.github_poc.dto.GraphQLCommitStatsResponse.Node;
import br.com.tcc.github_poc.dto.GraphQLRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class GraphQLCommitFetcher {

    private static final String QUERY = """
            query($owner: String!, $repo: String!, $since: GitTimestamp!, $cursor: String) {
              repository(owner: $owner, name: $repo) {
                defaultBranchRef {
                  target {
                    ... on Commit {
                      history(first: 100, since: $since, after: $cursor) {
                        pageInfo { hasNextPage endCursor }
                        nodes {
                          oid
                          messageHeadline
                          additions
                          deletions
                          author { date user { login } }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private final GithubClient githubClient;

    public List<Node> fetchAll(String token, String owner, String repo, String since) {
        List<Node> result = new ArrayList<>();
        String cursor = null;

        do {
            Map<String, Object> variables = new java.util.HashMap<>();
            variables.put("owner", owner);
            variables.put("repo", repo);
            variables.put("since", since);
            variables.put("cursor", cursor);

            GraphQLCommitStatsResponse response = githubClient.executeGraphQL(
                    token, new GraphQLRequest(QUERY, variables)
            );

            if (response == null || response.data() == null) break;

            var ref = response.data().repository();
            if (ref == null || ref.defaultBranchRef() == null) break;

            var history = ref.defaultBranchRef().target().history();
            if (history == null || history.nodes() == null || history.nodes().isEmpty()) break;

            result.addAll(history.nodes());
            log.debug("GraphQL commits: {} nodes acumulados (owner={}, repo={})", result.size(), owner, repo);

            var pageInfo = history.pageInfo();
            if (pageInfo == null || !pageInfo.hasNextPage()) break;
            cursor = pageInfo.endCursor();

        } while (true);

        return result;
    }
}
