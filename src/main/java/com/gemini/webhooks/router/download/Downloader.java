package com.gemini.webhooks.router.download;

import com.gemini.webhooks.router.domain.WebhookFilename;
import com.gemini.webhooks.router.domain.WebhookRecord;
import com.gemini.webhooks.router.storage.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Downloader {

    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private final DynamoDbSource source;
    private final TaskRepository repository;

    public Downloader(DynamoDbSource source, TaskRepository repository) {
        this.source = source;
        this.repository = repository;
    }

    public void download() {
        List<WebhookRecord> records;
        try {
            records = source.fetchAll();
        } catch (Exception e) {
            logger.error("Failed to scan DynamoDB — will retry on next cycle", e);
            return;
        }

        for (WebhookRecord record : records) {
            processRecord(record);
        }
    }

    private void processRecord(WebhookRecord record) {
        Optional<String> repoFullName = record.repoFullName();
        if (repoFullName.isEmpty()) {
            logger.warn("Cannot determine repository for deliveryId: {}", record.deliveryId());
            return;
        }

        String uniqueIdSuffix = "_" + record.uniqueId() + ".json";
        WebhookFilename webhookFilename = WebhookFilename.create(Instant.now(), repoFullName.get(), record.uniqueId());
        String filename = webhookFilename.toFilename();

        if (repository.listPending().stream().anyMatch(f -> f.endsWith(uniqueIdSuffix))) {
            logger.debug("Duplicate detected: file with uniqueId {} already in pending/, cleaning up DynamoDB", record.uniqueId());
            deleteQuietly(record.deliveryId());
            return;
        }

        try {
            repository.createPendingTask(filename, record.rawJson());
            logger.info("Downloaded webhook {} → pending/{}", record.deliveryId(), filename);
        } catch (IOException e) {
            logger.error("Failed to write webhook to pending/ for deliveryId: {}", record.deliveryId(), e);
            return;
        }

        deleteQuietly(record.deliveryId());
    }

    private void deleteQuietly(String deliveryId) {
        try {
            source.delete(deliveryId);
        } catch (Exception e) {
            logger.error("Failed to delete DynamoDB record {} after write — will be retried", deliveryId, e);
        }
    }
}
