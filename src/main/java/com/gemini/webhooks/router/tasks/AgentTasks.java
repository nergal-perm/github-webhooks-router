package com.gemini.webhooks.router.tasks;

import com.gemini.webhooks.router.domain.ProcessableWebhook;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface AgentTasks {
    void recoverStuck(ActiveRepos repos);

    List<AgentTask> listPending();

    void clearInvalid();

    boolean startProcessing(AgentTask task);

    Optional<String> readContent(AgentTask task);

    Optional<ProcessableWebhook> prepareForProcessing(AgentTask task, Path outputDir);

    void completeTask(AgentTask task);

    void failTask(AgentTask task);
}
