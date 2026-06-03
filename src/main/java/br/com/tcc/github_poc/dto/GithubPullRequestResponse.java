package br.com.tcc.github_poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubPullRequestResponse(
        Long id,
        int number,
        String title,
        String state,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("merged_at") String mergedAt,
        OwnerInfo user
) {}