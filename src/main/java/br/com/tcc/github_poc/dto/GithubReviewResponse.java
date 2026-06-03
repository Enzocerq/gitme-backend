package br.com.tcc.github_poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubReviewResponse(
        Long id,
        OwnerInfo user,
        String state,
        @JsonProperty("submitted_at") String submittedAt
) {}