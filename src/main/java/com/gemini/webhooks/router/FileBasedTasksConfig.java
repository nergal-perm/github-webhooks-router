package com.gemini.webhooks.router;

import java.nio.file.Path;

public record FileBasedTasksConfig(Path storageRoot, Path repoBaseDir, String tableName) {

    private static final Path DEFAULT_STORAGE_ROOT = Path.of("data");
    private static final Path DEFAULT_REPO_BASE_DIR = Path.of(System.getProperty("user.home"), "Dev");
    private static final String DEFAULT_TABLE_NAME = "GithubWebhookTable";

    public static FileBasedTasksConfig create() {
        return new FileBasedTasksConfig(DEFAULT_STORAGE_ROOT, DEFAULT_REPO_BASE_DIR, DEFAULT_TABLE_NAME);
    }

    public static FileBasedTasksConfig create(Path storageRoot, Path repoBaseDir) {
        return new FileBasedTasksConfig(storageRoot, repoBaseDir, DEFAULT_TABLE_NAME);
    }

    public static FileBasedTasksConfig create(Path tempDir) {
        return new FileBasedTasksConfig(tempDir.resolve("data"), tempDir.resolve("repos"), DEFAULT_TABLE_NAME);
    }

    public Path pendingDir() {
        return storageRoot.resolve("pending");
    }

    public Path processingDir() {
        return storageRoot.resolve("processing");
    }

    public Path completedDir() {
        return storageRoot.resolve("completed");
    }

    public Path failedDir() {
        return storageRoot.resolve("failed");
    }

    public Path outputsDir() {
        return storageRoot.resolve("outputs");
    }

    public Path skippedDir() {
        return storageRoot.resolve("skipped");
    }
}
