package br.com.tcc.github_poc.dto;

import java.util.List;

public record GraphQLCommitStatsResponse(Data data) {
    public record Data(Repository repository) {}
    public record Repository(DefaultBranchRef defaultBranchRef) {}
    public record DefaultBranchRef(Target target) {}
    public record Target(History history) {}
    public record History(PageInfo pageInfo, List<Node> nodes) {}

    public record PageInfo(boolean hasNextPage, String endCursor) {}

    public record Node(
            String oid,
            String messageHeadline,
            int additions,
            int deletions,
            Author author
    ) {}

    public record Author(String date, User user) {}
    public record User(String login) {}
}