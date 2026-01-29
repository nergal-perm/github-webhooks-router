# Proposal: Local Storage Layer

## Why
The daemon needs a reliable local buffer to decouple cloud webhook capture from local agent processing. Without persistent storage, webhooks would be lost during network outages or agent unavailability.

## What Changes
- Add `WebhookFilename` value object for file naming convention
- Add `LocalWebhookStore` service to orchestrate persistence
- Add `AppConfig` for storage path configuration
- Wire domain layer with existing `TaskRepository` infrastructure

## Scope
1.  **Directory Management:** Ensure the required folder `pending` exists at runtime.
2.  **Persistence Logic:** Implement the mechanism to write a raw JSON payload to the `pending` directory.
3.  **Data Contract:** Enforce the strict file naming convention required by the Producer-Consumer architecture.

## Architecture Approach
The implementation separates concerns into distinct layers:

- **Domain Layer:**
  - `WebhookFilename` (Value Object) - Encapsulates naming convention logic: timestamp formatting, repo name sanitization, UUID generation. Pure business rules, fully unit-testable without I/O.
  - `LocalWebhookStore` (Service) - Orchestrates persistence by composing `WebhookFilename` with the infrastructure layer.

- **Infrastructure Layer:**
  - `TaskRepository` (Interface) - Generic file persistence contract: `save(filename, content)`.
  - `FileSystemRepository` (Implementation) - Handles actual filesystem operations.

This separation allows:
- Testing naming rules without touching the filesystem
- Reusing `TaskRepository` for other persistence needs
- Clear boundaries between "what to save" and "how to save"

## Non-Goals
-   Reading/Polling from these directories (Dispatcher work).
-   Deleting or moving files (Dispatcher work).
-   AWS DynamoDB integration (Downloader work).
