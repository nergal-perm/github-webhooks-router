# Change: Add Basic Dispatcher

## Why
The storage layer is implemented but there's no mechanism to process the webhook files. We need a dispatcher that monitors the pending queue, launches agent subprocesses to handle webhooks, and manages file state transitions through the processing lifecycle.

## What Changes
- Add a `Dispatcher` component that runs on a scheduled interval (every 60 seconds)
- Scan the `pending` directory for webhook files (skip if agent is currently active)
- Extract repository name from webhook filename to determine target directory
- Read webhook file content to extract the prompt/issue description
- Change to repository directory (`~/Dev/<repo-name>`)
- Launch agent subprocess: `gemini -y "<webhook-content>"`
- Move files through state transitions: `pending` → `processing` → `completed` (or `failed`)
- Handle subprocess exit codes to determine success/failure
- Integrate dispatcher into the main daemon lifecycle with graceful shutdown

## Impact
- Affected specs: `system-lifecycle`, `storage` (minor), new `dispatcher` capability
- Affected code:
  - `Main.java` - integrate dispatcher scheduling (60-second interval)
  - New `Dispatcher.java` - core processing logic with early return if agent active
  - New `AgentProcess.java` - subprocess management with working directory support
  - `AppConfig.java` - add repository base directory configuration (`~/Dev`)
  - `WebhookFilename.java` - may need to extract repository name parsing logic
- This increment processes webhooks serially (one at a time). Concurrency control (one agent per repo) will be added in a future increment.
