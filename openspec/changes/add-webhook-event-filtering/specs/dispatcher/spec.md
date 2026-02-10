## ADDED Requirements
### Requirement: Webhook Event Type Detection
The dispatcher MUST determine the webhook event type by inspecting the JSON payload structure and `action` field.

#### Scenario: Issues Opened Event
Given a webhook payload contains `{"action": "opened", "issue": {"number": 1}}`
When the dispatcher detects the event type
Then it identifies the event as "issues.opened".

#### Scenario: Issues Closed Event
Given a webhook payload contains `{"action": "closed", "issue": {"number": 1}}`
When the dispatcher detects the event type
Then it identifies the event as "issues.closed".

#### Scenario: Pull Request Event
Given a webhook payload contains `{"action": "opened", "pull_request": {"number": 5}}`
When the dispatcher detects the event type
Then it identifies the event as "pull_request.opened".

#### Scenario: Push Event
Given a webhook payload contains `{"ref": "refs/heads/main", "commits": [...]}`
And the payload has no `action` field
When the dispatcher detects the event type
Then it identifies the event as "push".

#### Scenario: Comment Event
Given a webhook payload contains `{"action": "created", "comment": {...}, "issue": {"number": 1}}`
When the dispatcher detects the event type
Then it identifies the event as "issue_comment.created".

#### Scenario: Malformed JSON
Given a webhook payload contains invalid JSON
When the dispatcher attempts to detect the event type
Then it returns an unknown event type
And logs a warning.

### Requirement: Webhook Event Filtering
The dispatcher MUST only process webhooks of type "issues.opened". All other webhook event types MUST be moved to the `skipped/` directory without launching an agent subprocess.

#### Scenario: Issues Opened Dispatched
Given a webhook file contains an "issues.opened" event payload
When the dispatcher processes the pending queue
Then the webhook is dispatched to the agent subprocess normally
And follows the standard pending → processing → completed/failed flow.

#### Scenario: Other Event Types Skipped
Given a webhook file contains a "push" event payload
When the dispatcher processes the pending queue
Then the webhook file is moved from `pending/` to `skipped/`
And no agent subprocess is launched
And an info log indicates the file was skipped with its event type.

#### Scenario: Unknown Event Type Skipped
Given a webhook file contains a payload that cannot be classified
When the dispatcher processes the pending queue
Then the webhook file is moved from `pending/` to `skipped/`
And no agent subprocess is launched.

## MODIFIED Requirements
### Requirement: Pending Queue Scanning
The dispatcher MUST scan the pending directory for webhook files to process.

#### Scenario: Files Available
Given the pending directory contains files "a.json" and "b.json"
When the dispatcher scans the queue
Then it identifies both files as candidates for processing.

#### Scenario: Empty Queue
Given the pending directory is empty
When the dispatcher scans the queue
Then no processing occurs and the dispatcher waits for the next scheduled interval.

#### Scenario: Mixed Event Types
Given the pending directory contains an "issues.opened" webhook and a "push" webhook
When the dispatcher scans the queue
Then it dispatches the "issues.opened" webhook to the agent
And moves the "push" webhook to the `skipped/` directory.
