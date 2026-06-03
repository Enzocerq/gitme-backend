package br.com.tcc.github_poc.etl.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
public class RateLimitGuard {

    @Value("${etl.seed.rate-limit.threshold:100}")
    private int threshold;

    public void checkAndSleepIfNeeded(HttpHeaders headers) {
        List<String> remaining = headers.get("X-RateLimit-Remaining");
        List<String> reset = headers.get("X-RateLimit-Reset");

        if (remaining == null || remaining.isEmpty()) return;

        int remainingCount = Integer.parseInt(remaining.get(0));
        if (remainingCount >= threshold) return;

        if (reset == null || reset.isEmpty()) {
            sleepSeconds(60);
            return;
        }

        long resetEpoch = Long.parseLong(reset.get(0));
        long nowEpoch = Instant.now().getEpochSecond();
        long waitSeconds = Math.max(resetEpoch - nowEpoch + 5, 0);

        log.warn("Rate limit baixo: {} restantes. Aguardando {}s até reset.", remainingCount, waitSeconds);
        sleepSeconds(waitSeconds);
    }

    private void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
