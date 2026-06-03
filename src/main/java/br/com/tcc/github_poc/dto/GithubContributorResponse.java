package br.com.tcc.github_poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubContributorResponse(
        Long id,
        String login,
        @JsonProperty("avatar_url") String avatarUrl,
        int contributions
) {}