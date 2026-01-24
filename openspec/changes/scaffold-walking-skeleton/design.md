# Design: Walking Skeleton

## Directory Structure
Standard Maven layout is adopted to ensure compatibility with standard Java tooling.

```text
/
├── pom.xml                 # Build configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gemini/webhooks/router/
│   │   │       └── Main.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/               # Empty for now
└── dist/
    └── webhooks-router.service # Systemd unit file example
```

## Dependencies
Minimal set to satisfy the requirements:
-   `ch.qos.logback:logback-classic`: For logging implementation.
-   `org.slf4j:slf4j-api`: Facade.

## Application Logic
The application will act as a simple long-running service.

**Main Class (`Main.java`):**
1.  Initialize a `ScheduledExecutorService` (size 1).
2.  Schedule a task to run every 60 seconds.
3.  The task performs `logger.info("Hello, world. The time is {}", Instant.now())`.
4.  Add a `Runtime.getRuntime().addShutdownHook` to gracefully shut down the executor.

## Deployment (Systemd)
A standard unit file `webhooks-router.service` will be provided.
-   **Type:** `simple`
-   **ExecStart:** `/usr/bin/java -jar /path/to/daemon.jar`
-   **Restart:** `on-failure`
-   **User:** Dedicated service user (placeholder).
