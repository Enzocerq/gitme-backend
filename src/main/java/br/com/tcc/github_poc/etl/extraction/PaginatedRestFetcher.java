package br.com.tcc.github_poc.etl.extraction;

import br.com.tcc.github_poc.etl.ratelimit.RateLimitGuard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
@Component
public class PaginatedRestFetcher {

    private static final String BASE_URL = "https://api.github.com";
    private static final int PER_PAGE = 100;

    private final RestClient restClient;
    private final RateLimitGuard rateLimitGuard;

    public PaginatedRestFetcher(RestClient.Builder builder, RateLimitGuard rateLimitGuard) {
        this.restClient = builder.baseUrl(BASE_URL).build();
        this.rateLimitGuard = rateLimitGuard;
    }

    /**
     * Pagina até o servidor retornar lista vazia ou o stopWhen retornar true para o último item da página.
     * stopWhen: recebe o último item da página — retorne true para interromper a paginação (cutoff de data).
     */
    public <T> List<T> fetchAll(String token,
                                String path,
                                Map<String, String> extraParams,
                                ParameterizedTypeReference<List<T>> type,
                                Predicate<T> stopWhen) {
        List<T> result = new ArrayList<>();
        int page = 1;

        while (true) {
            final int currentPage = page;

            ResponseEntity<List<T>> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(path)
                                .queryParam("per_page", PER_PAGE)
                                .queryParam("page", currentPage);
                        extraParams.forEach(builder::queryParam);
                        return builder.build();
                    })
                    .header("Authorization", token)
                    .header("Accept", "application/vnd.github+json")
                    .retrieve()
                    .toEntity(type);

            rateLimitGuard.checkAndSleepIfNeeded(response.getHeaders());

            List<T> pageItems = response.getBody();
            if (pageItems == null || pageItems.isEmpty()) break;

            result.addAll(pageItems);

            T last = pageItems.get(pageItems.size() - 1);
            if (stopWhen != null && stopWhen.test(last)) break;

            if (pageItems.size() < PER_PAGE) break;

            log.debug("Buscando página {} de {}", page + 1, path);
            page++;
        }

        return result;
    }

    public <T> List<T> fetchAll(String token, String path,
                                Map<String, String> extraParams,
                                ParameterizedTypeReference<List<T>> type) {
        return fetchAll(token, path, extraParams, type, null);
    }
}
