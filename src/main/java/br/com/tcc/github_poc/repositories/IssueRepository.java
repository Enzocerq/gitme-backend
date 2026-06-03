package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    @Query("SELECT i FROM Issue i")
    List<Issue> findAllIssues();

    @Query("SELECT i FROM Issue i WHERE i.id = :id")
    Optional<Issue> findIssueById(@Param("id") Long id);

    @Query("""
           SELECT i FROM Issue i
           WHERE i.repository.id = :repoId
           ORDER BY i.createdAt DESC
           """)
    List<Issue> findIssuesByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT i FROM Issue i
           WHERE i.repository.id = :repoId
           AND i.state = :state
           ORDER BY i.createdAt DESC
           """)
    List<Issue> findIssuesByRepositoryIdAndState(
            @Param("repoId") Long repoId,
            @Param("state") String state
    );

    @Query("""
           SELECT i FROM Issue i
           WHERE i.authorLogin = :authorLogin
           ORDER BY i.createdAt DESC
           """)
    List<Issue> findIssuesByAuthorLogin(@Param("authorLogin") String authorLogin);

    @Query("""
           SELECT COUNT(i) FROM Issue i
           WHERE i.repository.id = :repoId
           """)
    Long countIssuesByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COUNT(i) FROM Issue i
           WHERE i.repository.id = :repoId
           AND i.state = :state
           """)
    Long countIssuesByRepositoryIdAndState(
            @Param("repoId") Long repoId,
            @Param("state") String state
    );

    @Query("""
           SELECT i FROM Issue i
           WHERE LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))
           ORDER BY i.createdAt DESC
           """)
    List<Issue> searchIssuesByTitle(@Param("title") String title);

    // --- Lead Time (média de horas entre criação e fechamento) ---

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (closed_at - created_at)) / 3600.0)
           FROM issues
           WHERE repo_id = :repoId
           AND state = 'closed'
           AND created_at >= :from
           AND closed_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgLeadTimeHoursByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (closed_at - created_at)) / 3600.0)
           FROM issues
           WHERE repo_id = :repoId
           AND author_login = :login
           AND state = 'closed'
           AND created_at >= :from
           AND closed_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgLeadTimeHoursByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
