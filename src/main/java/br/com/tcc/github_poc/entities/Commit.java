package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "sha")
@Entity
@Table(name = "commits", schema = "public")
public class Commit implements Persistable<String> {

    @Id
    @Column(name = "sha", nullable = false)
    private String sha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private GithubRepository repository;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "author_email")
    private String authorEmail;

    @Column(name = "commit_date")
    private LocalDateTime commitDate;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(name = "message_headline", columnDefinition = "text")
    private String messageHeadline;

    @Column(name = "additions")
    private Integer additions = 0;

    @Column(name = "deletions")
    private Integer deletions = 0;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "total_changes", insertable = false, updatable = false)
    private Integer totalChanges;

    @Transient
    private boolean newRecord = true;

    @Override
    public String getId() { return sha; }

    @Override
    public boolean isNew() { return newRecord; }

    @PostLoad
    @PostPersist
    void markNotNew() { this.newRecord = false; }
}
