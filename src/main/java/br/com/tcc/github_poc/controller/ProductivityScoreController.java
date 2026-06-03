package br.com.tcc.github_poc.controller;

import br.com.tcc.github_poc.dto.ProductivityGoalsDTO;
import br.com.tcc.github_poc.dto.ProductivityMetricsDTO;
import br.com.tcc.github_poc.dto.ProductivityMetricsProjection;
import br.com.tcc.github_poc.dto.ProductivityScoreResponseDTO;
import br.com.tcc.github_poc.metrics.service.ProductivityScoreService;
import br.com.tcc.github_poc.repositories.ProductivityScoreRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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

        ProductivityMetricsProjection projection =
                productivityScoreRepository.findProductivityMetricsByAuthorLogin(
                        authorLogin,
                        finalStartDate,
                        finalEndDate
                );

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

        ProductivityGoalsDTO goals = getDefaultMonthlyGoals();

        return productivityScoreService.calculate(metrics, goals);
    }

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
