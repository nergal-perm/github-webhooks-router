package com.gemini.webhooks.router.tasks;

public interface AgentTask {

    String repoName();

    boolean isForActive(ActiveRepos repos);

    String toFilename();
}
