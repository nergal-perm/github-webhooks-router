## 1. Documentation
- [ ] 1.1 Add full list of supported GitHub webhook event types to `openspec/project.md`

## 2. Storage Layer
- [ ] 2.1 Add `skippedDir()` method to `AppConfig`
- [ ] 2.2 Ensure `skipped/` directory is created on startup (same pattern as other directories)

## 3. Webhook Event Detection
- [ ] 3.1 Add `extractEventType(String webhookJson)` method to `WebhookParser` that returns an event type string (e.g., `"issues.opened"`, `"push"`, `"pull_request.closed"`) by inspecting the JSON payload structure and `action` field
- [ ] 3.2 Write tests for event type detection covering: issues opened, issues with other actions, pull requests, pushes, comments, and malformed JSON

## 4. Dispatcher Filtering
- [ ] 4.1 Add event type check in `Dispatcher.dispatch()` — after parsing filename but before acquiring repo lock, read the webhook content and check its event type
- [ ] 4.2 If the event type is NOT `"issues.opened"`, move the file to `skipped/` and continue to next file
- [ ] 4.3 Write tests for filtering: issues-opened dispatches normally, other event types are moved to skipped
