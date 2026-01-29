package com.gemini.webhooks.router;

import java.nio.file.Path;

public record AppConfig(Path storageRoot) {

    private static final Path DEFAULT_STORAGE_ROOT = Path.of("data");

    public static AppConfig defaults() {
        return new AppConfig(DEFAULT_STORAGE_ROOT);
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
}
