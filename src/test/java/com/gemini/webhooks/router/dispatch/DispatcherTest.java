package com.gemini.webhooks.router.dispatch;

import com.gemini.webhooks.router.AppConfig;
import com.gemini.webhooks.router.storage.FileSystemRepository;
import com.gemini.webhooks.router.storage.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DispatcherTest {

    @TempDir
    Path tempDir;

    private AppConfig config;
    private TaskRepository repository;
    private Dispatcher dispatcher;

    @BeforeEach
    void setUp() throws IOException {
        Path storageRoot = tempDir.resolve("storage");
        Path repoBaseDir = tempDir.resolve("repos");

        config = new AppConfig(storageRoot, repoBaseDir);
        repository = FileSystemRepository.create(config.pendingDir().toString());

        Files.createDirectories(config.pendingDir());
        Files.createDirectories(config.processingDir());
        Files.createDirectories(config.completedDir());
        Files.createDirectories(config.failedDir());
        Files.createDirectories(config.outputsDir());

        // Use synchronous executor for tests
        dispatcher = new Dispatcher(config, repository, new AgentProcess(repoBaseDir), Runnable::run);
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
    void dispatch_shouldMoveToFailedWhenRepoDirectoryNotFound() throws IOException {
        String validFilename = "2026-01-29T12:00:00.000Z_my-repo_abc12345.json";
        Path webhookFile = config.pendingDir().resolve(validFilename);
        Files.writeString(webhookFile, "{}");

        dispatcher.dispatch();

        assertThat(Files.exists(config.pendingDir().resolve(validFilename))).isFalse();
        assertThat(Files.exists(config.failedDir().resolve(validFilename))).isTrue();
    }

    @Test
    void dispatch_shouldProcessAllPendingFiles() throws IOException {
        // Create two webhook files
        String filename1 = "2026-01-29T12:00:00.000Z_repo1_abc12345.json";
        String filename2 = "2026-01-29T12:00:01.000Z_repo2_def67890.json";
        Files.writeString(config.pendingDir().resolve(filename1), "{}");
        Files.writeString(config.pendingDir().resolve(filename2), "{}");

        // Create repo directories
        Files.createDirectories(config.repoBaseDir().resolve("repo1"));
        Files.createDirectories(config.repoBaseDir().resolve("repo2"));

        // Dispatch should process BOTH files
        dispatcher.dispatch();

        // Both files should be out of pending and processing
        assertThat(Files.exists(config.pendingDir().resolve(filename1))).isFalse();
        assertThat(Files.exists(config.pendingDir().resolve(filename2))).isFalse();
        assertThat(Files.exists(config.processingDir().resolve(filename1))).isFalse();
        assertThat(Files.exists(config.processingDir().resolve(filename2))).isFalse();
    }

    @Test
    void dispatch_shouldRecoverStuckWebhooks() throws IOException {
        // Create a file in processing directory
        String filename = "2026-01-30T12:00:00.000Z_test-repo_abc12345.json";
        Files.createDirectories(config.processingDir());
        Files.writeString(config.processingDir().resolve(filename), "{}");
        
        // Create repo dir so it doesn't fail on that
        Files.createDirectories(config.repoBaseDir().resolve("test-repo"));

        // Dispatch should recover the file and then process it
        dispatcher.dispatch();

        // File should be processed (moved out of processing)
        assertThat(Files.exists(config.processingDir().resolve(filename))).isFalse();
    }

    @Test
    void dispatch_shouldCreateOutputFileForWebhookWithIssue() throws IOException {
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
        Files.createDirectories(config.repoBaseDir().resolve("test-repo"));

        dispatcher.dispatch();

        // Check that an output file was created in the outputs directory
        assertThat(Files.list(config.outputsDir()))
                .anyMatch(path -> path.getFileName().toString().startsWith("test-repo_issue-42_"));
    }

    @Test
    void dispatch_shouldCreateOutputFileForWebhookWithoutIssue() throws IOException {
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
        Files.createDirectories(config.repoBaseDir().resolve("test-repo"));

        dispatcher.dispatch();

        // Check that an output file was created without issue number
        assertThat(Files.list(config.outputsDir()))
                .anyMatch(path -> {
                    String name = path.getFileName().toString();
                    return name.startsWith("test-repo_") && !name.contains("issue");
                });
    }
}