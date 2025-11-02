package com.example.webdisgn.util;

import io.github.bucket4j.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> newBucket());
    }

    private Bucket newBucket() {
        Refill refill = Refill.greedy(3, Duration.ofMinutes(10));
        Bandwidth limit = Bandwidth.classic(3, refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
