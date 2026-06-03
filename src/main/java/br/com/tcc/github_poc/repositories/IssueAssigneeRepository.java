package br.com.tcc.github_poc.repositories;


import br.com.tcc.github_poc.entities.IssueAssignee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IssueAssigneeRepository extends JpaRepository<IssueAssignee, Long> {

    @Query("SELECT a FROM IssueAssignee a")
    List<IssueAssignee> findAllIssueAssignees();

    @Query("SELECT a FROM IssueAssignee a WHERE a.id = :id")
    Optional<IssueAssignee> findIssueAssigneeById(@Param("id") Long id);

    @Query("""
           SELECT a FROM IssueAssignee a
           WHERE a.issue.id = :issueId
           """)
    List<IssueAssignee> findAssigneesByIssueId(@Param("issueId") Long issueId);

    @Query("""
           SELECT a FROM IssueAssignee a
           WHERE a.login = :login
           """)
    List<IssueAssignee> findAssigneesByLogin(@Param("login") String login);

    @Query("""
           SELECT COUNT(a) FROM IssueAssignee a
           WHERE a.issue.id = :issueId
           """)
    Long countAssigneesByIssueId(@Param("issueId") Long issueId);
}