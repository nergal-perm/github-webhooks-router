## MODIFIED Requirements

### Requirement: Daemon Execution
The application MUST be capable of running as a long-lived background process (daemon) managed by the OS init system.

#### Scenario: Long-running Process
Given the application is started
When no external interrupts occur
Then the process remains running indefinitely (does not exit immediately after main).

#### Scenario: Graceful Shutdown
Given the application is running
And the dispatcher is processing webhooks
When a `SIGTERM` signal is received
Then the application shuts down its internal schedulers and threads cleanly
And the dispatcher stops accepting new work
And any in-progress agent subprocess is allowed to complete or is terminated
And the application exits.
