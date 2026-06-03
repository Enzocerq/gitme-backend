package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "reviews", schema = "public")
public class Review implements Persistable<Long> {

    @Id
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pull_request_id", nullable = false)
    private PullRequest pullRequest;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Transient
    private boolean newRecord = true;

    @Override
    public boolean isNew() { return newRecord; }

    @PostLoad
    @PostPersist
    void markNotNew() { this.newRecord = false; }
}
