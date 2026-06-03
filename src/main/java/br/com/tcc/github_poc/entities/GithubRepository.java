package br.com.tcc.github_poc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "repositories", schema = "public")
public class GithubRepository {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "html_url")
    private String htmlUrl;

    @Column(name = "stargazers_count")
    private Integer stargazersCount = 0;

    @JsonIgnore
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private List<Commit> commits = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private List<Issue> issues = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private List<PullRequest> pullRequests = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "repository", fetch = FetchType.LAZY)
    private List<RepositoryContributor> contributors = new ArrayList<>();

}