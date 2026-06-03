package br.com.tcc.github_poc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = "reviews")
@Entity
@Table(name = "pull_requests", schema = "public")
public class PullRequest implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repo_id", nullable = false)
    private GithubRepository repository;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @Column(name = "author_login")
    private String authorLogin;

    @JsonIgnore
    @OneToMany(mappedBy = "pullRequest", fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @Transient
    private boolean newRecord = true;

    @Override
    public boolean isNew() { return newRecord; }

    @PostLoad
    @PostPersist
    void markNotNew() { this.newRecord = false; }
}
