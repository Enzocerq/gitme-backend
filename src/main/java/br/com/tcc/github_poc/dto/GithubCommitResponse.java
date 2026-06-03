package br.com.tcc.github_poc.dto;

public record GithubCommitResponse(
        String sha,
        CommitDetalhe commit,
        OwnerInfo author
) {}
