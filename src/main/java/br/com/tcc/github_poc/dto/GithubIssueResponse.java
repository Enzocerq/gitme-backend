package br.com.tcc.github_poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GithubIssueResponse(
        Long id,
        int number,
        String title,
        String state,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("closed_at") String closedAt,
        OwnerInfo user,
        List<LabelInfo> labels,
        List<OwnerInfo> assignees,
        @JsonProperty("pull_request") Object pullRequest
) {
    public record LabelInfo(String name, String color, String description) {}
}