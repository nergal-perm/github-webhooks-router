# Proposal: Walking Skeleton & Build Infrastructure

## Goal
Establish the project's foundation by implementing a "walking skeleton" — a minimal, buildable, and deployable version of the daemon that runs on the target environment (Java 21, Systemd) and demonstrates basic liveness.

## Scope
1.  **Build System:** Initialize a Maven project structure.
2.  **Runtime:** Configure the application to run on Java 21.
3.  **Application Entry:** Create a minimal Java application (`Main.java`) that starts up and stays running.
4.  **Logging:** Integrate `slf4j` and `logback` to output timestamped "Hello, World" logs every minute.
5.  **Deployment:** Provide a `systemd` unit file for managing the daemon on Ubuntu.

## Non-Goals
-   DynamoDB connectivity (Deferred).
-   Webhook processing logic (Deferred).
-   Complex configuration management (Hardcoded/Minimal for now).
