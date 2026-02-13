# Change: Add Downloader — DynamoDB-to-local-disk webhook fetcher

## Why
The daemon currently processes webhooks already present in `pending/`, but has no way to populate that directory from the cloud. Without the Downloader, the bridge between the AWS Lambda → DynamoDB capture pipeline and the local AI agent is completely missing.

## What Changes
- Add a new `downloader` capability: a scheduled thread that polls `GithubWebhookTable` in DynamoDB, writes each webhook as a JSON file to `pending/`, and deletes the DynamoDB record only after a successful disk write.
- Extend `system-lifecycle` to include the Downloader as a second daemon thread started at startup and shut down gracefully alongside the Dispatcher.
- Add the AWS SDK v2 DynamoDB dependency (`software.amazon.awssdk:dynamodb`) to `pom.xml`.

## Out of Scope (deferred)
- Quiet hours / time-based polling suppression
- Configurable polling interval (hard-coded to 60 s for now, consistent with Dispatcher)
- DynamoDB Streams or event-driven push (Scan-based polling only)

## Impact
- **Affected specs:** `downloader` (new), `system-lifecycle` (modified)
- **Affected code:** `pom.xml`, `Main.java`, new `downloader/` package
- **No breaking changes** to existing Dispatcher or storage layers
