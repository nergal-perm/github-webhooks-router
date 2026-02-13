# Design: Downloader

## Context
The Downloader is the producer thread in the daemon's producer-consumer architecture. It polls DynamoDB and feeds the local `pending/` queue that the Dispatcher (consumer) already processes.

**DynamoDB table contract (provided):**
- Table: `GithubWebhookTable`
- Partition key: `deliveryId` (String — GitHub's delivery UUID, e.g. `72d3162e-cc78-11e3-81ab-4c9367dc0958`)
- Attribute: `payload` (String — raw GitHub webhook JSON body)
- No sort key, no GSI, no LSI, no TTL

## Goals / Non-Goals
- **Goals:** reliably move every DynamoDB record to a local file exactly once (at-least-once with dedup); survive network outages without crashing.
- **Non-Goals:** quiet hours, configurable interval, DynamoDB Streams, batched deletes.

## Decisions

### Use Scan, not Query
There is no GSI and no sort key, so a full-table Scan is the only option. The table is expected to be small at any given time (items are deleted immediately after download), so Scan overhead is negligible.

### Use `deliveryId` as the filename unique ID (idempotency)
`WebhookFilename` expects an 8-char hex unique ID. GitHub delivery UUIDs are full UUIDs (`72d3162e-cc78-11e3-81ab-...`). We take the first 8 hex characters of the UUID (stripping dashes: `72d3162e`), which satisfies the existing `[a-f0-9]{8}` pattern and makes the filename deterministic per delivery.

**Benefit:** If the process crashes after writing but before deleting, the next Scan returns the same `deliveryId`. The Downloader detects the file already exists in `pending/` (duplicate), skips the write, and deletes the DynamoDB record — no duplicate work reaches the Dispatcher.

### Delete-after-write (at-least-once)
Write to `pending/` → on success, delete from DynamoDB. If the write fails, skip the delete. The item remains in DynamoDB and will be retried on the next Scan cycle. If the delete fails after a successful write, the file stays in `pending/` and the DynamoDB item will be re-scanned on the next cycle — the duplicate-detection logic handles it.

### Nullable infrastructure wrapper (`DynamoDbSource`)
Following the established project pattern (A-Frame / Nullables), the DynamoDB client is wrapped in a `DynamoDbSource` infrastructure class with `create()` and `createNull(records)` factory methods. Tests use the Null version; production uses the real AWS client.

### Repository name from payload
The Downloader parses `repository.full_name` from the raw JSON payload (e.g. `"owner/repo"`). `WebhookFilename` already sanitizes slashes to dashes, so no extra handling is needed.

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Large Scan if DynamoDB backlog grows | Table stays small by design — items are deleted immediately after download |
| AWS credentials not configured | Downloader logs a startup error and the thread exits; Dispatcher continues unaffected |
| `payload` field missing from DynamoDB item | Log warning, skip item, do NOT delete — operator can inspect and retry |

## Open Questions
- None. The schema and scope are fully defined.
