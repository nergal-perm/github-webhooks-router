package com.gemini.webhooks.router;

import java.nio.file.Path;

public record AppConfig(Path storageRoot, Path repoBaseDir) {

    private static final Path DEFAULT_STORAGE_ROOT = Path.of("data");
    private static final Path DEFAULT_REPO_BASE_DIR = Path.of(System.getProperty("user.home"), "Dev");

    public static AppConfig defaults() {
        return new AppConfig(DEFAULT_STORAGE_ROOT, DEFAULT_REPO_BASE_DIR);
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
}
