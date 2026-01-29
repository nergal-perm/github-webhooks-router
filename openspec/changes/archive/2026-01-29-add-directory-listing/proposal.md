# Proposal: Add Directory Listing

## Why
The Dispatcher needs to scan the `pending/` folder to discover webhooks waiting for processing. Without listing capability, the daemon cannot begin its consumer workflow.

## What Changes
- Add `list()` method to `TaskRepository` interface
- Implement in `FileSystemRepository` using `Files.list()`
- Return filenames (not full paths) to maintain consistency with existing `save()` contract

## Impact
- Affected specs: `storage` (new Directory Listing requirement)
- Affected code: `TaskRepository.java`, `FileSystemRepository.java`
- No new dependencies
