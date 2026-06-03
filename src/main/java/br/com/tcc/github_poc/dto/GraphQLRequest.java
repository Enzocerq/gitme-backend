package br.com.tcc.github_poc.dto;

import java.util.Map;

public record GraphQLRequest(String query, Map<String, Object> variables) {

    public GraphQLRequest(String query) {
        this(query, Map.of());
    }
}