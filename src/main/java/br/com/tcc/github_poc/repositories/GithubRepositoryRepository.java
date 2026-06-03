package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {

    @Query("SELECT r FROM GithubRepository r")
    List<GithubRepository> findAllRepositories();

    @Query("SELECT r FROM GithubRepository r WHERE r.id = :id")
    Optional<GithubRepository> findRepositoryById(@Param("id") Long id);

    @Query("SELECT r FROM GithubRepository r WHERE r.user.id = :userId")
    List<GithubRepository> findRepositoriesByUserId(@Param("userId") Integer userId);

    @Query("""
           SELECT r FROM GithubRepository r
           WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))
           """)
    List<GithubRepository> findRepositoriesByName(@Param("name") String name);

    @Query("""
           SELECT r FROM GithubRepository r
           WHERE r.user.githubUsername = :githubUsername
           """)
    List<GithubRepository> findRepositoriesByGithubUsername(@Param("githubUsername") String githubUsername);

    @Query("""
           SELECT r FROM GithubRepository r
           ORDER BY r.stargazersCount DESC
           """)
    List<GithubRepository> findRepositoriesOrderByStarsDesc();

    @Query("""
           SELECT DISTINCT c.repository FROM Commit c
           WHERE c.authorLogin = :login
           AND c.commitDate BETWEEN :from AND :to
           """)
    List<GithubRepository> findReposByAuthorLoginAndPeriod(
            @Param("login") String login,
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to
    );
}