package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.FileBasedTasksConfig;
import com.gemini.webhooks.router.storage.FileSystemTaskRepository;
import com.gemini.webhooks.router.storage.TaskRepository;
import com.gemini.webhooks.router.tasks.FileBasedAgentTasks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispatcherTest {

    @TempDir
    Path tempDir;

    private FileBasedTasksConfig config;
    private TaskRepository repository;
    private AgentProcess agentProcess;
    private Dispatcher dispatcher;

    @BeforeEach
    void setUp() throws IOException {
        Path storageRoot = tempDir.resolve("storage");
        Path repoBaseDir = tempDir.resolve("repos");

        config = new FileBasedTasksConfig(storageRoot, repoBaseDir);
        repository = FileSystemTaskRepository.create(config);

        Files.createDirectories(config.pendingDir());
        Files.createDirectories(config.processingDir());
        Files.createDirectories(config.completedDir());
        Files.createDirectories(config.failedDir());
        Files.createDirectories(config.outputsDir());

        agentProcess = AgentProcess.createNull();
        dispatcher = new Dispatcher(config, repository, new FileBasedAgentTasks(config, repository), agentProcess, Runnable::run);
    }

    @Test
    void dispatch_shouldHandleEmptyQueue() {
        dispatcher.dispatch();

        // Should not throw any exceptions
    }

    @Test
    void dispatch_shouldMoveInvalidFilenameToFailed() throws IOException {
        Path invalidFile = config.pendingDir().resolve("invalid-filename.json");
        Files.writeString(invalidFile, "test content");

        dispatcher.dispatch();

        assertThat(Files.exists(config.pendingDir().resolve("invalid-filename.json"))).isFalse();
        assertThat(Files.exists(config.failedDir().resolve("invalid-filename.json"))).isTrue();
    }

    @Test
    void dispatch_shouldMoveToFailedWhenAgentFails() throws IOException {
        dispatcher = new Dispatcher(
                config, repository,
                new FileBasedAgentTasks(config, repository),
                AgentProcess.createNull(AgentProcess.ProcessResult.failure("agent error")),
                Runnable::run
        );

        String validFilename = "2026-01-29T12:00:00.000Z_my-repo_abc12345.json";
        Path webhookFile = config.pendingDir().resolve(validFilename);
        Files.writeString(webhookFile, "{}");

        dispatcher.dispatch();

        assertThat(Files.exists(config.pendingDir().resolve(validFilename))).isFalse();
        assertThat(Files.exists(config.failedDir().resolve(validFilename))).isTrue();
    }

    @Test
    void dispatch_shouldProcessAllPendingFiles() throws IOException {
        String filename1 = "2026-01-29T12:00:00.000Z_repo1_abc12345.json";
        String filename2 = "2026-01-29T12:00:01.000Z_repo2_def67890.json";
        Files.writeString(config.pendingDir().resolve(filename1), "{}");
        Files.writeString(config.pendingDir().resolve(filename2), "{}");

        dispatcher.dispatch();

        assertThat(Files.exists(config.pendingDir().resolve(filename1))).isFalse();
        assertThat(Files.exists(config.pendingDir().resolve(filename2))).isFalse();
        assertThat(Files.exists(config.processingDir().resolve(filename1))).isFalse();
        assertThat(Files.exists(config.processingDir().resolve(filename2))).isFalse();
    }

    @Test
    void dispatch_shouldRecoverStuckWebhooks() throws IOException {
        String filename = "2026-01-30T12:00:00.000Z_test-repo_abc12345.json";
        Files.writeString(config.processingDir().resolve(filename), "{}");

        dispatcher.dispatch();

        assertThat(Files.exists(config.processingDir().resolve(filename))).isFalse();
    }

    @Test
    void dispatch_shouldPassOutputFileWithIssueNumber() throws IOException {
        var output = agentProcess.trackOutput();
        String filename = "2026-01-30T12:00:00.000Z_test-repo_abc12345.json";
        String webhookContent = """
                {
                    "issue": {
                        "number": 42,
                        "title": "Geography question"
                    },
                    "comment": {
                        "body": "What is the capital of Great Britain?"
                    }
                }
                """;
        Files.writeString(config.pendingDir().resolve(filename), webhookContent);

        dispatcher.dispatch();

        assertThat(output.data()).hasSize(1);
        String outputFilename = output.data().getFirst().outputFile().getFileName().toString();
        assertThat(outputFilename).startsWith("test-repo_issue-42_");
    }

    @Test
    void dispatch_shouldPassOutputFileWithoutIssueNumber() throws IOException {
        var output = agentProcess.trackOutput();
        String filename = "2026-01-30T12:00:00.000Z_test-repo_def67890.json";
        String webhookContent = """
                {
                    "ref": "refs/heads/main",
                    "commits": [
                        {
                            "message": "What is the capital of Great Britain?"
                        }
                    ]
                }
                """;
        Files.writeString(config.pendingDir().resolve(filename), webhookContent);

        dispatcher.dispatch();

        assertThat(output.data()).hasSize(1);
        String outputFilename = output.data().getFirst().outputFile().getFileName().toString();
        assertThat(outputFilename).startsWith("test-repo_");
        assertThat(outputFilename).doesNotContain("issue");
    }

    @Test
    void dispatch_shouldProcessSameRepoInNextCycle() throws IOException {
        String filename1 = "2026-01-29T12:00:00.000Z_my-repo_abc12345.json";
        String filename2 = "2026-01-29T12:00:01.000Z_my-repo_def67890.json";
        Files.writeString(config.pendingDir().resolve(filename1), "{}");

        dispatcher.dispatch();
        assertThat(Files.exists(config.completedDir().resolve(filename1))).isTrue();

        Files.writeString(config.pendingDir().resolve(filename2), "{}");
        dispatcher.dispatch();
        assertThat(Files.exists(config.completedDir().resolve(filename2))).isTrue();
    }

    @Test
    void dispatch_shouldSkipSameRepoWhenAlreadyActive() throws IOException {
        List<Runnable> submitted = new ArrayList<>();
        dispatcher = new Dispatcher(config, repository, new FileBasedAgentTasks(config, repository), agentProcess, submitted::add);

        String filename1 = "2026-01-29T12:00:00.000Z_same-repo_abc12345.json";
        String filename2 = "2026-01-29T12:00:01.000Z_same-repo_def67890.json";
        Files.writeString(config.pendingDir().resolve(filename1), "{}");
        Files.writeString(config.pendingDir().resolve(filename2), "{}");

        dispatcher.dispatch();

        assertThat(submitted).hasSize(1);
        assertThat(Files.exists(config.pendingDir().resolve(filename2))).isTrue();
    }
}