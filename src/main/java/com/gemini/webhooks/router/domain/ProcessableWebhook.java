package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;
import com.gemini.webhooks.router.utils.OutputFilename;

import java.nio.file.Path;
import java.time.Instant;

public class ProcessableWebhook {
    private final AgentTask task;
    private final WebhookPayload payload;
    private final Path outputFile;

    public ProcessableWebhook(AgentTask task, WebhookPayload payload, Path outputDir) {
        this.task = task;
        this.payload = payload;
        String outputFilename = OutputFilename.generate(task.repoName(), payload.issueNumber(), Instant.now());
        this.outputFile = outputDir.resolve(outputFilename);
    }

    public String repoName() {
        return task.repoName();
    }

    public String webhookContent() {
        return payload.rawJson();
    }

    public Path outputFile() {
        return outputFile;
    }
}
