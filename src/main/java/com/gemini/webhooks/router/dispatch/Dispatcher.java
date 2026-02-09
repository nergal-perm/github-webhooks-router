package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.gemini.webhooks.router.domain.ProcessableWebhook;
import com.gemini.webhooks.router.tasks.ActiveRepos;
import com.gemini.webhooks.router.tasks.AgentTask;
import com.gemini.webhooks.router.tasks.AgentTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final FileBasedTasksConfig config;
    private final AgentTasks tasks;
    private final AgentProcess agentProcess;
    private final ActiveRepos activeRepos = new ActiveRepos();
    private final Executor executor;

    public Dispatcher(FileBasedTasksConfig config, AgentTasks tasks) {
        this(config, tasks, AgentProcess.create(config.repoBaseDir()), Executors.newVirtualThreadPerTaskExecutor());
    }

    public Dispatcher(FileBasedTasksConfig config, AgentTasks tasks, AgentProcess agentProcess, Executor executor) {
        this.config = config;
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
            // Prepare webhook for processing
            Optional<ProcessableWebhook> webhookOpt = tasks.prepareForProcessing(task, config.outputsDir());
            if (webhookOpt.isEmpty()) {
                tasks.failTask(task);
                return;
            }

            ProcessableWebhook webhook = webhookOpt.get();
            Path outputFile = webhook.outputFile();
            logger.info("Agent output for {} will be written to: {}", webhook.repoName(), outputFile);

            // Execute agent process
            AgentProcess.ProcessResult result = agentProcess.execute(webhook.repoName(), webhook.webhookContent(), outputFile);

            // Handle result
            if (result.isSuccess()) {
                tasks.completeTask(task);
            } else {
                logger.error("Agent process failed for {}: {}", webhook.repoName(), result.errorMessage());
                tasks.failTask(task);
            }
        } catch (Exception e) {
            logger.error("Unexpected error processing webhook: {}", task.toFilename(), e);
            tasks.failTask(task);
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