package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;
import com.gemini.webhooks.router.utils.OutputFilename;
import com.gemini.webhooks.router.utils.WebhookParser;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public class ProcessableWebhook {
    private final AgentTask task;
    private final String webhookContent;
    private final Path outputFile;

    public ProcessableWebhook(AgentTask task, String webhookContent, Path outputDir) {
        this.task = task;
        this.webhookContent = webhookContent;
        Optional<Integer> issueNumber = WebhookParser.extractIssueNumber(webhookContent);
        String outputFilename = OutputFilename.generate(task.repoName(), issueNumber, Instant.now());
        this.outputFile = outputDir.resolve(outputFilename);
    }

    public AgentTask task() {
        return task;
    }

    public String repoName() {
        return task.repoName();
    }

    public String webhookContent() {
        return webhookContent;
    }

    public Path outputFile() {
        return outputFile;
    }
}
