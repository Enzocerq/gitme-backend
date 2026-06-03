package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.RepositoryContributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;



public interface RepositoryContributorRepository extends JpaRepository<RepositoryContributor, Long> {

    @Query("SELECT c FROM RepositoryContributor c")
    List<RepositoryContributor> findAllContributors();

    @Query("SELECT c FROM RepositoryContributor c WHERE c.id = :id")
    Optional<RepositoryContributor> findContributorById(@Param("id") Long id);

    @Query("""
           SELECT c FROM RepositoryContributor c
           WHERE c.repository.id = :repoId
           ORDER BY c.contributions DESC
           """)
    List<RepositoryContributor> findContributorsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT c FROM RepositoryContributor c
           WHERE c.login = :login
           """)
    List<RepositoryContributor> findContributorsByLogin(@Param("login") String login);

    @Query("""
           SELECT c FROM RepositoryContributor c
           WHERE c.repository.id = :repoId
           AND c.login = :login
           """)
    Optional<RepositoryContributor> findContributorByRepositoryIdAndLogin(
            @Param("repoId") Long repoId,
            @Param("login") String login
    );

    @Query("""
           SELECT COUNT(c) FROM RepositoryContributor c
           WHERE c.repository.id = :repoId
           """)
    Long countContributorsByRepositoryId(@Param("repoId") Long repoId);

    @Query("""
           SELECT COALESCE(SUM(c.contributions), 0) FROM RepositoryContributor c
           WHERE c.repository.id = :repoId
           """)
    Long sumContributionsByRepositoryId(@Param("repoId") Long repoId);
}