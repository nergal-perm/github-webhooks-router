## 1. Dependency
- [x] 1.1 Add `software.amazon.awssdk:dynamodb` (v2) to `pom.xml`

## 2. Domain — Webhook Record value object
- [x] 2.1 Create `WebhookRecord` record (`deliveryId: String`, `payload: String`) in `domain/`
- [x] 2.2 Write tests: construction and field accessors

## 3. Infrastructure — DynamoDB source (Nullable)
- [x] 3.1 Create `DynamoDbSource` interface with `List<WebhookRecord> fetchAll()` and `void delete(String deliveryId)`
- [x] 3.2 Implement `DynamoDbSource.create(tableName)` — real AWS SDK DynamoDB client (Scan + DeleteItem)
- [x] 3.3 Implement `DynamoDbSource.createNull()` — returns empty list; delete is a no-op
- [x] 3.4 Implement `DynamoDbSource.createNull(List<WebhookRecord>)` — returns provided records for tests

## 4. Downloader component
- [x] 4.1 Create `Downloader` class with `download()` method
- [x] 4.2 Implement DynamoDB poll → extract repo name → generate filename → write pending → delete from DynamoDB
- [x] 4.3 Implement duplicate detection: if file already exists in `pending/`, skip write and delete from DynamoDB
- [x] 4.4 Handle missing/malformed `payload` and missing `repository.full_name` (log warning, skip, do NOT delete)
- [x] 4.5 Handle network/IO errors gracefully (log, continue to next record or skip cycle)

## 5. Tests for Downloader
- [x] 5.1 Test: empty source → no files written, no errors
- [x] 5.2 Test: one record → file created in pending/ with correct name and content
- [x] 5.3 Test: record with slash in repo name → filename sanitised correctly
- [x] 5.4 Test: write succeeds → DynamoDB record deleted
- [x] 5.5 Test: duplicate record → existing file NOT overwritten, DynamoDB record still deleted
- [x] 5.6 Test: missing `repository` field → no file written, no DynamoDB delete
- [x] 5.7 Test: malformed JSON payload → no file written, no DynamoDB delete

## 6. Configuration
- [x] 6.1 Add `tableName` field to `FileBasedTasksConfig` (default: `"GithubWebhookTable"`)

## 7. Wire into Main
- [x] 7.1 Instantiate `DynamoDbSource.create(config.tableName())` in `Main`
- [x] 7.2 Schedule `downloader.download()` at fixed rate (60 s, 0 s initial delay) alongside Dispatcher
- [x] 7.3 Verify graceful shutdown covers both threads (no changes needed to existing shutdown hook)
