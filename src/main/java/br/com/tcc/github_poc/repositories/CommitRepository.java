package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.Commit;
import br.com.tcc.github_poc.metrics.dto.common.DailyCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommitRepository extends JpaRepository<Commit, String> {

    @Query("SELECT c FROM Commit c")
    List<Commit> findAllCommits();

    @Query("SELECT c FROM Commit c WHERE c.sha = :sha")
    Optional<Commit> findCommitBySha(@Param("sha") String sha);

    @Query("""
           SELECT c FROM Commit c
           WHERE c.repository.id = :repoId
           ORDER BY c.commitDate DESC
           """)
    List<Commit> findCommitsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT c FROM Commit c
           WHERE c.authorLogin = :authorLogin
           ORDER BY c.commitDate DESC
           """)
    List<Commit> findCommitsByAuthorLogin(@Param("authorLogin") String authorLogin);

    @Query("""
           SELECT c FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.authorLogin = :authorLogin
           ORDER BY c.commitDate DESC
           """)
    List<Commit> findCommitsByRepositoryIdAndAuthorLogin(
            @Param("repoId") Long repoId,
            @Param("authorLogin") String authorLogin
    );

    @Query("""
           SELECT COUNT(c) FROM Commit c
           WHERE c.repository.id = :repoId
           """)
    Long countCommitsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COALESCE(SUM(c.additions), 0) FROM Commit c
           WHERE c.repository.id = :repoId
           """)
    Long sumAdditionsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COALESCE(SUM(c.deletions), 0) FROM Commit c
           WHERE c.repository.id = :repoId
           """)
    Long sumDeletionsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COALESCE(SUM(c.totalChanges), 0) FROM Commit c
           WHERE c.repository.id = :repoId
           """)
    Long sumTotalChangesByRepositoryId(@Param("repoId") Long repoId);

    // --- Métricas com filtro de período ---

    @Query("""
           SELECT COUNT(c) FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.commitDate BETWEEN :from AND :to
           """)
    Long countByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COUNT(c) FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.authorLogin = :login
           AND c.commitDate BETWEEN :from AND :to
           """)
    Long countByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COALESCE(SUM(c.totalChanges), 0) FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.commitDate BETWEEN :from AND :to
           """)
    Long sumTotalChangesByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT COALESCE(SUM(c.totalChanges), 0) FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.authorLogin = :login
           AND c.commitDate BETWEEN :from AND :to
           """)
    Long sumTotalChangesByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT COUNT(DISTINCT DATE(commit_date))
           FROM commits
           WHERE repo_id = :repoId
           AND author_login = :login
           AND commit_date BETWEEN :from AND :to
           """, nativeQuery = true)
    Long countDistinctActiveDays(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT DATE(commit_date) AS day, COUNT(*) AS cnt
           FROM commits
           WHERE repo_id = :repoId
           AND commit_date BETWEEN :from AND :to
           GROUP BY DATE(commit_date)
           ORDER BY day
           """, nativeQuery = true)
    List<DailyCountProjection> countByDay(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT DATE(commit_date) AS day, COUNT(*) AS cnt
           FROM commits
           WHERE repo_id = :repoId
           AND author_login = :login
           AND commit_date BETWEEN :from AND :to
           GROUP BY DATE(commit_date)
           ORDER BY day
           """, nativeQuery = true)
    List<DailyCountProjection> countByDayAndAuthor(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
           SELECT c FROM Commit c
           WHERE c.repository.id = :repoId
           AND c.authorLogin = :login
           ORDER BY c.commitDate DESC
           """)
    Page<Commit> findRecentByRepoAndAuthor(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            Pageable pageable
    );

    @Query("""
           SELECT DISTINCT c.repository FROM Commit c
           WHERE c.authorLogin = :login
           AND c.commitDate BETWEEN :from AND :to
           """)
    List<br.com.tcc.github_poc.entities.GithubRepository> findReposByAuthorAndPeriod(
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT author_login FROM commits
           WHERE repo_id IN :repoIds
           GROUP BY author_login
           ORDER BY COUNT(*) DESC
           LIMIT 1
           """, nativeQuery = true)
    String findTopContributorByRepoIds(@Param("repoIds") List<Long> repoIds);
}
