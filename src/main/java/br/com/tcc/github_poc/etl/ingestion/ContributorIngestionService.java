package br.com.tcc.github_poc.etl.ingestion;

import br.com.tcc.github_poc.client.GithubClient;
import br.com.tcc.github_poc.dto.GithubContributorResponse;
import br.com.tcc.github_poc.entities.GithubRepository;
import br.com.tcc.github_poc.entities.GithubUser;
import br.com.tcc.github_poc.entities.RepositoryContributor;
import br.com.tcc.github_poc.etl.mapper.DtoToEntityMapper;
import br.com.tcc.github_poc.repositories.GithubUserRepository;
import br.com.tcc.github_poc.repositories.RepositoryContributorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContributorIngestionService {

    private final GithubClient githubClient;
    private final GithubUserRepository githubUserRepository;
    private final RepositoryContributorRepository contributorRepository;
    private final DtoToEntityMapper mapper;

    @Transactional
    public void ingest(String token, String owner, String repo, GithubRepository repository) {
        log.info("Ingerindo contribuidores de {}/{}", owner, repo);

        int page = 1;
        int total = 0;

        while (true) {
            List<GithubContributorResponse> pageItems = githubClient.getContributors(token, owner, repo, page, 100);
            if (pageItems == null || pageItems.isEmpty()) break;

            List<GithubUser> users = new ArrayList<>();
            List<RepositoryContributor> contributors = new ArrayList<>();

            for (GithubContributorResponse dto : pageItems) {
                users.add(mapper.toGithubUser(dto));
                contributors.add(mapper.toContributor(dto, repository));
            }

            // 1 SELECT em lote para GithubUser; só persiste os novos
            Set<String> existingLogins = githubUserRepository.findAllById(
                    users.stream().map(GithubUser::getLogin).toList()
            ).stream().map(GithubUser::getLogin).collect(Collectors.toSet());
            List<GithubUser> newUsers = users.stream()
                    .filter(u -> !existingLogins.contains(u.getLogin()))
                    .toList();
            if (!newUsers.isEmpty()) githubUserRepository.saveAll(newUsers);

            // 1 SELECT em lote para RepositoryContributor
            Set<Long> existingContribIds = contributorRepository.findAllById(
                    contributors.stream().map(RepositoryContributor::getId).toList()
            ).stream().map(RepositoryContributor::getId).collect(Collectors.toSet());
            List<RepositoryContributor> newContribs = contributors.stream()
                    .filter(c -> !existingContribIds.contains(c.getId()))
                    .toList();
            if (!newContribs.isEmpty()) contributorRepository.saveAll(newContribs);

            total += pageItems.size();
            if (pageItems.size() < 100) break;
            page++;
        }

        log.info("Contribuidores persistidos: {} (repo={})", total, repo);
    }
}
