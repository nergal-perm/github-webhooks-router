# github-webhooks-router
The ingestion workflow for github webhooks stored in DynamoDB. Gets the hook, dispatches it to an appropriate AI agent to handle.

## Configuration

### Storage Root
By default, webhook files are stored in the `data/` directory with the following structure:
- `data/pending/` - Newly downloaded webhooks awaiting processing
- `data/processing/` - Webhooks currently being processed by an agent
- `data/completed/` - Successfully processed webhooks
- `data/failed/` - Failed webhook processing attempts

### Repository Base Directory
The dispatcher expects all repositories to be checked out in `~/Dev/` directory. Each repository should be in a subdirectory named after the repository name extracted from the webhook filename.

Example:
- Webhook filename: `2026-01-29T12:00:00.000Z_my-repo_abc12345.json`
- Expected repository path: `~/Dev/my-repo/`

### Agent Executable
The dispatcher launches the `gemini` command (must be in PATH) with the webhook content as an argument:
```bash
cd ~/Dev/<repo-name>
gemini -y "<webhook-content>"
```

The agent subprocess:
- Receives the webhook content as a command-line argument
- Executes in the context of the repository directory
- Returns exit code 0 for success, non-zero for failure

## Deployment

To run the daemon as a systemd service on Ubuntu, see [`deploy/README.md`](deploy/README.md).

## Running the Daemon

Build and run:
```bash
mvn clean package
java -jar target/router-0.1.0-SNAPSHOT.jar
```

The daemon will:
- Check for pending webhooks every 60 seconds
- Process webhooks serially (one at a time)
- Log heartbeat messages every 60 seconds
