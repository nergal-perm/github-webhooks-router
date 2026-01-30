# Change: Route Agent Output to Files

## Why
Currently, agent subprocess output is inherited by the parent daemon process (via `.inheritIO()` in `AgentProcess.java:32`), which writes to the daemon's stdout. This makes it difficult to:
- Review what the agent did for a specific webhook
- Debug failed agent executions
- Archive agent session logs for audit trails
- Correlate agent output with the webhook that triggered it

## What Changes
- Parse webhook JSON payload to extract issue number (if present) from GitHub webhook structure
- Generate output filenames with pattern: `{reponame}_{issue-number}_{timestamp}.txt` (or `{reponame}_{timestamp}.txt` if no issue)
- Create `outputs/` directory under storage root to store agent session logs
- Redirect agent subprocess stdout and stderr to a single combined output file
- Update `AgentProcess.java` to use `ProcessBuilder.redirectOutput()` and `redirectError()` instead of `inheritIO()`
- Add output file path configuration to `AppConfig.java`

## Impact
- Affected specs: `dispatcher`, `storage`
- Affected code:
  - `AppConfig.java` - add `outputsDir()` method
  - `AgentProcess.java` - replace `.inheritIO()` with file redirection, add issue number parameter
  - `Dispatcher.java` - parse webhook JSON to extract issue number, pass to `AgentProcess`
  - May need JSON parsing utility or leverage existing Jackson dependency
- This change enables persistent logging of agent sessions without changing the core dispatch flow
