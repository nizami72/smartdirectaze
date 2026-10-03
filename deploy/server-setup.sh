#!/usr/bin/env bash
# One-time server preparation for SmartDirect. Run on the server as root:
#   DB_PASSWORD=... bash server-setup.sh
# Expects smartdirect.service, backup-db.sh and nginx/smartdirect.qrfood.az.conf next to it (deploy/ copied to /tmp/sd-deploy).
set -euo pipefail
: "${DB_PASSWORD:?set DB_PASSWORD}"
HERE=$(cd "$(dirname "$0")" && pwd)
DOMAIN=smartdirect.qrfood.az

# App user and directories
id smartdirect >/dev/null 2>&1 || useradd --system --home /opt/smartdirect --shell /usr/sbin/nologin smartdirect
mkdir -p /opt/smartdirect/photo /opt/smartdirect/logs /etc/smartdirect /var/www/$DOMAIN/dist /var/backups/smartdirect
chown -R smartdirect:smartdirect /opt/smartdirect

# Postgres in its own container, only on localhost, memory capped
if ! docker ps -a --format '{{.Names}}' | grep -qx smartdirect-db; then
  docker run -d --name smartdirect-db --restart unless-stopped --memory 256m \
    -p 127.0.0.1:5433:5432 -v smartdirect_pgdata:/var/lib/postgresql/data \
    -e POSTGRES_USER=smartdirect -e POSTGRES_PASSWORD="$DB_PASSWORD" -e POSTGRES_DB=smartdirect_db \
    postgres:16-alpine -c shared_buffers=64MB -c max_connections=30
fi

# Service (started by deploy.sh once the jar is uploaded)
install -m 644 "$HERE/smartdirect.service" /etc/systemd/system/smartdirect.service
systemctl daemon-reload
systemctl enable smartdirect.service

# Daily database backup at 02:30, 14 days kept
install -m 755 "$HERE/backup-db.sh" /opt/smartdirect/backup-db.sh
( crontab -l 2>/dev/null | grep -v "/opt/smartdirect/backup-db.sh"; echo "30 3 * * * /opt/smartdirect/backup-db.sh" ) | crontab -

# Health check every minute, Telegram alert to the operator
install -m 755 "$HERE/healthcheck.sh" /opt/smartdirect/healthcheck.sh
( crontab -l 2>/dev/null | grep -v "/opt/smartdirect/healthcheck.sh"; echo "* * * * * /opt/smartdirect/healthcheck.sh" ) | crontab -

# Nginx site: needs the certificate (see deploy/README.md)
install -m 644 "$HERE/nginx/$DOMAIN.conf" /etc/nginx/sites-available/$DOMAIN.conf
echo "Server prepared. Next: certificate, env file, deploy.sh"
