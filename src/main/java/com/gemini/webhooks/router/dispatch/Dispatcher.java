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

    private final Path outputsDir;
    private final AgentTasks tasks;
    private final AgentProcess agentProcess;
    private final ActiveRepos activeRepos = new ActiveRepos();
    private final Executor executor;

    public Dispatcher(FileBasedTasksConfig config, AgentTasks tasks) {
        this(config.outputsDir(), tasks, AgentProcess.create(config.repoBaseDir()), Executors.newVirtualThreadPerTaskExecutor());
    }

    public Dispatcher(Path outputsDir, AgentTasks tasks, AgentProcess agentProcess, Executor executor) {
        this.outputsDir = outputsDir;
        this.tasks = tasks;
        this.agentProcess = agentProcess;
        this.executor = executor;
    }

    public void dispatch() {
        tasks.clearInvalid();
        tasks.skipUnsupported();
        tasks.recoverStuck(activeRepos);

         tasks.listPending().stream()
                .filter(this::notForActiveRepositories)
                .forEach(this::processTask);
    }

    private void processWebhook(AgentTask task) {
        try {
            Optional<ProcessableWebhook> webhookOpt = tasks.prepareForProcessing(task, outputsDir);
            if (webhookOpt.isEmpty()) {
                tasks.failTask(task);
                return;
            }

            ProcessableWebhook webhook = webhookOpt.get();
            Path outputFile = webhook.outputFile();
            logger.info("Agent output for {} will be written to: {}", webhook.repoName(), outputFile);

            AgentProcess.ProcessResult result = agentProcess.execute(webhook.repoName(), webhook.webhookContent(), outputFile);

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
        final boolean repoIsTaken = activeRepos.isTaken(task.repoName());
        if (repoIsTaken) {
            logger.debug("The repo {} already has active task, skipping {}", task.repoName(), task.toFilename());
        }
        return !repoIsTaken;
    }

    private void processTask(AgentTask task) {
        if (!activeRepos.takeFor(task)) {
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
                activeRepos.releaseFor(task);
            }
        });
    }
}