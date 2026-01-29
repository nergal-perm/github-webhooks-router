# Tasks: Local Storage Layer

## 1. Specification
- [x] Define `Storage Requirements` in `openspec/specs/storage/spec.md`.
    - [x] Directory Initialization requirement.
    - [x] Webhook Persistence requirement.
    - [x] File Naming Convention requirement.

## 2. Domain Layer: `WebhookFilename` Value Object
- [x] Create `WebhookFilename` record in `com.gemini.webhooks.router.domain`.
    - [x] Constructor with `timestamp`, `repoName`, `uniqueId`.
    - [x] Factory method `create(repoName)` using current time + generated UUID.
    - [x] Method `sanitizedRepoName()` - replace `/` and invalid chars with `-`.
    - [x] Method `toFilename()` - assemble full filename string.
- [x] Unit tests for `WebhookFilename` (no mocks, pure logic).
    - [x] Assert filename format matches `{timestamp}_{repo}_{uuid}.json`.
    - [x] Assert sanitization of `owner/repo-name` → `owner-repo-name`.
    - [x] Assert timestamp is ISO-8601 UTC format.

## 3. Infrastructure Layer: `TaskRepository` (already exists)
- [x] Interface `TaskRepository.save(filename, content)`.
- [x] Implementation `FileSystemRepository` with directory creation.
- [x] Integration test proving file is written correctly.

## 4. Domain Layer: `LocalWebhookStore` Service
- [x] Create `LocalWebhookStore` implementing `WebhookStore` interface.
    - [x] Compose `WebhookFilename` + `TaskRepository`.
    - [x] Implement `save(repoName, payload)` orchestration.
- [x] Unit tests with fake/mock `TaskRepository`.
    - [x] Assert correct filename passed to repository.
    - [x] Assert payload passed unchanged.

## 5. Configuration
- [x] Create `AppConfig` to hold `storageRoot` path.
- [x] Wire `LocalWebhookStore` with configured pending directory path.
