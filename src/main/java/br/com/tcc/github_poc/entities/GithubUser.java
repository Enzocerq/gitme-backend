package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

@Data
@EqualsAndHashCode(of = "login")
@Entity
@Table(name = "github_users", schema = "public")
public class GithubUser implements Persistable<String> {

    @Id
    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "github_id")
    private Long githubId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "html_url")
    private String htmlUrl;

    @Transient
    private boolean newRecord = true;

    @Override
    public String getId() { return login; }

    @Override
    public boolean isNew() { return newRecord; }

    @PostLoad
    @PostPersist
    void markNotNew() { this.newRecord = false; }
}
