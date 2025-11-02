package com.example.webdisgn.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME_MS = 60 * 1000;

    public boolean isBlocked(String ip) {
        Attempt attempt = attempts.get(ip);
        if (attempt == null) return false;
        if (attempt.count >= MAX_ATTEMPTS) {
            long timePassed = Instant.now().toEpochMilli() - attempt.lastAttempt;
            if (timePassed < BLOCK_TIME_MS) return true;
            else attempts.remove(ip);
        }
        return false;
    }

    public void recordAttempt(String ip) {
        Attempt attempt = attempts.getOrDefault(ip, new Attempt(0, 0));
        attempt.count++;
        attempt.lastAttempt = Instant.now().toEpochMilli();
        attempts.put(ip, attempt);
    }

    private static class Attempt {
        int count;
        long lastAttempt;
        public Attempt(int count, long lastAttempt) {
            this.count = count;
            this.lastAttempt = lastAttempt;
        }
    }
}
