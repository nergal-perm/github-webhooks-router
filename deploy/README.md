# Deploying webhooks-router as a systemd service

## Prerequisites

- Ubuntu host with Java 21+ installed
- The project built into a fat-jar via `mvn package -DskipTests`

## Install steps

**1. Build the fat-jar**
```bash
mvn package -DskipTests
```

**2. Create the dedicated service user**
```bash
sudo useradd --system --no-create-home webhooks-router
```

**3. Create the install directory and copy the jar**
```bash
sudo mkdir -p /opt/webhooks-router
sudo cp target/router-*.jar /opt/webhooks-router/webhooks-router.jar
sudo chown -R webhooks-router:webhooks-router /opt/webhooks-router
```

**4. Create the working directory for data storage**
```bash
sudo mkdir -p /var/lib/webhooks-router
sudo chown webhooks-router: /var/lib/webhooks-router
```

**5. Install the unit file**
```bash
sudo cp deploy/webhooks-router.service /etc/systemd/system/
```

**6. Reload systemd and enable the service**
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now webhooks-router
```

**7. Verify it is running**
```bash
sudo systemctl status webhooks-router
journalctl -u webhooks-router -f
```

## Day-to-day operations

| Action | Command |
|---|---|
| Stop | `sudo systemctl stop webhooks-router` |
| Start | `sudo systemctl start webhooks-router` |
| Restart | `sudo systemctl restart webhooks-router` |
| Disable on boot | `sudo systemctl disable webhooks-router` |
| View logs | `journalctl -u webhooks-router -f` |

## Graceful shutdown

The service uses `KillSignal=SIGTERM` and `TimeoutStopSec=30`. When stopped,
systemd sends SIGTERM which triggers the daemon's shutdown hook — the scheduler
stops accepting new work and the DynamoDB client is closed cleanly. Any
in-progress agent subprocess is given time to finish before a hard kill.

## Validating the unit file

On an Ubuntu host with systemd:
```bash
systemd-analyze verify deploy/webhooks-router.service
```
