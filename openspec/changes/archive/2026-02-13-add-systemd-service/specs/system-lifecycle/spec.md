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

#### Scenario: systemd SIGTERM triggers graceful shutdown
- **GIVEN** the service is managed by systemd and the unit file uses the default `KillSignal` (SIGTERM)
- **WHEN** `systemctl stop webhooks-router` is issued
- **THEN** systemd sends SIGTERM to the JVM process
- **AND** the application performs its graceful shutdown sequence (see Graceful Shutdown scenario above)
- **AND** the process exits with code 0 within the `TimeoutStopSec` window
- **AND** systemd reports the unit as `inactive (dead)` after the stop completes

#### Scenario: Service starts on boot via systemd
- **GIVEN** the unit file is installed and enabled (`WantedBy=multi-user.target`)
- **WHEN** the Ubuntu host boots to multi-user runlevel
- **THEN** systemd starts the webhooks-router service automatically
- **AND** both the Downloader and Dispatcher threads begin their scheduled work
- **AND** the first heartbeat log line is emitted to the journal within 60 seconds of start

#### Scenario: Logs routed to systemd journal
- **GIVEN** the service is started by systemd with `StandardOutput=journal`
- **WHEN** the daemon emits any log output (including heartbeats and error messages)
- **THEN** all output is captured by journald and accessible via `journalctl -u webhooks-router`
- **AND** no separate log-file configuration is required for basic operational visibility
