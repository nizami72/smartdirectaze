#!/usr/bin/env bash
# DEBUG ONLY: wipes SmartDirect on the server to a fresh install (empty database, no product photos).
# Works only with your SSH key and only while /etc/smartdirect/allow-reset exists on the server.
# Close it for good:  ssh -i ~/.ssh/key2 root@157.180.16.28 rm /etc/smartdirect/allow-reset
#
#   bash deploy/reset-server.sh            # asks to type the site name
#   RESET_CONFIRM=smartdirect.qrfood.az bash deploy/reset-server.sh
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
SERVER=root@157.180.16.28
SITE=smartdirect.qrfood.az
SSH="ssh -i $HOME/.ssh/key2 -o BatchMode=yes"

CONFIRM=${RESET_CONFIRM:-}
if [ -z "$CONFIRM" ]; then
  read -r -p "This deletes ALL SmartDirect data on $SITE. Type the site name to confirm: " CONFIRM
fi
[ "$CONFIRM" = "$SITE" ] || { echo "Not confirmed, nothing changed."; exit 1; }

$SSH "$SERVER" bash -s <<'REMOTE'
set -euo pipefail
[ -f /etc/smartdirect/allow-reset ] || { echo "Reset is closed on this server (/etc/smartdirect/allow-reset missing)."; exit 1; }

BACKUP=/var/backups/smartdirect/before-reset_$(date +%F_%H%M%S).sql.gz
docker exec smartdirect-db pg_dump -U smartdirect smartdirect_db | gzip > "$BACKUP"
echo "backup: $BACKUP ($(du -h "$BACKUP" | cut -f1))"

systemctl stop smartdirect
docker exec smartdirect-db psql -q -U smartdirect -d smartdirect_db -c "SET client_min_messages TO warning; DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
find /opt/smartdirect/photo -mindepth 1 -delete
systemctl start smartdirect

# Hibernate creates the empty tables on start
for i in $(seq 1 60); do curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null && break; sleep 2; done
curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null || { journalctl -u smartdirect -n 30 --no-pager; exit 1; }
echo "fresh: $(docker exec smartdirect-db psql -U smartdirect -d smartdirect_db -Atc "select (select count(*) from users) || ' users, ' || (select count(*) from shops) || ' shops'")"
REMOTE
echo "Done. Create the admin account again: bash deploy/create-admin.sh (the site refuses admin emails)."
