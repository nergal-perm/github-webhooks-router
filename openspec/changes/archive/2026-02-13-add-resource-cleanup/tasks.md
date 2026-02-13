## 1. DynamoDbSource resource cleanup
- [x] 1.1 Make `DynamoDbSource` implement `AutoCloseable` with a `close()` method that closes the `DynamoDbClient` (no-op in Null variant)
- [x] 1.2 Add a `trackClose()` method to the Null variant to verify `close()` was called in tests

## 2. Shutdown hook integration
- [x] 2.1 Update `Main` shutdown hook to call `dynamoDbSource.close()` after `scheduler.awaitTermination`

## 3. Tests
- [x] 3.1 Test that `close()` on the real variant delegates to the underlying client (via Null variant's close tracker)
- [x] 3.2 Test that `close()` on the Null variant is a safe no-op
