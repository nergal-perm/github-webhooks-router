package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.tasks.AgentTask;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessableWebhookTest {

    @Test
    void outputFile_shouldIncludeIssueNumberWhenPresent() {
        WebhookPayload payload = new WebhookPayload("""
                {"issue": {"number": 42}}
                """);
        AgentTask task = WebhookFilename.create("test-repo");
        Path outputDir = Path.of("/tmp/outputs");

        ProcessableWebhook webhook = new ProcessableWebhook(task, payload, outputDir);

        Path outputFile = webhook.outputFile();
        assertThat(outputFile.getFileName().toString())
                .startsWith("test-repo_issue-42_")
                .endsWith(".txt");
    }

    @Test
    void outputFile_shouldExcludeIssueNumberWhenAbsent() {
        WebhookPayload payload = new WebhookPayload("""
                {"ref": "refs/heads/main"}
                """);
        AgentTask task = WebhookFilename.create("test-repo");
        Path outputDir = Path.of("/tmp/outputs");

        ProcessableWebhook webhook = new ProcessableWebhook(task, payload, outputDir);

        Path outputFile = webhook.outputFile();
        assertThat(outputFile.getFileName().toString())
                .startsWith("test-repo_")
                .doesNotContain("issue")
                .endsWith(".txt");
    }

    @Test
    void repoName_shouldReturnTaskRepoName() {
        AgentTask task = WebhookFilename.create("my-repo");
        ProcessableWebhook webhook = new ProcessableWebhook(task, new WebhookPayload("{}"), Path.of("/tmp"));

        assertThat(webhook.repoName()).isEqualTo("my-repo");
    }

    @Test
    void webhookContent_shouldReturnRawJson() {
        String json = "{\"test\": \"data\"}";
        AgentTask task = WebhookFilename.create("repo");
        ProcessableWebhook webhook = new ProcessableWebhook(task, new WebhookPayload(json), Path.of("/tmp"));

        assertThat(webhook.webhookContent()).isEqualTo(json);
    }
}
