package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.AppConfig;
import com.gemini.webhooks.router.domain.WebhookFilename;
import com.gemini.webhooks.router.storage.TaskRepository;
import com.gemini.webhooks.router.utils.OutputFilename;
import com.gemini.webhooks.router.utils.WebhookParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Dispatcher {
    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final AppConfig config;
    private final TaskRepository repository;
    private final AgentProcess agentProcess;
    private final Set<String> activeRepos = ConcurrentHashMap.newKeySet();
    private final Executor executor;

    public Dispatcher(AppConfig config, TaskRepository repository) {
        this(config, repository, new AgentProcess(config.repoBaseDir()), Executors.newVirtualThreadPerTaskExecutor());
    }

    public Dispatcher(AppConfig config, TaskRepository repository, AgentProcess agentProcess, Executor executor) {
        this.config = config;
        this.repository = repository;
        this.agentProcess = agentProcess;
        this.executor = executor;
    }

    public void dispatch() {
        recoverStuckWebhooks();

        try {
            List<String> pendingFiles = repository.list();

            if (pendingFiles.isEmpty()) {
                logger.debug("No pending webhook files to process");
                return;
            }

            // Sort files to ensure chronological processing
            List<String> sortedFiles = pendingFiles.stream().sorted().toList();
            logger.info("Found {} pending webhook files", sortedFiles.size());

            for (String filename : sortedFiles) {
                // Parse filename to extract repository name
                WebhookFilename webhookFilename;
                try {
                    webhookFilename = WebhookFilename.parse(filename);
                } catch (IllegalArgumentException e) {
                    logger.error("Failed to parse webhook filename: {}", filename, e);
                    moveToFailed(filename);
                    continue;
                }

                String repoName = webhookFilename.repoName();

                // Check if repo is already being processed
                if (activeRepos.contains(repoName)) {
                    logger.debug("Repository {} is already being processed, skipping {}", repoName, filename);
                    continue;
                }

                // Try to acquire lock for repo
                if (activeRepos.add(repoName)) {
                    logger.info("Dispatching webhook file: {} for repo: {}", filename, repoName);
                    executor.execute(() -> {
                        try {
                            processWebhook(filename, webhookFilename);
                        } finally {
                            activeRepos.remove(repoName);
                        }
                    });
                }
            }

        } catch (Exception e) {
            logger.error("Error during dispatch cycle", e);
        }
    }

    private void processWebhook(String filename, WebhookFilename webhookFilename) {
        String repoName = webhookFilename.repoName();
        try {
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

            // Parse issue number from webhook JSON
            Optional<Integer> issueNumber = WebhookParser.extractIssueNumber(webhookContent);
            if (issueNumber.isPresent()) {
                logger.info("Extracted issue number: {} for repo: {}", issueNumber.get(), repoName);
            }

            // Generate output filename
            Instant sessionStart = Instant.now();
            String outputFilename = OutputFilename.generate(repoName, issueNumber, sessionStart);
            Path outputFile = config.outputsDir().resolve(outputFilename);
            logger.info("Agent output for {} will be written to: {}", repoName, outputFile);

            // Execute agent process
            AgentProcess.ProcessResult result = agentProcess.execute(repoName, webhookContent, outputFile);

            // Handle result
            if (result.isSuccess()) {
                moveFromProcessingToCompleted(filename);
            } else {
                logger.error("Agent process failed for {}: {}", repoName, result.errorMessage());
                moveFromProcessingToFailed(filename);
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing webhook: {}", filename, e);
            moveFromProcessingToFailed(filename);
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

    private void recoverStuckWebhooks() {
        try {
            List<String> processingFiles = repository.list(config.processingDir());
            for (String filename : processingFiles) {
                // Only recover if repo is not active
                try {
                    WebhookFilename wf = WebhookFilename.parse(filename);
                    if (!activeRepos.contains(wf.repoName())) {
                        logger.warn("Recovering stuck webhook: {}", filename);
                        repository.move(filename, config.processingDir(), config.pendingDir());
                    }
                } catch (Exception e) {
                    logger.error("Failed to recover stuck webhook: {}", filename, e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to list processing directory for recovery", e);
        }
    }
}