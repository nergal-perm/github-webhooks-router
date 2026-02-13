## MODIFIED Requirements

### Requirement: Daemon Execution
The application MUST be capable of running as a long-lived background process (daemon) managed by the OS init system.

#### Scenario: Long-running Process
Given the application is started
When no external interrupts occur
Then the process remains running indefinitely (does not exit immediately after main).

#### Scenario: Downloader and Dispatcher both started
Given the application is started
When the daemon initialises
Then both the Downloader thread and the Dispatcher thread are scheduled to run
And the Downloader begins polling DynamoDB for pending webhooks
And the Dispatcher begins scanning the local `pending/` directory for files to dispatch.

#### Scenario: CLI flags applied before daemon threads start
Given the daemon is launched with `--storage-root /var/lib/webhooks-router --table-name ProdTable`
When the application initialises
Then the Downloader uses `ProdTable` as the DynamoDB table name
And the Dispatcher uses `/var/lib/webhooks-router` as the storage root
And both threads start normally.

#### Scenario: Graceful Shutdown
Given the application is running
And the dispatcher is processing webhooks
And the downloader is polling DynamoDB
When a `SIGTERM` signal is received
Then the application shuts down its internal schedulers and threads cleanly
And the Dispatcher stops accepting new work
And the Downloader stops scheduling new poll cycles
And any in-progress agent subprocess is allowed to complete or is terminated
And the `DynamoDbSource` is closed, releasing its AWS SDK client and connection pool
And the application exits.
