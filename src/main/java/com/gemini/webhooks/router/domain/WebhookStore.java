package com.gemini.webhooks.router.domain;

import java.io.IOException;
import java.nio.file.Path;

public interface WebhookStore {
    /**
     * Persists a webhook payload to the pending queue.
     *
     * @param repoName    The repository name (used for concurrency locking downstream)
     * @param jsonPayload The raw JSON content
     * @return The path to the saved file
     */
    Path save(String repoName, String jsonPayload) throws IOException;
}
