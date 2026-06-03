package br.com.tcc.github_poc.dto;

public record CommitDetalhe(
        AutorInfo author,
        String message
) {}