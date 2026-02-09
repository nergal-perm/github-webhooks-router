package com.gemini.webhooks.router.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AgentTasks {
    void recoverStuck(ActiveRepos repos);

    List<AgentTask> listPending();

    void clearInvalid();

    boolean startProcessing(AgentTask task);

    Optional<String> readContent(AgentTask task);

    void completeTask(AgentTask task);

    void failTask(AgentTask task);
}
