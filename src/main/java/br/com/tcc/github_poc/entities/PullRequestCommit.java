package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pull_request_commits", schema = "public")
public class PullRequestCommit implements Persistable<PullRequestCommitId> {

    @EmbeddedId
    private PullRequestCommitId id;

    @Transient
    private boolean newRecord = true;

    public PullRequestCommit(Long prId, String commitSha) {
        this.id = new PullRequestCommitId(prId, commitSha);
    }

    @Override
    public PullRequestCommitId getId() { return id; }

    @Override
    public boolean isNew() { return newRecord; }

    @PostLoad
    @PostPersist
    void markNotNew() { this.newRecord = false; }
}
