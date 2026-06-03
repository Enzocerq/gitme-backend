package br.com.tcc.github_poc.exception;

public record ErrorResponse(int status, String error, String message, String path, String timestamp) {}
