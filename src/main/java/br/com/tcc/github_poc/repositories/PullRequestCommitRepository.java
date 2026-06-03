package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.PullRequestCommit;
import br.com.tcc.github_poc.entities.PullRequestCommitId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PullRequestCommitRepository extends JpaRepository<PullRequestCommit, PullRequestCommitId> {

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (p.merged_at - GREATEST(c_min.min_date, p.created_at))) / 3600.0)
           FROM pull_requests p
           JOIN (
               SELECT prc.pr_id, MIN(c.commit_date) AS min_date
               FROM pull_request_commits prc
               JOIN commits c ON c.sha = prc.commit_sha
               GROUP BY prc.pr_id
           ) c_min ON c_min.pr_id = p.id
           WHERE p.repo_id = :repoId
           AND p.merged_at IS NOT NULL
           AND p.merged_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgCycleTimeHoursByRepoPeriod(
            @Param("repoId") Long repoId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query(value = """
           SELECT AVG(EXTRACT(EPOCH FROM (p.merged_at - GREATEST(c_min.min_date, p.created_at))) / 3600.0)
           FROM pull_requests p
           JOIN (
               SELECT prc.pr_id, MIN(c.commit_date) AS min_date
               FROM pull_request_commits prc
               JOIN commits c ON c.sha = prc.commit_sha
               GROUP BY prc.pr_id
           ) c_min ON c_min.pr_id = p.id
           WHERE p.repo_id = :repoId
           AND p.author_login = :login
           AND p.merged_at IS NOT NULL
           AND p.merged_at BETWEEN :from AND :to
           """, nativeQuery = true)
    Double avgCycleTimeHoursByRepoAuthorPeriod(
            @Param("repoId") Long repoId,
            @Param("login") String login,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
