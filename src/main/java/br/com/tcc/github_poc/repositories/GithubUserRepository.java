package br.com.tcc.github_poc.repositories;

import br.com.tcc.github_poc.entities.GithubUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GithubUserRepository extends JpaRepository<GithubUser, String> {

    @Query("SELECT u FROM GithubUser u WHERE u.login = :login")
    Optional<GithubUser> findByLogin(@Param("login") String login);

    @Query("SELECT u FROM GithubUser u WHERE u.githubId = :githubId")
    Optional<GithubUser> findByGithubId(@Param("githubId") Long githubId);

    @Query("SELECT u FROM GithubUser u ORDER BY u.login ASC")
    List<GithubUser> findAllUsers();
}
