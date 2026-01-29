# Change: Add File Movement Between Directories

## Why
The Dispatcher thread needs to move webhook files through the processing pipeline: `pending/` → `processing/` → `completed/` (or `failed/`). Without this capability, the system cannot track which webhooks are actively being processed or archive completed work.

## What Changes
- Add `move` operation to storage layer that relocates a file from one directory to another
- Support all directory transitions required by the dispatcher workflow
- Preserve filename during move (only directory changes)

## Impact
- Affected specs: `storage`
- Affected code: `LocalWebhookStore.java`, `TaskRepository.java`
