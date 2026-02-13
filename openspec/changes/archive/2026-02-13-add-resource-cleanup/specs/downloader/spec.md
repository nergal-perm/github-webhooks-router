## ADDED Requirements

### Requirement: DynamoDbSource Resource Lifecycle
`DynamoDbSource` MUST implement `AutoCloseable` and release its underlying AWS SDK client when closed, so that HTTP connections and thread pools are not leaked after shutdown.

#### Scenario: Close releases the AWS client
Given `DynamoDbSource` was created with a real DynamoDB client
When `close()` is called
Then the underlying `DynamoDbClient` is closed and its resources are released.

#### Scenario: Close on Null variant is a safe no-op
Given `DynamoDbSource` was created via `createNull()`
When `close()` is called
Then no exception is thrown
And no real client interactions occur.
