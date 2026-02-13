package com.gemini.webhooks.router.download;

import com.gemini.webhooks.router.domain.WebhookPayload;
import com.gemini.webhooks.router.domain.WebhookRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DynamoDbSource {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbSource.class);

    private final DynamoDbClient client;
    private final String tableName;
    private final List<WebhookRecord> nullRecords;
    private final List<DeleteTracker> trackers = new ArrayList<>();

    public static DynamoDbSource create(String tableName) {
        return new DynamoDbSource(DynamoDbClient.create(), tableName, null);
    }

    public static DynamoDbSource createNull() {
        return new DynamoDbSource(null, null, List.of());
    }

    public static DynamoDbSource createNull(List<WebhookRecord> records) {
        return new DynamoDbSource(null, null, List.copyOf(records));
    }

    private DynamoDbSource(DynamoDbClient client, String tableName, List<WebhookRecord> nullRecords) {
        this.client = client;
        this.tableName = tableName;
        this.nullRecords = nullRecords;
    }

    public DeleteTracker trackDeletes() {
        DeleteTracker tracker = new DeleteTracker();
        trackers.add(tracker);
        return tracker;
    }

    public List<WebhookRecord> fetchAll() {
        if (nullRecords != null) {
            return nullRecords;
        }
        ScanResponse response = client.scan(ScanRequest.builder().tableName(tableName).build());
        return response.items().stream()
                .map(item -> {
                    String deliveryId = item.get("deliveryId").s();
                    String rawPayload = item.containsKey("payload") ? item.get("payload").s() : null;
                    WebhookPayload payload = rawPayload != null ? new WebhookPayload(rawPayload) : null;
                    return new WebhookRecord(deliveryId, payload);
                })
                .toList();
    }

    public void delete(String deliveryId) {
        trackers.forEach(t -> t.add(deliveryId));
        if (nullRecords != null) {
            return;
        }
        client.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("deliveryId", AttributeValue.fromS(deliveryId)))
                .build());
        logger.debug("Deleted DynamoDB record: {}", deliveryId);
    }

    public static class DeleteTracker {
        private final List<String> deletedIds = new ArrayList<>();

        void add(String deliveryId) {
            deletedIds.add(deliveryId);
        }

        public List<String> deletedIds() {
            return Collections.unmodifiableList(deletedIds);
        }
    }
}
