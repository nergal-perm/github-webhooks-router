# dispatcher Specification

## Purpose
Processes webhook files from the local pending queue by launching AI agent subprocesses. Manages per-repository concurrency (one agent per repo at a time, parallel across repos), file state transitions (pending → processing → completed/failed), and agent output capture.
## Requirements
### Requirement: Dispatcher Lifecycle
The system MUST run a dispatcher component that processes webhook files from the pending queue on a scheduled interval.

#### Scenario: Dispatcher Startup
Given the daemon application starts
When the dispatcher is initialized
Then the dispatcher begins checking the pending directory every 60 seconds.

#### Scenario: Dispatcher Shutdown
Given the dispatcher is running
When a SIGTERM signal is received
Then the dispatcher stops scheduling new scans
And any in-progress agent subprocess is allowed to complete or is terminated gracefully.

#### Scenario: Skip When Agent Active
Given the dispatcher's scheduled check occurs
When an agent subprocess is currently running
Then the dispatcher returns immediately without scanning the pending directory
And waits for the next scheduled interval.

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

### Requirement: Webhook Content Extraction
The dispatcher MUST extract repository name from the filename and read webhook content before launching the agent.

#### Scenario: Extract Repository Name
Given a webhook filename "2026-01-29T12:00:00.000Z_my-project_abc123.json"
When the dispatcher parses the filename
Then it extracts the repository name as "my-project".

#### Scenario: Read Webhook Content
Given a webhook file in processing directory
When the dispatcher prepares to launch the agent
Then it reads the file content as a UTF-8 string
And uses this content as the prompt argument for the agent command.

### Requirement: Agent Subprocess Execution
The dispatcher MUST launch an external agent executable as a subprocess to handle each webhook file and capture its output to a dedicated file.

#### Scenario: Successful Launch with Output Capture
Given a webhook file "2026-01-29T12:00:00.000Z_my-repo_abc123.json" exists in pending
And the file content is `{"issue": {"number": 42}, "comment": {"body": "Fix the login bug"}}`
When the dispatcher processes this file
Then it extracts the repository name "my-repo" from the filename
And it extracts the issue number 42 from the JSON payload
And moves the file to the processing directory
And changes the working directory to "~/Dev/my-repo"
And creates an output file at "outputs/my-repo_issue-42_2026-01-30T10:30:00.000Z.txt"
And launches the agent subprocess with command `gemini -y "{...}"`
And redirects subprocess stdout and stderr to the output file
And waits for the subprocess to complete.

#### Scenario: Repository Directory Not Found
Given a webhook file for repository "unknown-repo"
When the dispatcher attempts to change to directory "~/Dev/unknown-repo"
And the directory does not exist
Then an error is logged
And the webhook file is moved to the failed directory
And no output file is created.

#### Scenario: Agent Executable Not Found
Given the agent executable "gemini" is not in the system PATH
When the dispatcher attempts to launch the subprocess
Then an error is logged
And the webhook file is moved to the failed directory
And an output file is created with the error message.

#### Scenario: Output File Creation Failure
Given the outputs directory is not writable
When the dispatcher attempts to create an output file
Then an error is logged
And the webhook file is moved to the failed directory
And processing continues with the next webhook on subsequent scans.

### Requirement: File State Transitions
The dispatcher MUST move webhook files through state directories based on processing outcomes.

#### Scenario: Move to Processing
Given a webhook file exists in pending
When the dispatcher selects it for processing
Then the file is moved to the processing directory before launching the agent subprocess.

#### Scenario: Success - Move to Completed
Given an agent subprocess is handling a webhook file in processing
When the subprocess exits with exit code 0
Then the webhook file is moved from processing to completed.

#### Scenario: Failure - Move to Failed
Given an agent subprocess is handling a webhook file in processing
When the subprocess exits with a non-zero exit code
Then the webhook file is moved from processing to failed
And the exit code and any error output are logged.

### Requirement: Per-Repository Concurrency
The dispatcher MUST allow processing multiple webhooks in parallel if they target different repositories, while ensuring only one agent runs per repository at a time.

#### Scenario: Parallel Processing for Different Repos
Given the pending directory contains "repo1_abc.json" and "repo2_def.json"
When the dispatcher scans the queue
Then it launches an agent for "repo1"
And it simultaneously launches an agent for "repo2".

#### Scenario: Serial Processing for Same Repo
Given the pending directory contains "repo1_abc.json" and "repo1_def.json"
When the dispatcher scans the queue
Then it launches an agent for "repo1" to process "abc.json"
And it skips "def.json" until the first agent completes.

### Requirement: Error Handling
The dispatcher MUST handle errors gracefully without crashing the daemon.

#### Scenario: Subprocess Crash
Given an agent subprocess is running
When the subprocess crashes unexpectedly
Then the dispatcher logs the error
And moves the webhook file to the failed directory
And continues normal operation on the next scan interval.

#### Scenario: File System Error
Given a file move operation fails due to I/O error
When the dispatcher attempts to transition file state
Then the error is logged
And the dispatcher continues processing other files on subsequent scans.

### Requirement: Issue Number Extraction
The dispatcher MUST extract the issue number from webhook JSON payloads when present.

#### Scenario: GitHub Issue Webhook
Given a webhook payload contains `{"issue": {"number": 42}}`
When the dispatcher parses the webhook content
Then it extracts the issue number as 42.

#### Scenario: GitHub Pull Request Webhook
Given a webhook payload contains `{"pull_request": {"number": 123}}`
When the dispatcher parses the webhook content
Then it extracts the issue number as 123.

#### Scenario: Non-Issue Webhook
Given a webhook payload is a push event without issue or pull_request fields
When the dispatcher parses the webhook content
Then it returns no issue number (null or empty).

#### Scenario: Malformed JSON
Given a webhook payload contains invalid JSON
When the dispatcher attempts to parse the content
Then it logs a warning and continues processing without an issue number.

### Requirement: Agent Output File Naming
The dispatcher MUST generate unique output filenames for each agent session.

#### Scenario: With Issue Number
Given a webhook for repository "my-repo" with issue number 42
And the session starts at "2026-01-30T10:30:00.000Z"
When the dispatcher generates the output filename
Then the filename is "my-repo_issue-42_2026-01-30T10:30:00.000Z.txt".

#### Scenario: Without Issue Number
Given a webhook for repository "my-repo" with no issue number
And the session starts at "2026-01-30T10:30:00.000Z"
When the dispatcher generates the output filename
Then the filename is "my-repo_2026-01-30T10:30:00.000Z.txt".

#### Scenario: Filename Sanitization
Given a repository name contains special characters
When the dispatcher generates the output filename
Then special characters are handled consistently with webhook filename parsing rules.

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

