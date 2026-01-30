# Implementation Tasks - Parallel Dispatch per Repository

## 1. Dispatcher Refactoring
- [ ] 1.1 Replace `AtomicBoolean agentActive` with a thread-safe set (e.g., `ConcurrentHashMap.newKeySet()`) for `activeRepos`.
- [ ] 1.2 Update `dispatch()` to loop through all pending files from the repository.
- [ ] 1.3 Implement logic to skip files if their repository is in `activeRepos`.
- [ ] 1.4 Launch agent processes in background threads so `dispatch()` doesn't block.

## 2. Process Management
- [ ] 2.1 Update `AgentProcess.execute` to return the `Process` object or a handle that can be monitored.
- [ ] 2.2 Implement a mechanism in `Dispatcher` to "reap" finished processes and release repository locks.
- [ ] 2.3 Ensure file movement to `completed`/`failed` happens after the process terminates.

## 3. Testing
- [ ] 3.1 Add test case for multiple repositories being processed in parallel.
- [ ] 3.2 Add test case for multiple webhooks for the SAME repository being processed serially.
- [ ] 3.3 Verify no race conditions in repository locking/unlocking.

## 4. Documentation
- [ ] 4.1 Update `openspec/specs/dispatcher/spec.md` to reflect per-repository concurrency.
