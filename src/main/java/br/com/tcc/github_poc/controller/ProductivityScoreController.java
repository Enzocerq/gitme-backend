package br.com.tcc.github_poc.controller;

import br.com.tcc.github_poc.dto.ProductivityGoalsDTO;
import br.com.tcc.github_poc.dto.ProductivityMetricsDTO;
import br.com.tcc.github_poc.dto.ProductivityMetricsProjection;
import br.com.tcc.github_poc.dto.ProductivityScoreResponseDTO;
import br.com.tcc.github_poc.dto.ScoreTrendPointDTO;
import br.com.tcc.github_poc.metrics.service.ProductivityScoreService;
import br.com.tcc.github_poc.repositories.ProductivityScoreRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/productivity-score")
public class ProductivityScoreController {

    private final ProductivityScoreRepository productivityScoreRepository;
    private final ProductivityScoreService productivityScoreService;

    public ProductivityScoreController(
            ProductivityScoreRepository productivityScoreRepository,
            ProductivityScoreService productivityScoreService
    ) {
        this.productivityScoreRepository = productivityScoreRepository;
        this.productivityScoreService = productivityScoreService;
    }

    @GetMapping("/{authorLogin}")
    public ProductivityScoreResponseDTO getProductivityScore(
            @PathVariable String authorLogin,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();
        LocalDateTime finalStartDate = startDate != null ? startDate : finalEndDate.minusDays(30);

        return scoreFor(authorLogin, finalStartDate, finalEndDate);
    }

    /**
     * Série temporal do Score de Produtividade ao longo do período selecionado.
     *
     * <p>Cada ponto é calculado sobre uma <strong>janela móvel de 30 dias</strong> encerrada na
     * data do ponto — o mesmo tamanho de janela em que as metas mensais são calibradas, o que
     * mantém os pontos comparáveis entre si (à semelhança de uma média móvel). A evolução do
     * indicador é mais informativa que o valor absoluto isolado.
     *
     * @param points quantidade de pontos uniformemente espaçados no intervalo (2–30, padrão 12).
     */
    @GetMapping("/{authorLogin}/trend")
    public List<ScoreTrendPointDTO> getProductivityScoreTrend(
            @PathVariable String authorLogin,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,

            @RequestParam(required = false, defaultValue = "12") int points
    ) {
        LocalDateTime finalEndDate = endDate != null ? endDate : LocalDateTime.now();
        LocalDateTime finalStartDate = startDate != null ? startDate : finalEndDate.minusDays(30);

        int n = Math.max(2, Math.min(points, 30));
        long totalSeconds = Duration.between(finalStartDate, finalEndDate).getSeconds();
        if (totalSeconds <= 0) {
            return List.of(new ScoreTrendPointDTO(
                    finalEndDate.toLocalDate(),
                    scoreFor(authorLogin, finalEndDate.minusDays(ROLLING_WINDOW_DAYS), finalEndDate).scoreFinal()
            ));
        }

        List<ScoreTrendPointDTO> trend = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            LocalDateTime pointEnd = finalStartDate.plusSeconds(Math.round((double) totalSeconds * i / (n - 1)));
            LocalDateTime pointStart = pointEnd.minusDays(ROLLING_WINDOW_DAYS);
            double score = scoreFor(authorLogin, pointStart, pointEnd).scoreFinal();
            trend.add(new ScoreTrendPointDTO(pointEnd.toLocalDate(), score));
        }
        return trend;
    }

    /** Calcula o score para uma janela [start, end] reaproveitando projeção + serviço. */
    private ProductivityScoreResponseDTO scoreFor(String authorLogin, LocalDateTime start, LocalDateTime end) {
        ProductivityMetricsProjection projection =
                productivityScoreRepository.findProductivityMetricsByAuthorLogin(authorLogin, start, end);

        if (projection == null) {
            throw new RuntimeException("Usuário não encontrado ou sem métricas disponíveis.");
        }

        ProductivityMetricsDTO metrics = new ProductivityMetricsDTO(
                safeInteger(projection.getCommits()),
                safeInteger(projection.getPrsCriados()),
                safeInteger(projection.getPrsMergeados()),
                safeDouble(projection.getCycleTimeMedio()),
                safeInteger(projection.getReviewsRealizadas()),
                safeInteger(projection.getDiasAtivos())
        );

        return productivityScoreService.calculate(metrics, getDefaultMonthlyGoals());
    }

    private static final int ROLLING_WINDOW_DAYS = 30;

    private ProductivityGoalsDTO getDefaultMonthlyGoals() {
        return new ProductivityGoalsDTO(
                50.0,
                3.0,
                0.80,
                8.0,
                20.0
        );
    }

    private int safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }
}
