# Project Context

## Purpose
**GitHub Webhooks Router (Daemon)**
A lightweight Java system daemon designed to bridge the gap between cloud-based webhook capture and a local, "not-always-online" AI coding agent.

**Core Workflow:**
1.  **Capture:** An AWS Lambda stores incoming GitHub webhooks in a DynamoDB table.
2.  **Fetch (Downloader):** This daemon connects to DynamoDB (when online), batches downloads all pending webhooks, writes them to local disk as JSON files, and deletes the remote records to ensure "at-least-once" delivery to the local machine.
3.  **Dispatch (Dispatcher):** The daemon monitors the local queue and spawns AI Agent subprocesses to handle the webhooks. It manages concurrency by ensuring only **one agent runs per repository** at a time, while allowing parallel agents across different repositories.

## Tech Stack
-   **Language:** Java 21+ (LTS).
-   **Framework:** None / Minimal. Pure Java standard library preferred for low memory footprint and fast startup.
-   **Dependencies:**
    -   `aws-java-sdk-dynamodb` (v2): For fetching webhooks.
    -   `jackson-databind`: For JSON parsing and serialization.
    -   `slf4j` + `logback`: For logging.
    -   `picocli` (Optional): For command-line argument parsing.
-   **Runtime:** Running as a `systemd` service on Ubuntu.

## Project Conventions

### Architecture Patterns
**The Asynchronous Buffering Daemon (Producer-Consumer)**

1.  **The Downloader Thread (Producer)**
    *   **Schedule:** Configurable (e.g., polling interval, "quiet hours" support to avoid polling at night).
    *   **Responsibility:**
        *   Poll DynamoDB.
        *   Serialize items to local disk in the `pending` folder.
        *   **Safety:** Delete from DynamoDB *only* after successful disk write.
    *   **File Naming:** `YYYY-MM-DDThh:mm:ss_reponame_uniqueId.json`.
        *   *Purpose:* Allows the Dispatcher to quickly identify the target repository without parsing the full JSON.

2.  **The Dispatcher Thread (Consumer)**
    *   **Responsibility:**
        *   Scan `pending` folder.
        *   Manage a registry of running Agent PIDs mapped to Repository Names.
        *   **Scheduling Logic:**
            *   Iterate through pending files.
            *   Extract `reponame` from filename.
            *   Check if `reponame` is currently locked (Agent running).
            *   If **Free**: Move file to `processing`, launch Agent subprocess, add to lock registry.
            *   If **Locked**: Skip and try the next file.
        *   **Reaping:** Periodically check subprocess status. On completion, move file to `completed` (or delete) and release the lock.

### Directory Structure
The daemon operates on a strict directory structure (configurable root, e.g., `/var/lib/webhooks-router/`):
-   `inbox/` (or `pending/`): Webhooks downloaded from AWS, waiting for processing.
-   `processing/`: Webhooks currently being handled by an Agent.
-   `completed/`: Successfully processed webhooks (optional archive).
-   `failed/`: Webhooks that caused an agent crash or failed to parse.
-   `skipped/`: Webhooks with unsupported event types (not processed by any agent).

### Supported Webhook Event Types
Only the following GitHub webhook event types trigger agent processing:
-   **`issues.opened`** — A new issue was opened.

All other event types are moved to the `skipped/` directory without processing.
This includes (but is not limited to):
-   `issues.closed`, `issues.edited`, `issues.labeled`, etc.
-   `pull_request.opened`, `pull_request.closed`, `pull_request.merged`, etc.
-   `push` (branch push events)
-   `create`, `delete` (branch/tag creation/deletion)
-   `issue_comment.created` and other comment events
-   Any unknown or malformed event structure

### Code Style
-   Standard Java naming conventions (CamelCase for classes/methods).
-   Explicit error handling (Checked exceptions where recovery is possible).
-   Immutable data structures where possible (Records).

## Domain Context
-   **"The Agent":** An external executable (e.g., a CLI tool or shell script) that takes a webhook JSON file path as an argument. It performs actual coding tasks.
-   **"Not-Always-Online":** The system must tolerate network outages. The *Downloader* might fail to connect, but the *Dispatcher* must continue working on locally buffered files.

## External Dependencies
-   **AWS DynamoDB:** Source of truth for incoming webhooks.
-   **Local AI Agent:** The downstream consumer of the webhooks (invoked via `ProcessBuilder`).

## Roadmap

Planned directions, not yet proposed or specced. Each item is a candidate for a future change proposal.

### 1. Pluggable Agent Backends
Currently the Dispatcher hardcodes a single agent executable (`gemini`). The goal is to make agent selection configurable and eventually dynamic.

-   Define an **Agent Configuration** model: a named agent entry with an executable path, argument template, and optional selector criteria.
-   The Dispatcher chooses which agent to invoke based on a **selection strategy**: static (configured per-repo or globally), or dynamic (chosen at dispatch time based on issue content or metadata).
-   Initial concrete use case: support `claude` as an alternative to `gemini`, selectable via configuration.

### 2. Pre-dispatch Task Analysis
Before handing a webhook to a full coding agent, run a **lightweight analysis step** to determine the best invocation parameters (agent choice, flags, context hints, etc.).

-   A fast/cheap LLM call reads the issue title and body and produces a structured decision: which agent backend to use, any extra prompt context to inject, estimated complexity, etc.
-   The analysis result is written alongside the webhook file (e.g., `<name>.analysis.json`) and consumed by the Dispatcher when building the agent subprocess command.
-   Must not block the Dispatcher if the analysis step is slow or fails — fall back to defaults.

### 3. Agent Action Reporting via GitHub Issue Comments
Agents should persist an audit trail of their actions as comments on the originating GitHub issue, making progress visible directly in the GitHub UI.

-   This is **not** a direct responsibility of the Downloader or Dispatcher; it is an agent-side concern.
-   The router's role is to ensure agents receive enough context to post comments: at minimum, the issue number and repository full name must be passed as invocation parameters (already present in the webhook JSON).
-   Agent configuration (see item 1) should include an optional `reporting` section that enables/disables comment posting and specifies the GitHub token or app credential to use.
-   Future: the Dispatcher could optionally post a "started processing" comment when it launches an agent, and a "completed" or "failed" comment when the subprocess exits.