## ADDED Requirements

### Requirement: Unit File Validity
The project SHALL ship a systemd service unit file at `deploy/webhooks-router.service` that is syntactically valid and accepted by systemd without errors or warnings.

#### Scenario: Unit file present in repository
- **WHEN** the repository is checked out
- **THEN** the file `deploy/webhooks-router.service` exists

#### Scenario: Unit file passes systemd validation
- **GIVEN** a host with systemd installed
- **WHEN** `systemd-analyze verify deploy/webhooks-router.service` is run
- **THEN** the command exits with code 0 and reports no errors

### Requirement: Boot Enablement
The unit file SHALL declare `WantedBy=multi-user.target` so that `systemctl enable` causes the service to start automatically on system boot.

#### Scenario: Service starts after enable and reboot
- **GIVEN** the unit file is installed in `/etc/systemd/system/`
- **AND** `systemctl enable webhooks-router` has been run
- **WHEN** the host reboots
- **THEN** the `webhooks-router` process is running before any interactive login session begins

#### Scenario: WantedBy directive present
- **WHEN** the unit file is inspected
- **THEN** the `[Install]` section contains `WantedBy=multi-user.target`

### Requirement: Restart on Failure
The service SHALL restart automatically when the process exits with a non-zero exit code.

#### Scenario: Non-zero exit triggers restart
- **GIVEN** the service is running
- **WHEN** the JVM exits with a non-zero status (e.g. unhandled exception, OOM)
- **THEN** systemd restarts the process within the default `RestartSec` interval

#### Scenario: Clean exit does not restart
- **GIVEN** the service is running
- **WHEN** the process exits with code 0 (e.g. triggered by SIGTERM graceful shutdown)
- **THEN** systemd does NOT restart the process automatically

### Requirement: Journal Log Routing
The service SHALL route both stdout and stderr to the systemd journal so that logs are accessible via `journalctl`.

#### Scenario: Logs visible in journal
- **GIVEN** the service is running
- **WHEN** the daemon emits a log line (e.g. the heartbeat message) to stdout
- **THEN** the message is visible in `journalctl -u webhooks-router`

#### Scenario: StandardOutput and StandardError directives present
- **WHEN** the unit file is inspected
- **THEN** the `[Service]` section contains `StandardOutput=journal`
- **AND** the `[Service]` section contains `StandardError=journal`

### Requirement: Non-root Service User
The service SHALL run under a dedicated system user account, not as root.

#### Scenario: User directive present
- **WHEN** the unit file is inspected
- **THEN** the `[Service]` section contains a `User=` directive set to a non-root account name (e.g. `webhooks-router`)

#### Scenario: Process runs as non-root
- **GIVEN** the service is running
- **WHEN** the process list is inspected
- **THEN** the `webhooks-router` process is owned by the configured non-root user and NOT by `root`

### Requirement: Deployment Documentation
The repository SHALL include a `deploy/README.md` with step-by-step instructions for installing and enabling the service on Ubuntu.

#### Scenario: README covers full install sequence
- **WHEN** an operator follows `deploy/README.md` on a clean Ubuntu host
- **THEN** the service is running and enabled for boot without additional research
- **AND** the README includes steps for: building the jar, creating the system user, placing the jar, installing the unit file, running `systemctl enable --now`, and verifying with `journalctl`
