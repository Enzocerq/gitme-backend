package br.com.tcc.github_poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepoDetailResponse(
        Long id,
        String name,
        String description,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("stargazers_count") int stargazersCount,
        OwnerInfo owner
) {}
