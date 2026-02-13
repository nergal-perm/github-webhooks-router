## ADDED Requirements
### Requirement: Configurable Poll Interval
The Downloader MUST use a configurable poll interval (supplied via the `--poll-interval-seconds`
CLI flag) as the period between successive DynamoDB scans.
The default interval when no flag is supplied SHALL be 60 seconds.

#### Scenario: Default interval used when flag is absent
- **WHEN** the daemon starts without `--poll-interval-seconds`
- **THEN** the Downloader is scheduled to run every 60 seconds.

#### Scenario: Custom interval applied
- **WHEN** the daemon starts with `--poll-interval-seconds 120`
- **THEN** the Downloader is scheduled to run every 120 seconds.

### Requirement: Polling Schedule — Quiet Hours
The Downloader MUST support an optional daily quiet-hours window, configured via
`--quiet-hours-start` and `--quiet-hours-end` (both in HH:MM, 24-hour local time).
When the current local time falls within the window, the Downloader MUST skip the
DynamoDB scan, log a debug message, and return immediately.

#### Scenario: Current time is within quiet hours — polling skipped
- **WHEN** quiet hours are configured as 22:00–07:00
- **AND** the current local time is 23:30
- **THEN** the Downloader logs a debug message (e.g., "Skipping download: quiet hours active")
- **AND** no DynamoDB scan is performed.

#### Scenario: Current time is outside quiet hours — polling proceeds
- **WHEN** quiet hours are configured as 22:00–07:00
- **AND** the current local time is 10:00
- **THEN** the Downloader proceeds with the normal DynamoDB scan cycle.

#### Scenario: Midnight-spanning window — time before midnight is within hours
- **WHEN** quiet hours are configured as 22:00–07:00
- **AND** the current local time is 22:45
- **THEN** the Downloader skips the scan (window spans midnight; 22:45 is after start).

#### Scenario: Midnight-spanning window — time after midnight is within hours
- **WHEN** quiet hours are configured as 22:00–07:00
- **AND** the current local time is 03:00
- **THEN** the Downloader skips the scan (03:00 is before end time 07:00).

#### Scenario: Midnight-spanning window — boundary of end time is outside hours
- **WHEN** quiet hours are configured as 22:00–07:00
- **AND** the current local time is 07:00
- **THEN** the Downloader proceeds with the normal scan (end boundary is exclusive).

#### Scenario: Quiet hours not configured — polling always proceeds
- **WHEN** neither `--quiet-hours-start` nor `--quiet-hours-end` is supplied
- **THEN** the Downloader runs on every scheduled cycle regardless of the time of day.
