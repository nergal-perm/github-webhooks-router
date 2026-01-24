# GEMINI.md - Project Context

## Project Overview
**GitHub Webhooks Router (Daemon)**
This project is a lightweight Java system daemon designed to bridge the gap between cloud-based webhook capture (AWS DynamoDB) and a local, "not-always-online" AI coding agent.

It ensures that webhooks are captured even when the local machine is offline, buffered to disk, and then processed by local AI agents in a controlled, concurrency-safe manner.

## Architecture: "The Asynchronous Buffering Daemon"
The system implements a Producer-Consumer pattern with two main threads:

1.  **Downloader (Producer):**
    *   Polls AWS DynamoDB for new webhooks.
    *   Downloads and saves them to a local `pending/` directory.
    *   Deletes from DynamoDB only *after* successful local storage (At-least-once delivery).
    *   File naming convention: `YYYY-MM-DDThh:mm:ss_reponame_uniqueId.json`.

2.  **Dispatcher (Consumer):**
    *   Monitors the `pending/` directory.
    *   Dispatches webhooks to a local AI Agent (subprocess).
    *   **Concurrency Control:** Ensures only one agent runs per repository at a time.
    *   Moves files to `processing/`, `completed/`, or `failed/` based on outcome.

## Tech Stack
*   **Language:** Java 21+ (LTS)
*   **Framework:** Minimal / Pure Java (to minimize memory footprint).
*   **Key Libraries:**
    *   `aws-java-sdk-dynamodb` (v2)
    *   `jackson-databind` (JSON processing)
    *   `slf4j` + `logback` (Logging)
*   **Runtime:** Systemd service on Ubuntu.

## Current Status
*   **Phase:** Design & Specification.
*   **Build System:** To be determined (likely Maven or Gradle, pending scaffold).
*   **Source Code:** Not yet initialized.

## Development Workflow (OpenSpec)
This project uses **OpenSpec** for spec-driven development.
*   **Specs:** `openspec/specs/` contains the source of truth for requirements.
*   **Changes:** Work is planned and tracked via proposals in `openspec/changes/`.
*   **Project Context:** `openspec/project.md` contains detailed conventions and architectural decisions.

## key Commands (Predicted)
*   `openspec list` - View active change proposals.
*   `openspec validate` - Validate specs and proposals.
*   *(Future)* `./mvnw clean install` - Build the daemon.
