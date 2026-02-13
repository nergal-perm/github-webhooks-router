# Change: Add systemd Service Unit File

## Why
The daemon is designed to run as a systemd service on Ubuntu (per `project.md`), but no service unit file or deployment packaging exists today. Operators must start the process manually, there is no automatic restart on failure, and logs are not routed to the system journal. This change closes that gap by shipping a ready-to-use unit file and documenting the install procedure.

## What Changes
- New file `deploy/webhooks-router.service`: a systemd unit file that:
  - Runs the daemon as a dedicated non-root user (`webhooks-router`)
  - Uses `Restart=on-failure` so systemd restarts the process after a non-zero exit
  - Routes stdout/stderr to journald (no custom log-file configuration needed)
  - Launches the fat-jar produced by `mvn package` via `ExecStart`
  - Is enabled for `multi-user.target` so it starts on boot
- New file `deploy/README.md`: step-by-step install notes (create user, copy jar, install unit, `systemctl enable --now`)
- New capability spec: `specs/deployment/spec.md` covering unit-file validity, boot enablement, restart policy, journald integration, and SIGTERM-to-graceful-shutdown behaviour
- MODIFIED delta for `system-lifecycle`: extends the existing "Daemon Execution" requirement with systemd-specific scenarios (SIGTERM delivery, journal routing, boot start)

## Impact
- Affected specs: `deployment` (new), `system-lifecycle` (modified)
- Affected code/files:
  - `deploy/webhooks-router.service` (new, not a Java source file)
  - `deploy/README.md` (new)
- No Java source changes; no breaking changes to existing behaviour
