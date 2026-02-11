# Change: Add Webhook Event Type Filtering

## Why
The dispatcher currently processes all webhook files blindly regardless of event type. The system receives webhooks for many GitHub event types (issues, PRs, pushes, comments, etc.), but only "issues opened" should trigger an agent subprocess for now. All other event types should be moved to a `skipped/` directory so they are clearly separated from processed, failed, or completed webhooks.

## What Changes
- **Document supported webhook event types** in project.md for reference
- **Add webhook event type detection** via JSON payload inspection in `WebhookParser` (check `action` field and top-level object structure)
- **Add event filtering to Dispatcher** — only "issues opened" webhooks proceed to agent dispatch; all others are moved to `skipped/`
- **Add `skipped/` directory** to storage layer (`AppConfig`, `TaskRepository` initialization)

## Impact
- Affected specs: `dispatcher`, `storage`
- Affected code: `WebhookParser.java`, `Dispatcher.java`, `AppConfig.java`, `project.md`
