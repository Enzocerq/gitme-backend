package br.com.tcc.github_poc.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PullRequestCommitId implements Serializable {

    @Column(name = "pr_id")
    private Long prId;

    @Column(name = "commit_sha")
    private String commitSha;
}
