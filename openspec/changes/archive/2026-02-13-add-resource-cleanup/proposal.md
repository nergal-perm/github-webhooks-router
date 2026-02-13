# Change: Add resource cleanup for DynamoDbSource on shutdown

## Why
`DynamoDbSource` holds a `DynamoDbClient` instance that is never closed. When the daemon shuts down via SIGTERM, the AWS SDK HTTP client and its connection pool are left open, causing a resource leak on every restart.

## What Changes
- `DynamoDbSource` implements `AutoCloseable` and closes its underlying `DynamoDbClient` on `close()`
- The `Main` shutdown hook closes `DynamoDbSource` after the scheduler has stopped

## Impact
- Affected specs: `downloader`, `system-lifecycle`
- Affected code: `DynamoDbSource.java`, `Main.java`
