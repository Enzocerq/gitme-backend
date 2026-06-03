package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.PullRequest;
import br.com.tcc.github_poc.metrics.dto.common.DailyCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {

    @Query("SELECT p FROM PullRequest p")
    List<PullRequest> findAllPullRequests();

    @Query("SELECT p FROM PullRequest p WHERE p.id = :id")
    Optional<PullRequest> findPullRequestById(@Param("id") Long id);

    @Query("""
           SELECT p FROM PullRequest p
           WHERE p.repository.id = :repoId
           ORDER BY p.createdAt DESC
           """)
    List<PullRequest> findPullRequestsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT p FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.state = :state
           ORDER BY p.createdAt DESC
           """)
    List<PullRequest> findPullRequestsByRepositoryIdAndState(
            @Param("repoId") Long repoId,
            @Param("state") String state
    );

    @Query("""
           SELECT p FROM PullRequest p
           WHERE p.authorLogin = :authorLogin
           ORDER BY p.createdAt DESC
           """)
    List<PullRequest> findPullRequestsByAuthorLogin(@Param("authorLogin") String authorLogin);

    @Query("""
           SELECT p FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.mergedAt IS NOT NULL
           ORDER BY p.mergedAt DESC
           """)
    List<PullRequest> findMergedPullRequestsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           """)
    Long countPullRequestsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.state = :state
           """)
    Long countPullRequestsByRepositoryIdAndState(
            @Param("repoId") Long repoId,
            @Param("state") String state
    );

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.mergedAt IS NOT NULL
           """)
    Long countMergedPullRequestsByRepositoryId(@Param("repoId") Long repoId);

    // --- Métricas com filtro de período ---

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.createdAt BETWEEN :from AND :to
           """)
    Long countByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.authorLogin = :login
           AND p.createdAt BETWEEN :from AND :to
           """)
    Long countByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.mergedAt IS NOT NULL
           AND p.createdAt BETWEEN :from AND :to
           """)
    Long countMergedByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COUNT(p) FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.authorLogin = :login
           AND p.mergedAt IS NOT NULL
           AND p.createdAt BETWEEN :from AND :to
           """)
    Long countMergedByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT DATE(created_at) AS day, COUNT(*) AS cnt
           FROM pull_requests
           WHERE repo_id = :repoId
           AND created_at BETWEEN :from AND :to
           GROUP BY DATE(created_at)
           ORDER BY day
           """, nativeQuery = true)
    List<DailyCountProjection> countByDay(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT DATE(created_at) AS day, COUNT(*) AS cnt
           FROM pull_requests
           WHERE repo_id = :repoId
           AND author_login = :login
           AND created_at BETWEEN :from AND :to
           GROUP BY DATE(created_at)
           ORDER BY day
           """, nativeQuery = true)
    List<DailyCountProjection> countByDayAndAuthor(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT p FROM PullRequest p
           WHERE p.repository.id = :repoId
           AND p.authorLogin = :login
           ORDER BY p.createdAt DESC
           """)
    Page<PullRequest> findRecentByRepoAndAuthor(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            Pageable pageable
    );
}
