package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "issue_assignees", schema = "public")
public class IssueAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "issue_assignees_id_seq_generator")
    @SequenceGenerator(
            name = "issue_assignees_id_seq_generator",
            sequenceName = "public.issue_assignees_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "html_url")
    private String htmlUrl;
}