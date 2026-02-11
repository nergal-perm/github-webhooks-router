package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;

public class InvalidAgentTask implements AgentTask {

    private final String filename;
    private final String reason;

    public InvalidAgentTask(String filename, String reason) {
        this.filename = filename;
        this.reason = reason;
    }

    @Override
    public String repoName() {
        return "INVALID";
    }

    @Override
    public String toFilename() {
        return filename;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    public String reason() {
        return reason;
    }
}
