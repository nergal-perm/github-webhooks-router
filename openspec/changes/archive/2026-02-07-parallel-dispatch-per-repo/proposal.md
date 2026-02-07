# Change: Parallel Dispatch per Repository

## Why
Currently, the Dispatcher uses a global `agentActive` lock, which allows only one agent process to run at a time across all repositories. The project vision (as stated in `project.md`) requires allowing parallel agent executions as long as they are targeting different repositories. This increases throughput and ensures that a long-running task in one repo doesn't block critical tasks in another.

## What Changes
- Replace the global `AtomicBoolean agentActive` with a `Set<String> activeRepos` to track repositories currently being processed.
- Update `Dispatcher.dispatch()` to iterate through all pending files instead of just the first one.
- For each pending file:
    - Extract the repository name from the filename.
    - Check if the repository is already being processed.
    - If free, start the agent process in a separate thread (or just manage multiple processes).
- Actually, since `dispatch()` is called on a schedule, we can either:
    - Start processes and let them run in the background, and have the dispatcher check on them in the next cycle.
    - Or use an ExecutorService to manage a pool of agent processes.
- For simplicity and following the spec, we'll keep the scheduled `dispatch()` but allow it to launch multiple processes if they are for different repos.
- We need a way to track active processes so we can "reap" them when they finish.

## Impact
- `Dispatcher.java`: Significant logic update to handle multiple active processes.
- `AgentProcess.java`: May need to return a `CompletableFuture` or the `Dispatcher` needs to manage the `Process` objects.
- `spec.md`: Update "Requirement: Serial Processing" to "Requirement: Per-Repository Concurrency".
