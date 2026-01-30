# dispatcher Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

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
