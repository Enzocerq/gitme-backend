package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.dto.ProductivityMetricsProjection;
import br.com.tcc.github_poc.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProductivityScoreRepository extends JpaRepository<User, Integer> {

    @Query(value = """
        SELECT
            COALESCE((
                SELECT COUNT(*)
                FROM commits c
                WHERE c.author_login = :authorLogin
                  AND c.commit_date BETWEEN :startDate AND :endDate
            ), 0) AS commits,

            COALESCE((
                SELECT COUNT(*)
                FROM pull_requests pr
                WHERE pr.author_login = :authorLogin
                  AND pr.created_at BETWEEN :startDate AND :endDate
            ), 0) AS prsCriados,

            COALESCE((
                SELECT COUNT(*)
                FROM pull_requests pr
                WHERE pr.author_login = :authorLogin
                  AND pr.merged_at IS NOT NULL
                  AND pr.merged_at BETWEEN :startDate AND :endDate
            ), 0) AS prsMergeados,

            COALESCE((
                SELECT AVG(EXTRACT(EPOCH FROM (pr.merged_at - pr.created_at)) / 86400.0)
                FROM pull_requests pr
                WHERE pr.author_login = :authorLogin
                  AND pr.merged_at IS NOT NULL
                  AND pr.created_at IS NOT NULL
                  AND pr.merged_at BETWEEN :startDate AND :endDate
            ), 0) AS cycleTimeMedio,

            COALESCE((
                SELECT COUNT(*)
                FROM reviews r
                WHERE r.author_login = :authorLogin
                  AND r.submitted_at BETWEEN :startDate AND :endDate
            ), 0) AS reviewsRealizadas,

            COALESCE((
                SELECT COUNT(DISTINCT activity_day)
                FROM (
                    SELECT DATE(c.commit_date) AS activity_day
                    FROM commits c
                    WHERE c.author_login = :authorLogin
                      AND c.commit_date BETWEEN :startDate AND :endDate

                    UNION

                    SELECT DATE(pr.created_at) AS activity_day
                    FROM pull_requests pr
                    WHERE pr.author_login = :authorLogin
                      AND pr.created_at BETWEEN :startDate AND :endDate

                    UNION

                    SELECT DATE(r.submitted_at) AS activity_day
                    FROM reviews r
                    WHERE r.author_login = :authorLogin
                      AND r.submitted_at BETWEEN :startDate AND :endDate
                ) activity
            ), 0) AS diasAtivos

        FROM (SELECT 1) dummy
        """, nativeQuery = true)
    ProductivityMetricsProjection findProductivityMetricsByAuthorLogin(
            @Param("authorLogin") String authorLogin,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
