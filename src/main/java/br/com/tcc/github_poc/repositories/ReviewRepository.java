package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.Review;
import br.com.tcc.github_poc.metrics.dto.common.DailyDurationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r WHERE r.pullRequest.id = :pullRequestId ORDER BY r.submittedAt ASC")
    List<Review> findByPullRequestId(@Param("pullRequestId") Long pullRequestId);

    @Query("SELECT r FROM Review r WHERE r.pullRequest.id = :pullRequestId ORDER BY r.submittedAt ASC LIMIT 1")
    Optional<Review> findFirstReviewByPullRequestId(@Param("pullRequestId") Long pullRequestId);

    @Query("SELECT r FROM Review r WHERE r.authorLogin = :login ORDER BY r.submittedAt DESC")
    List<Review> findByAuthorLogin(@Param("login") String login);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.pullRequest.id = :pullRequestId")
    long countByPullRequestId(@Param("pullRequestId") Long pullRequestId);

    // --- Time in Review (média de horas entre criação da PR e primeira review) ---

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (fr.min_submitted - p.created_at)) / 3600.0)
           FROM pull_requests p
           JOIN (
               SELECT pull_request_id, MIN(submitted_at) AS min_submitted
               FROM reviews
               GROUP BY pull_request_id
           ) fr ON fr.pull_request_id = p.id
           WHERE p.repo_id = :repoId
           AND p.created_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgTimeInReviewHoursByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (fr.min_submitted - p.created_at)) / 3600.0)
           FROM pull_requests p
           JOIN (
               SELECT pull_request_id, MIN(submitted_at) AS min_submitted
               FROM reviews
               GROUP BY pull_request_id
           ) fr ON fr.pull_request_id = p.id
           WHERE p.repo_id = :repoId
           AND p.author_login = :login
           AND p.created_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgTimeInReviewHoursByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT DATE(p.created_at) AS day,
                  AVG(EXTRACT(EPOCH FROM (fr.min_submitted - p.created_at)) / 3600.0) AS avg_hours
           FROM pull_requests p
           JOIN (
               SELECT pull_request_id, MIN(submitted_at) AS min_submitted
               FROM reviews
               GROUP BY pull_request_id
           ) fr ON fr.pull_request_id = p.id
           WHERE p.repo_id = :repoId
           AND p.created_at BETWEEN :from AND :to
           GROUP BY DATE(p.created_at)
           ORDER BY day
           """, nativeQuery = true)
    List<DailyDurationProjection> avgTimeInReviewByDay(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // --- Distribuição de revisões por autor ---

    @Query(value = """
           SELECT r.author_login AS login, COUNT(*) AS reviews
           FROM reviews r
           JOIN pull_requests p ON r.pull_request_id = p.id
           WHERE p.repo_id = :repoId
           AND r.submitted_at BETWEEN :from AND :to
           AND r.author_login IS NOT NULL
           GROUP BY r.author_login
           ORDER BY reviews DESC
           LIMIT :lim
           """, nativeQuery = true)
    List<Object[]> countReviewsByAuthorGrouped(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("lim") int lim
    );

    // --- Contribuidores ativos (distinct author_logins em commits no período) ---

    @Query(value = """
           SELECT COUNT(DISTINCT r.author_login)
           FROM reviews r
           JOIN pull_requests p ON r.pull_request_id = p.id
           WHERE p.repo_id = :repoId
           AND r.submitted_at BETWEEN :from AND :to
           AND r.author_login IS NOT NULL
           """, nativeQuery = true)
    Long countActiveReviewersByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
