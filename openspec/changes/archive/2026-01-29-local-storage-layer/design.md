# Design: Local Storage Layer

## Architecture: Separation of Concerns

The storage layer separates **business logic** (naming conventions, domain rules) from **infrastructure** (filesystem I/O):

```
┌─────────────────────────────────────────────────────────┐
│  Domain Layer                                           │
│  ┌─────────────────┐    ┌─────────────────────────────┐ │
│  │ WebhookFilename │    │ LocalWebhookStore           │ │
│  │ (Value Object)  │◄───│ save(repoName, payload)     │ │
│  │ - sanitization  │    │ - orchestrates persistence  │ │
│  │ - formatting    │    └──────────────┬──────────────┘ │
│  └─────────────────┘                   │                │
└────────────────────────────────────────┼────────────────┘
                                         │ delegates
┌────────────────────────────────────────┼────────────────┐
│  Infrastructure Layer                  ▼                │
│  ┌─────────────────────────────────────────────────────┐│
│  │ TaskRepository (interface)                          ││
│  │ save(filename, content) → Path                      ││
│  └─────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────┐│
│  │ FileSystemRepository (implementation)               ││
│  │ - creates directories                               ││
│  │ - writes files to disk                              ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

**Benefits:**
- `WebhookFilename` is pure logic, 100% unit-testable without mocks
- `TaskRepository` has no domain knowledge, reusable for other persistence needs
- `LocalWebhookStore` composes both, easy to test with a fake repository

## Directory Structure
The application will assert ownership of a root storage directory (configurable via `AppConfig`, defaulting to `./data` for dev).

```text
{storage-root}/
├── pending/      # [Inbox] New webhooks waiting for dispatch
├── processing/   # [Locked] Currently being handled by an agent
├── completed/    # [Archive] Successfully processed
└── failed/       # [Dead Letter] Failed to parse or process
```

## Value Object: `WebhookFilename`

An immutable record encapsulating the file naming convention. Responsible for:
- Sanitizing repository names (replace `/` and invalid chars with `-`)
- Formatting timestamps to ISO-8601 UTC
- Generating unique suffixes (UUID)
- Assembling the final filename string

### API
```java
public record WebhookFilename(Instant timestamp, String repoName, String uniqueId) {
    // Factory method for typical usage
    public static WebhookFilename create(String repoName);

    // Returns sanitized repo name (alphanumeric + dashes only)
    public String sanitizedRepoName();

    // Returns the full filename string
    public String toFilename();
}
```

### File Naming Convention
Files must be named to allow sorting by time and filtering by repository without opening the file.

Format: `{ISO-8601-Timestamp}_{Sanitized-RepoName}_{UUID}.json`

*   **Timestamp:** `YYYY-MM-DDThh:mm:ss.SSSZ` (UTC)
*   **RepoName:** Sanitized (alphanumeric + dashes only) to prevent filesystem issues.
*   **UUID:** Short random suffix (8 chars) to prevent collisions during high-volume bursts.

**Example:**
`2026-01-24T10:15:30.000Z_gemini-cli_a1b2c3d4.json`

## Component: `LocalWebhookStore`
A domain service that orchestrates webhook persistence by composing `WebhookFilename` and `TaskRepository`.

### API
```java
public interface WebhookStore {
    /**
     * Persists a webhook payload to the pending queue.
     * @param repoName The repository name (used for concurrency locking downstream)
     * @param jsonPayload The raw JSON content
     * @return The path to the saved file
     */
    Path save(String repoName, String jsonPayload);
}
```

## Configuration
*   **Root Path:** Defined in `AppConfig` (initially hardcoded/env var).
*   **Initialization:** On application startup (constructor or `@PostConstruct`), the store checks and creates the 4 subdirectories if they don't exist.
