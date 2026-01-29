package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.AppConfig;
import com.gemini.webhooks.router.domain.WebhookFilename;
import com.gemini.webhooks.router.storage.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final AppConfig config;
    private final TaskRepository repository;
    private final AgentProcess agentProcess;
    private final AtomicBoolean agentActive = new AtomicBoolean(false);

    public Dispatcher(AppConfig config, TaskRepository repository) {
        this.config = config;
        this.repository = repository;
        this.agentProcess = new AgentProcess(config.repoBaseDir());
    }

    public void dispatch() {
        // Early return if agent is currently active
        if (agentActive.get()) {
            logger.debug("Agent subprocess is active, skipping dispatch cycle");
            return;
        }

        try {
            List<String> pendingFiles = repository.list();

            if (pendingFiles.isEmpty()) {
                logger.debug("No pending webhook files to process");
                return;
            }

            // Process the first file (serial processing)
            String filename = pendingFiles.get(0);
            logger.info("Processing webhook file: {}", filename);

            processWebhook(filename);

        } catch (Exception e) {
            logger.error("Error during dispatch cycle", e);
        }
    }

    private void processWebhook(String filename) {
        agentActive.set(true);
        try {
            // Parse filename to extract repository name
            WebhookFilename webhookFilename;
            try {
                webhookFilename = WebhookFilename.parse(filename);
            } catch (IllegalArgumentException e) {
                logger.error("Failed to parse webhook filename: {}", filename, e);
                moveToFailed(filename);
                return;
            }

            String repoName = webhookFilename.repoName();

            // Move to processing directory
            try {
                repository.move(filename, config.pendingDir(), config.processingDir());
                logger.info("Moved {} to processing directory", filename);
            } catch (IOException e) {
                logger.error("Failed to move file to processing: {}", filename, e);
                return;
            }

            // Read webhook content
            String webhookContent;
            try {
                Path processingFilePath = config.processingDir().resolve(filename);
                webhookContent = Files.readString(processingFilePath);
            } catch (IOException e) {
                logger.error("Failed to read webhook content from: {}", filename, e);
                moveFromProcessingToFailed(filename);
                return;
            }

            // Execute agent process
            AgentProcess.ProcessResult result = agentProcess.execute(repoName, webhookContent);

            // Handle result
            if (result.isSuccess()) {
                moveFromProcessingToCompleted(filename);
            } else {
                logger.error("Agent process failed: {}", result.errorMessage());
                moveFromProcessingToFailed(filename);
            }

        } finally {
            agentActive.set(false);
        }
    }

    private void moveToFailed(String filename) {
        try {
            repository.move(filename, config.pendingDir(), config.failedDir());
            logger.info("Moved {} to failed directory", filename);
        } catch (IOException e) {
            logger.error("Failed to move file to failed directory: {}", filename, e);
        }
    }

    private void moveFromProcessingToCompleted(String filename) {
        try {
            repository.move(filename, config.processingDir(), config.completedDir());
            logger.info("Moved {} to completed directory", filename);
        } catch (IOException e) {
            logger.error("Failed to move file to completed directory: {}", filename, e);
        }
    }

    private void moveFromProcessingToFailed(String filename) {
        try {
            repository.move(filename, config.processingDir(), config.failedDir());
            logger.info("Moved {} to failed directory", filename);
        } catch (IOException e) {
            logger.error("Failed to move file to failed directory: {}", filename, e);
        }
    }
}
