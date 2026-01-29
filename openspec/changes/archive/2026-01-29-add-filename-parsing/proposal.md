# Proposal: Add Filename Parsing

## Why
The Dispatcher needs to extract the repository name from webhook filenames without opening the files. Currently `WebhookFilename` can generate filenames but cannot parse them back into components. This blocks the Dispatcher's core scanning logic.

## What Changes
- Add static `parse(String filename)` method to `WebhookFilename` value object
- Parse filename string back into timestamp, repoName, and uniqueId components
- Validate filename format and throw `IllegalArgumentException` for malformed input

## Impact
- Affected specs: `storage` (File Naming Convention requirement)
- Affected code: `WebhookFilename.java`, new unit tests
- No new dependencies
