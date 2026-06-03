package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.IssueLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueLabelRepository extends JpaRepository<IssueLabel, Long> {

    @Query("SELECT l FROM IssueLabel l")
    List<IssueLabel> findAllIssueLabels();

    @Query("SELECT l FROM IssueLabel l WHERE l.id = :id")
    Optional<IssueLabel> findIssueLabelById(@Param("id") Long id);

    @Query("""
           SELECT l FROM IssueLabel l
           WHERE l.issue.id = :issueId
           """)
    List<IssueLabel> findLabelsByIssueId(@Param("issueId") Long issueId);

    @Query("""
           SELECT l FROM IssueLabel l
           WHERE LOWER(l.name) = LOWER(:name)
           """)
    List<IssueLabel> findLabelsByName(@Param("name") String name);

    @Query("""
           SELECT COUNT(l) FROM IssueLabel l
           WHERE l.issue.id = :issueId
           """)
    Long countLabelsByIssueId(@Param("issueId") Long issueId);
}