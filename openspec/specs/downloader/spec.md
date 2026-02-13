# downloader Specification

## Purpose
TBD - created by archiving change add-downloader. Update Purpose after archive.
## Requirements
### Requirement: DynamoDB Polling
The downloader MUST scan the `GithubWebhookTable` DynamoDB table on each scheduled cycle to retrieve all pending webhook records.

#### Scenario: Items present in table
Given the `GithubWebhookTable` contains two records with `deliveryId` values "abc12345-..." and "def67890-..."
When the downloader runs a scheduled cycle
Then it retrieves both records for processing.

#### Scenario: Empty table
Given the `GithubWebhookTable` contains no records
When the downloader runs a scheduled cycle
Then no files are written to the `pending/` directory
And no error is raised.

#### Scenario: Network failure during scan
Given the DynamoDB service is unreachable
When the downloader attempts to scan
Then the error is logged
And no files are modified
And the downloader waits for the next scheduled interval before retrying.

### Requirement: Webhook Persistence
The downloader MUST write each downloaded webhook payload to the `pending/` directory as a UTF-8 JSON file before removing the record from DynamoDB.

#### Scenario: Valid payload — file written to pending
Given a DynamoDB record with `deliveryId` "72d3162e-cc78-11e3-81ab-4c9367dc0958" and a payload containing `"repository": {"full_name": "owner/my-repo"}`
When the downloader processes this record
Then a file named `{timestamp}_owner-my-repo_72d3162e.json` is created in the `pending/` directory
And the file content is the raw payload string exactly as stored in DynamoDB.

#### Scenario: Repository name extracted from full_name
Given a webhook payload contains `"repository": {"full_name": "acme-corp/backend-api"}`
When the downloader generates the filename
Then the repository segment of the filename is "acme-corp-backend-api" (slashes replaced with dashes).

#### Scenario: Missing repository field
Given a DynamoDB record whose payload does not contain a `repository` field
When the downloader processes this record
Then a warning is logged identifying the `deliveryId`
And no file is written to `pending/`
And the DynamoDB record is NOT deleted.

#### Scenario: Missing payload attribute
Given a DynamoDB record has no `payload` attribute
When the downloader processes this record
Then a warning is logged identifying the `deliveryId`
And no file is written to `pending/`
And the DynamoDB record is NOT deleted.

#### Scenario: Malformed JSON payload
Given a DynamoDB record whose `payload` attribute is not valid JSON
When the downloader processes this record
Then a warning is logged identifying the `deliveryId`
And no file is written to `pending/`
And the DynamoDB record is NOT deleted.

### Requirement: At-Least-Once Delivery
The downloader MUST guarantee that a webhook payload reaches the `pending/` directory before the corresponding DynamoDB record is deleted, so that no webhook is silently lost if the process is interrupted.

#### Scenario: Successful write followed by delete
Given a DynamoDB record is successfully written to `pending/`
When the disk write completes without error
Then the downloader deletes the record from `GithubWebhookTable`.

#### Scenario: Disk write failure — record retained
Given the `pending/` directory is not writable
When the downloader attempts to write a webhook payload
Then the error is logged
And the DynamoDB record is NOT deleted
And the downloader continues processing remaining records in the same cycle.

#### Scenario: DynamoDB delete failure — disk file retained
Given a webhook payload was successfully written to `pending/`
And the subsequent DynamoDB delete call fails
Then the error is logged
And the file remains in `pending/` and will be processed by the Dispatcher normally
And the DynamoDB record remains and will be re-encountered on the next scan cycle (handled by duplicate detection).

### Requirement: Duplicate Download Handling
The downloader MUST skip re-writing a webhook that is already present in the `pending/` directory, and MUST delete the corresponding DynamoDB record to prevent indefinite re-scanning.

#### Scenario: File already exists in pending
Given a DynamoDB record with `deliveryId` "72d3162e-..." exists
And a file `{timestamp}_owner-my-repo_72d3162e.json` already exists in `pending/`
When the downloader processes this record
Then the existing file is NOT overwritten
And the DynamoDB record IS deleted (dedup cleanup).

#### Scenario: File not in pending — normal write
Given no file with the derived filename exists in `pending/`
When the downloader processes the record
Then it proceeds with the normal write-then-delete flow.

### Requirement: AWS Configuration
The downloader MUST use the AWS SDK default credential chain and support a configurable DynamoDB table name.

#### Scenario: Default table name
Given no explicit table name is configured
When the downloader initialises its DynamoDB connection
Then it connects to the table named "GithubWebhookTable".

#### Scenario: Configured table name
Given the table name is configured as "MyCustomWebhookTable"
When the downloader initialises its DynamoDB connection
Then it connects to "MyCustomWebhookTable".

#### Scenario: AWS credentials unavailable
Given no AWS credentials are available in the default credential chain
When the downloader attempts to initialise
Then the error is logged at startup
And the Downloader thread exits without affecting the Dispatcher.

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

