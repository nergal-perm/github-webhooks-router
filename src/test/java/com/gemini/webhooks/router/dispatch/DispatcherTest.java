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

        dispatcher = new Dispatcher(config, repository);
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
        Files.writeString(webhookFile, "Fix the bug");

        dispatcher.dispatch();

        assertThat(Files.exists(config.pendingDir().resolve(validFilename))).isFalse();
        assertThat(Files.exists(config.failedDir().resolve(validFilename))).isTrue();
    }

    @Test
    void dispatch_shouldSkipWhenAgentIsActive() throws IOException {
        // Create two webhook files
        String filename1 = "2026-01-29T12:00:00.000Z_repo1_abc12345.json";
        String filename2 = "2026-01-29T12:00:01.000Z_repo2_def67890.json";
        Files.writeString(config.pendingDir().resolve(filename1), "task 1");
        Files.writeString(config.pendingDir().resolve(filename2), "task 2");

        // Create repo directories
        Files.createDirectories(config.repoBaseDir().resolve("repo1"));
        Files.createDirectories(config.repoBaseDir().resolve("repo2"));

        // First dispatch will process the first file
        dispatcher.dispatch();

        // Both files should still be in pending or processing since agent will fail
        // (gemini command doesn't exist)
        assertThat(Files.exists(config.pendingDir().resolve(filename2))).isTrue();
    }
}
