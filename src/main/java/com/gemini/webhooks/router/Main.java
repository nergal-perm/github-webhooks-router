package com.gemini.webhooks.router;

import com.gemini.webhooks.router.dispatch.Dispatcher;
import com.gemini.webhooks.router.storage.FileSystemTaskRepository;
import com.gemini.webhooks.router.storage.TaskRepository;
import com.gemini.webhooks.router.tasks.AgentTasks;
import com.gemini.webhooks.router.tasks.FileBasedAgentTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void main(String[] args) {
        logger.info("Starting Webhooks Router Daemon...");

        FileBasedTasksConfig config = FileBasedTasksConfig.create();

        try {
            ensureDirectoriesExist(config);
        } catch (IOException e) {
            logger.error("Failed to initialize directories", e);
            System.exit(1);
        }

        TaskRepository repository = FileSystemTaskRepository.create(config);
        AgentTasks tasks = new FileBasedAgentTasks(config, repository);
        Dispatcher dispatcher = new Dispatcher(config, tasks);

        scheduler.scheduleAtFixedRate(() -> {
            logger.info("Hello, world. The time is {}", Instant.now());
        }, 0, 60, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                dispatcher.dispatch();
            } catch (Exception e) {
                logger.error("Dispatcher error", e);
            }
        }, 10, 60, TimeUnit.SECONDS);

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

    private static void ensureDirectoriesExist(FileBasedTasksConfig config) throws IOException {
        Files.createDirectories(config.pendingDir());
        Files.createDirectories(config.processingDir());
        Files.createDirectories(config.completedDir());
        Files.createDirectories(config.failedDir());
        Files.createDirectories(config.outputsDir());
        Files.createDirectories(config.skippedDir());
        logger.info("Initialized storage directories at: {}", config.storageRoot());
    }
}
