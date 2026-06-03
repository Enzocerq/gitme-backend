package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GithubRepoDetailResponse;
import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.entities.User;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.GithubRepositoryRepository;
import br.com.tcc.github_poc.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class RepositoryIngestionService {

    private final GithubClient githubClient;
    private final GithubRepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final DtoToEntityMapper mapper;

    @Transactional
    public GithubRepository ingest(String token, String owner, String repo) {
        log.info("Ingerindo repositório {}/{}", owner, repo);

        GithubRepoDetailResponse dto = githubClient.getRepo(token, owner, repo);

        User user = userRepository.findUserByGithubUsername(owner)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(owner);
                    newUser.setGithubUsername(owner);
                    newUser.setEmail(owner + "@github.seed");
                    return userRepository.save(newUser);
                });

        GithubRepository entity = mapper.toRepository(dto, user);
        GithubRepository saved = repositoryRepository.save(entity);
        log.info("Repositório {} persistido (id={})", saved.getName(), saved.getId());
        return saved;
    }
}
