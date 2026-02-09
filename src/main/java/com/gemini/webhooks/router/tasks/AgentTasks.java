package com.gemini.webhooks.router.tasks;

import java.util.List;

public interface AgentTasks {
    void recoverStuck(ActiveRepos repos);

    List<AgentTask> listPending();

    void clearInvalid();

    boolean startProcessing(AgentTask task);
}
