package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;

public class InvalidAgentTask implements AgentTask {

    private final String message;

    public InvalidAgentTask(String message) {
        this.message = message;
    }

    @Override
    public String repoName() {
        return "INVALID";
    }

    @Override
    public String toFilename() {
        return message;
    }
}
