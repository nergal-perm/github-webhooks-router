package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProcessableWebhook {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private final AgentTask task;
    private final WebhookPayload payload;
    private final Path outputFile;

    public ProcessableWebhook(AgentTask task, WebhookPayload payload, Path outputDir) {
        this.task = task;
        this.payload = payload;
        this.outputFile = outputDir.resolve(outputFilename(task.repoName(), payload.issueNumber(), Instant.now()));
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

    private static String outputFilename(String repoName, Optional<Integer> issueNumber, Instant timestamp) {
        String ts = FORMATTER.format(timestamp);
        return issueNumber
                .map(n -> "%s_issue-%d_%s.txt".formatted(repoName, n, ts))
                .orElse("%s_%s.txt".formatted(repoName, ts));
    }
}
