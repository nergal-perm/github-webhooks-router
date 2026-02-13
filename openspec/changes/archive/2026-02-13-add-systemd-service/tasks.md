## 1. Unit File
- [x] 1.1 Create `deploy/` directory at the project root
- [x] 1.2 Write `deploy/webhooks-router.service` with the following sections:
  - `[Unit]`: Description, After=network.target
  - `[Service]`: Type=simple, User=webhooks-router, ExecStart pointing at the fat-jar, Restart=on-failure, StandardOutput=journal, StandardError=journal
  - `[Install]`: WantedBy=multi-user.target
- [x] 1.3 Verify the unit file passes `systemd-analyze verify deploy/webhooks-router.service` on an Ubuntu host (or note this as a manual check in the README)

## 2. Deployment Documentation
- [x] 2.1 Create `deploy/README.md` with ordered install steps:
  1. Build the fat-jar: `mvn package -DskipTests`
  2. Create the dedicated user: `sudo useradd --system --no-create-home webhooks-router`
  3. Copy the jar to `/opt/webhooks-router/webhooks-router.jar`
  4. Create the working directory: `sudo mkdir -p /var/lib/webhooks-router && sudo chown webhooks-router: /var/lib/webhooks-router`
  5. Copy the unit file: `sudo cp deploy/webhooks-router.service /etc/systemd/system/`
  6. Reload and enable: `sudo systemctl daemon-reload && sudo systemctl enable --now webhooks-router`
  7. Verify: `sudo systemctl status webhooks-router` and `journalctl -u webhooks-router -f`
- [ ] 2.2 Add a brief mention of the service file in the root `README.md` (or confirm it already links to `deploy/README.md`)
