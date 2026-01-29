# Tasks: Add Filename Parsing

## 1. Implementation
- [x] Add `parse(String filename)` static method to `WebhookFilename`.
    - [x] Extract timestamp from filename prefix.
    - [x] Extract repository name from middle segment.
    - [x] Extract unique ID from suffix (before `.json`).
    - [x] Throw `IllegalArgumentException` for malformed filenames.

## 2. Unit Tests
- [x] Test successful parsing of valid filename.
    - [x] Assert extracted timestamp matches expected `Instant`.
    - [x] Assert extracted repo name matches expected value.
    - [x] Assert extracted unique ID matches expected value.
- [x] Test rejection of malformed filenames.
    - [x] Missing `.json` extension.
    - [x] Wrong number of underscore-separated segments.
    - [x] Invalid timestamp format.
