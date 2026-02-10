package com.gemini.webhooks.router.tasks;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.gemini.webhooks.router.domain.InvalidAgentTask;
import com.gemini.webhooks.router.domain.ProcessableWebhook;
import com.gemini.webhooks.router.domain.WebhookFilename;
import com.gemini.webhooks.router.storage.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileBasedAgentTasks implements AgentTasks {

    private static final Logger logger = LoggerFactory.getLogger(FileBasedAgentTasks.class);
    private final FileBasedTasksConfig config;
    private final TaskRepository tasks;

    public FileBasedAgentTasks(FileBasedTasksConfig config, TaskRepository tasks) {
        this.config = config;
        this.tasks = tasks;
    }

    @Override
    public void recoverStuck(ActiveRepos repos) {
        try {
            List<String> processingFiles = tasks.listProcessing();
            for (String filename : processingFiles) {
                try {
                    AgentTask task = WebhookFilename.parse(filename);
                    if (!repos.isTaken(task.repoName())) {
                        logger.warn("Recovering stuck webhook: {}", filename);
                        tasks.move(filename, config.processingDir(), config.pendingDir());
                    }
                } catch (Exception e) {
                    logger.error("Failed to recover stuck webhook: {}", filename, e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to list processing directory for recovery", e);
        }
    }

    @Override
    public List<AgentTask> listPending() {
        return tasks.listPending().stream()
                .map(WebhookFilename::parse)
                .toList();
    }

    @Override
    public void clearInvalid() {
        tasks.listPending().stream()
                .filter(filename -> WebhookFilename.parse(filename) instanceof InvalidAgentTask)
                .forEach(this::moveToFailed);
    }

    private void moveToFailed(String filename) {
        try {
            tasks.move(filename, config.pendingDir(), config.failedDir());
            logger.info("Moved {} to failed directory", filename);
        } catch (IOException e) {
            logger.error("Failed to move invalid webhook to failed: {}", filename, e);
        }
    }

    @Override
    public boolean startProcessing(AgentTask task) {
        try {
            tasks.move(task.toFilename(), config.pendingDir(), config.processingDir());
            logger.info("Moved {} to processing directory", task.toFilename());
        } catch (IOException e) {
            logger.error("Failed to move file to processing: {}", task.toFilename(), e);
            return false;
        }
        return true;
    }

    private Optional<String> readContent(AgentTask task) {
        try {
            Path processingFilePath = config.processingDir().resolve(task.toFilename());
            String content = Files.readString(processingFilePath);
            return Optional.of(content);
        } catch (IOException e) {
            logger.error("Failed to read webhook content from: {}", task.toFilename(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProcessableWebhook> prepareForProcessing(AgentTask task, Path outputDir) {
        return readContent(task).map(content ->
            new ProcessableWebhook(task, content, outputDir)
        );
    }

    @Override
    public void completeTask(AgentTask task) {
        try {
            tasks.move(task.toFilename(), config.processingDir(), config.completedDir());
            logger.info("Moved {} to completed directory", task.toFilename());
        } catch (IOException e) {
            logger.error("Failed to move file to completed directory: {}", task.toFilename(), e);
        }
    }

    @Override
    public void failTask(AgentTask task) {
        try {
            tasks.move(task.toFilename(), config.processingDir(), config.failedDir());
            logger.info("Moved {} to failed directory", task.toFilename());
        } catch (IOException e) {
            logger.error("Failed to move file to failed directory: {}", task.toFilename(), e);
        }
    }
}
