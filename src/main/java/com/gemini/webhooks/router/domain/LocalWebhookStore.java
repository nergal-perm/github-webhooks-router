package com.gemini.webhooks.router.domain;

import com.gemini.webhooks.router.AppConfig;
import com.gemini.webhooks.router.storage.FileSystemRepository;
import com.gemini.webhooks.router.storage.TaskRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class LocalWebhookStore implements WebhookStore {

    private final TaskRepository repository;
    private final Function<String, WebhookFilename> filenameFactory;

    public static LocalWebhookStore create(AppConfig config) {
        TaskRepository repository = FileSystemRepository.create(config.pendingDir().toString());
        return new LocalWebhookStore(repository);
    }

    public LocalWebhookStore(TaskRepository repository) {
        this(repository, WebhookFilename::create);
    }

    LocalWebhookStore(TaskRepository repository, Function<String, WebhookFilename> filenameFactory) {
        this.repository = repository;
        this.filenameFactory = filenameFactory;
    }

    @Override
    public Path save(String repoName, String jsonPayload) throws IOException {
        WebhookFilename filename = filenameFactory.apply(repoName);
        return repository.save(filename.toFilename(), jsonPayload);
    }
}
