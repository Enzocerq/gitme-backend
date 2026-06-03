package br.com.tcc.github_poc.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "issue_labels", schema = "public")
public class IssueLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "issue_labels_id_seq_generator")
    @SequenceGenerator(
            name = "issue_labels_id_seq_generator",
            sequenceName = "public.issue_labels_id_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "description", columnDefinition = "text")
    private String description;

}