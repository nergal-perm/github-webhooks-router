package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.gemini.webhooks.router.storage.TaskRepository;
import com.gemini.webhooks.router.tasks.ActiveRepos;
import com.gemini.webhooks.router.tasks.AgentTask;
import com.gemini.webhooks.router.tasks.AgentTasks;
import com.gemini.webhooks.router.utils.OutputFilename;
import com.gemini.webhooks.router.utils.WebhookParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final FileBasedTasksConfig config;
    private final TaskRepository repository;
    private final AgentTasks tasks;
    private final AgentProcess agentProcess;
    private final ActiveRepos activeRepos = new ActiveRepos();
    private final Executor executor;

    public Dispatcher(FileBasedTasksConfig config, TaskRepository repository, AgentTasks tasks) {
        this(config, repository, tasks, AgentProcess.create(config.repoBaseDir()), Executors.newVirtualThreadPerTaskExecutor());
    }

    public Dispatcher(FileBasedTasksConfig config, TaskRepository repository, AgentTasks tasks, AgentProcess agentProcess, Executor executor) {
        this.config = config;
        this.repository = repository;
        this.tasks = tasks;
        this.agentProcess = agentProcess;
        this.executor = executor;
    }

    public void dispatch() {
        tasks.clearInvalid();
        tasks.recoverStuck(activeRepos);

         tasks.listPending().stream()
                .filter(this::notForActiveRepositories)
                .forEach(this::processTask);
    }

    private void processWebhook(AgentTask task) {
        try {
            // Read webhook content
            String webhookContent;
            try {
                Path processingFilePath = config.processingDir().resolve(task.toFilename());
                webhookContent = Files.readString(processingFilePath);
            } catch (IOException e) {
                logger.error("Failed to read webhook content from: {}", task.toFilename(), e);
                moveFromProcessingToFailed(task.toFilename());
                return;
            }

            // Parse issue number from webhook JSON
            String repoName = task.repoName();
            Optional<Integer> issueNumber = WebhookParser.extractIssueNumber(webhookContent);
            issueNumber.ifPresent(integer -> logger.info("Extracted issue number: {} for repo: {}", integer, repoName));

            // Generate output filename
            Instant sessionStart = Instant.now();
            String outputFilename = OutputFilename.generate(repoName, issueNumber, sessionStart);
            Path outputFile = config.outputsDir().resolve(outputFilename);
            logger.info("Agent output for {} will be written to: {}", repoName, outputFile);

            // Execute agent process
            AgentProcess.ProcessResult result = agentProcess.execute(repoName, webhookContent, outputFile);

            // Handle result
            if (result.isSuccess()) {
                moveFromProcessingToCompleted(task.toFilename());
            } else {
                logger.error("Agent process failed for {}: {}", repoName, result.errorMessage());
                moveFromProcessingToFailed(task.toFilename());
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing webhook: {}", task.toFilename(), e);
            moveFromProcessingToFailed(task.toFilename());
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

    private boolean notForActiveRepositories(AgentTask task) {
        final boolean forActiveRepo = task.isForActive(activeRepos);
        if (forActiveRepo) {
            logger.debug("The repo {} already has active task, skipping {}", task.repoName(), task.toFilename());
        }
        return !forActiveRepo;
    }

    private void processTask(AgentTask task) {
        if (!activeRepos.lockRepoFor(task)) {
            return;
        }

        logger.info("Dispatching webhook file: {} for repo: {}", task.toFilename(), task.repoName());

        if (!tasks.startProcessing(task)) {
            return;
        }

        executor.execute(() -> {
            try {
                processWebhook(task);
            } finally {
                activeRepos.unlockRepoFor(task);
            }
        });
    }
}