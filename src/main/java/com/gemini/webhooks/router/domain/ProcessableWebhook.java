package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;
import com.gemini.webhooks.router.utils.OutputFilename;
import com.gemini.webhooks.router.utils.WebhookParser;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a webhook task with its content, ready for processing.
 * Knows how to extract processing information (repo name, webhook content, output path).
 */
public class ProcessableWebhook {
    private final AgentTask task;
    private final String webhookContent;
    private final Path outputDir;

    public ProcessableWebhook(AgentTask task, String webhookContent, Path outputDir) {
        this.task = task;
        this.webhookContent = webhookContent;
        this.outputDir = outputDir;
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
        Optional<Integer> issueNumber = WebhookParser.extractIssueNumber(webhookContent);
        Instant sessionStart = Instant.now();
        String outputFilename = OutputFilename.generate(task.repoName(), issueNumber, sessionStart);
        return outputDir.resolve(outputFilename);
    }
}
