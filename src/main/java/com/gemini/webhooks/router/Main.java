package com.gemini.webhooks.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        logger.info("Starting Webhooks Router Daemon...");

        // Schedule the "Hello World" task every 60 seconds
        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Hello, world. The time is {}", Instant.now());
        }, 0, 60, TimeUnit.SECONDS);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Webhooks Router Daemon...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            logger.info("Shutdown complete.");
        }));
    }
}
