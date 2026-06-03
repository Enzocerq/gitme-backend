package br.com.tcc.github_poc.etl.mapper;

import br.com.tcc.github_poc.dto.*;
import br.com.tcc.github_poc.entities.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Component
public class DtoToEntityMapper {

    public GithubRepository toRepository(GithubRepoDetailResponse dto, User owner) {
        GithubRepository repo = new GithubRepository();
        repo.setId(dto.id());
        repo.setName(dto.name());
        repo.setDescription(dto.description());
        repo.setHtmlUrl(dto.htmlUrl());
        repo.setStargazersCount(dto.stargazersCount());
        repo.setUser(owner);
        return repo;
    }

    public GithubUser toGithubUser(GithubContributorResponse dto) {
        GithubUser user = new GithubUser();
        user.setLogin(dto.login());
        user.setGithubId(dto.id());
        user.setAvatarUrl(dto.avatarUrl());
        return user;
    }

    public GithubUser toGithubUser(String login) {
        GithubUser user = new GithubUser();
        user.setLogin(login);
        return user;
    }

    public RepositoryContributor toContributor(GithubContributorResponse dto, GithubRepository repository) {
        RepositoryContributor contributor = new RepositoryContributor();
        contributor.setId(dto.id());
        contributor.setLogin(dto.login());
        contributor.setAvatarUrl(dto.avatarUrl());
        contributor.setContributions(dto.contributions());
        contributor.setRepository(repository);
        return contributor;
    }

    public Commit toCommit(GraphQLCommitStatsResponse.Node node, GithubRepository repository) {
        Commit commit = new Commit();
        commit.setSha(node.oid());
        commit.setMessageHeadline(node.messageHeadline());
        commit.setAdditions(node.additions());
        commit.setDeletions(node.deletions());
        commit.setRepository(repository);

        if (node.author() != null) {
            commit.setCommitDate(parseDate(node.author().date()));
            if (node.author().user() != null) {
                commit.setAuthorLogin(node.author().user().login());
            }
        }
        return commit;
    }

    public PullRequest toPullRequest(GithubPullRequestResponse dto, GithubRepository repository) {
        PullRequest pr = new PullRequest();
        pr.setId(dto.id());
        pr.setNumber(dto.number());
        pr.setTitle(dto.title());
        pr.setState(dto.state());
        pr.setCreatedAt(parseDate(dto.createdAt()));
        pr.setMergedAt(parseDate(dto.mergedAt()));
        pr.setAuthorLogin(dto.user() != null ? dto.user().login() : null);
        pr.setRepository(repository);
        return pr;
    }

    public Issue toIssue(GithubIssueResponse dto, GithubRepository repository) {
        Issue issue = new Issue();
        issue.setId(dto.id());
        issue.setNumber(dto.number());
        issue.setTitle(dto.title());
        issue.setState(dto.state());
        issue.setCreatedAt(parseDate(dto.createdAt()));
        issue.setClosedAt(parseDate(dto.closedAt()));
        issue.setAuthorLogin(dto.user() != null ? dto.user().login() : null);
        issue.setRepository(repository);
        return issue;
    }

    public IssueAssignee toAssignee(OwnerInfo dto, Issue issue) {
        IssueAssignee assignee = new IssueAssignee();
        assignee.setLogin(dto.login());
        assignee.setIssue(issue);
        return assignee;
    }

    public IssueLabel toLabel(GithubIssueResponse.LabelInfo dto, Issue issue) {
        IssueLabel label = new IssueLabel();
        label.setName(dto.name());
        label.setColor(dto.color());
        label.setDescription(dto.description());
        label.setIssue(issue);
        return label;
    }

    public Review toReview(GithubReviewResponse dto, PullRequest pullRequest) {
        Review review = new Review();
        review.setId(dto.id());
        review.setState(dto.state());
        review.setSubmittedAt(parseDate(dto.submittedAt()));
        review.setAuthorLogin(dto.user() != null ? dto.user().login() : null);
        review.setPullRequest(pullRequest);
        return review;
    }

    public LocalDateTime parseDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return null;
        return OffsetDateTime.parse(isoDate).toLocalDateTime();
    }
}
