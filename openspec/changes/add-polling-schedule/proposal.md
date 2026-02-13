# Change: Add Configurable Polling Interval and Quiet Hours

## Why
The poll interval is currently hardcoded to 60 seconds in `Main.java`, and the Downloader
runs unconditionally around the clock. `project.md` explicitly calls for a configurable
schedule with "quiet hours" support so the daemon does not hammer DynamoDB (or wake
sleeping machines) during nighttime hours.

## What Changes
- The poll interval becomes configurable via `--poll-interval-seconds` (default: 60).
- Two new CLI flags, `--quiet-hours-start` and `--quiet-hours-end` (HH:MM, 24-hour),
  define a daily window during which the Downloader skips its DynamoDB scan.
- When the current local time falls inside the quiet window the Downloader logs a
  debug message and returns early without touching DynamoDB or the filesystem.
- Midnight-spanning windows are supported (e.g., `22:00`–07:00`).
- The Dispatcher schedule is unaffected; only the Downloader obeys quiet hours.

## Impact
- Affected specs: `downloader`
- Affected code: `Main.java`, `Downloader.java` (new guard), CLI argument wiring
  (cross-reference: `add-cli-args` proposal, not yet created)
- No breaking changes to the DynamoDB schema or the local file format.
