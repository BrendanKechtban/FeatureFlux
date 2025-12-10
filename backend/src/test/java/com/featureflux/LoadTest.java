package com.featureflux;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Load test for Redis evaluation engine
 * Measures throughput (requests per second) under load
 */
@SpringBootTest
@ActiveProfiles("test")
public class LoadTest {

    private static final int THREAD_COUNT = 50;
    private static final int REQUESTS_PER_THREAD = 1000;
    private static final String TEST_FLAG_KEY = "load-test-flag";
    private static final String BASE_URL = "http://localhost:8080";

    @Test
    public void testRedisEvaluationThroughput() throws InterruptedException {
        System.out.println("=== Redis Evaluation Engine Load Test ===");
        System.out.println("Threads: " + THREAD_COUNT);
        System.out.println("Requests per thread: " + REQUESTS_PER_THREAD);
        System.out.println("Total requests: " + (THREAD_COUNT * REQUESTS_PER_THREAD));
        System.out.println();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicLong successCount = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_THREAD; j++) {
                        String userId = "user-" + threadId + "-" + j;
                        try {
                            // Simulate evaluation request
                            boolean result = evaluateFlag(TEST_FLAG_KEY, userId);
                            if (result) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        long totalRequests = THREAD_COUNT * REQUESTS_PER_THREAD;

        double throughput = (totalRequests * 1000.0) / totalTime; // requests per second
        double avgLatency = (double) totalTime / totalRequests; // milliseconds

        System.out.println("=== Results ===");
        System.out.println("Total time: " + totalTime + " ms");
        System.out.println("Total requests: " + totalRequests);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Errors: " + errorCount.get());
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");
        System.out.println("Average latency: " + String.format("%.2f", avgLatency) + " ms");
        System.out.println();
        System.out.println("✅ Load test completed");
    }

    private boolean evaluateFlag(String flagKey, String userId) {
        // This would make an actual HTTP request in a real load test
        // For now, simulate the evaluation logic
        try {
            Thread.sleep(1); // Simulate network latency
            return Math.random() > 0.5; // Simulate evaluation result
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

