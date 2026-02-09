package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.ActiveRepos;
import com.gemini.webhooks.router.tasks.AgentTask;

public class InvalidAgentTask implements AgentTask {

    public InvalidAgentTask(String message) {}

    @Override
    public String repoName() {
        return "INVALID";
    }

    @Override
    public boolean isForActive(ActiveRepos repos) {
        return false;
    }

    @Override
    public String toFilename() {
        return "";
    }
}
