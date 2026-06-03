package br.com.tcc.github_poc.etl;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SeedJobState {

    public enum Status { IDLE, RUNNING, DONE, ERROR }

    private final AtomicReference<Status> status = new AtomicReference<>(Status.IDLE);
    private final AtomicReference<String> currentRepo = new AtomicReference<>("");
    private final AtomicReference<String> errorMessage = new AtomicReference<>("");
    private final AtomicReference<LocalDateTime> startedAt = new AtomicReference<>();
    private final AtomicReference<LocalDateTime> finishedAt = new AtomicReference<>();
    private final AtomicInteger commitsIngested = new AtomicInteger();
    private final AtomicInteger pullRequestsIngested = new AtomicInteger();
    private final AtomicInteger issuesIngested = new AtomicInteger();
    private final AtomicInteger reviewsIngested = new AtomicInteger();
    private final AtomicInteger prCommitsIngested = new AtomicInteger();

    public void start(String firstRepo) {
        status.set(Status.RUNNING);
        currentRepo.set(firstRepo);
        errorMessage.set("");
        startedAt.set(LocalDateTime.now());
        finishedAt.set(null);
        commitsIngested.set(0);
        pullRequestsIngested.set(0);
        issuesIngested.set(0);
        reviewsIngested.set(0);
        prCommitsIngested.set(0);
    }

    public void markCurrentRepo(String repo) { currentRepo.set(repo); }
    public void markDone() { status.set(Status.DONE); finishedAt.set(LocalDateTime.now()); }
    public void markError(String msg) { status.set(Status.ERROR); errorMessage.set(msg); finishedAt.set(LocalDateTime.now()); }
    public boolean isRunning() { return status.get() == Status.RUNNING; }

    public void addCommits(int n) { commitsIngested.addAndGet(n); }
    public void addPullRequests(int n) { pullRequestsIngested.addAndGet(n); }
    public void addIssues(int n) { issuesIngested.addAndGet(n); }
    public void addReviews(int n) { reviewsIngested.addAndGet(n); }
    public void addPrCommits(int n) { prCommitsIngested.addAndGet(n); }

    public Snapshot snapshot() {
        return new Snapshot(
                status.get().name(),
                currentRepo.get(),
                errorMessage.get(),
                startedAt.get(),
                finishedAt.get(),
                commitsIngested.get(),
                pullRequestsIngested.get(),
                issuesIngested.get(),
                reviewsIngested.get(),
                prCommitsIngested.get()
        );
    }

    public record Snapshot(
            String status,
            String currentRepo,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            int commitsIngested,
            int pullRequestsIngested,
            int issuesIngested,
            int reviewsIngested,
            int prCommitsIngested
    ) {}
}
