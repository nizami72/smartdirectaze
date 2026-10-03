#!/usr/bin/env bash
# Restores the SmartDirect database on the server from a dump: a nightly one on the server
# or a copy from this laptop (~/.dumps/smartdirectaze). Run on the laptop from the repo root:
#   bash deploy/restore-db.sh                                   # lists the dumps
#   bash deploy/restore-db.sh smartdirect_2026-10-02.sql.gz     # restores this one (asks to confirm)
#   RESTORE_CONFIRM=smartdirect.qrfood.az bash deploy/restore-db.sh smartdirect_2026-10-02.sql.gz
# The current database is dumped first (before-restore_*.sql.gz), so a restore can itself be undone.
# The site is down for the restore itself, usually 15-30 seconds.
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
SERVER=root@157.180.16.28
SITE=smartdirect.qrfood.az
REMOTE_DIR=/var/backups/smartdirect
LOCAL_DIR="$HOME/.dumps/smartdirectaze"
SSH=(ssh -i "$HOME/.ssh/key2" -o BatchMode=yes)

NAME=${1:-}
if [ -z "$NAME" ]; then
  echo "On the server ($REMOTE_DIR):"
  "${SSH[@]}" "$SERVER" "ls -1t $REMOTE_DIR/*.sql.gz | xargs -n1 basename" | sed 's/^/  /'
  echo "On this laptop ($LOCAL_DIR):"
  ls -1t "$LOCAL_DIR"/*.sql.gz 2>/dev/null | xargs -rn1 basename | sed 's/^/  /'
  echo
  echo "Restore one:  bash deploy/restore-db.sh <file name>"
  exit 0
fi
NAME=$(basename "$NAME")

# Only on the laptop: upload it first (the server keeps just the 4 newest nightly dumps)
if ! "${SSH[@]}" "$SERVER" "[ -f $REMOTE_DIR/$NAME ]"; then
  [ -f "$LOCAL_DIR/$NAME" ] || { echo "$NAME is neither on the server nor in $LOCAL_DIR, nothing changed."; exit 1; }
  gzip -t "$LOCAL_DIR/$NAME" || { echo "$LOCAL_DIR/$NAME is damaged, nothing changed."; exit 1; }
  echo "Uploading $NAME from this laptop..."
  rsync -a -e "${SSH[*]}" "$LOCAL_DIR/$NAME" "$SERVER:$REMOTE_DIR/$NAME"
fi

CONFIRM=${RESTORE_CONFIRM:-}
if [ -z "$CONFIRM" ]; then
  read -r -p "The database on $SITE will be REPLACED by $NAME. Type the site name to confirm: " CONFIRM
fi
[ "$CONFIRM" = "$SITE" ] || { echo "Not confirmed, nothing changed."; exit 1; }

"${SSH[@]}" "$SERVER" bash -s -- "$REMOTE_DIR/$NAME" <<'REMOTE'
set -euo pipefail
DUMP=$1
PSQL="docker exec -i smartdirect-db psql -q -v ON_ERROR_STOP=1 -U smartdirect"
gzip -t "$DUMP" || { echo "$DUMP is damaged, nothing changed."; exit 1; }

SAFETY=/var/backups/smartdirect/before-restore_$(date +%F_%H%M%S).sql.gz
docker exec smartdirect-db pg_dump -U smartdirect smartdirect_db | gzip > "$SAFETY"
echo "current database saved: $SAFETY"

echo "stopping the site..."
systemctl stop smartdirect
# Whatever happens below, the site is started again
trap 'systemctl start smartdirect' EXIT
$PSQL -d postgres -c "DROP DATABASE smartdirect_db WITH (FORCE)" -c "CREATE DATABASE smartdirect_db OWNER smartdirect"
if ! gunzip -c "$DUMP" | $PSQL -d smartdirect_db >/dev/null; then
  echo "RESTORE FAILED: putting the previous database back from $SAFETY"
  $PSQL -d postgres -c "DROP DATABASE smartdirect_db WITH (FORCE)" -c "CREATE DATABASE smartdirect_db OWNER smartdirect"
  gunzip -c "$SAFETY" | $PSQL -d smartdirect_db >/dev/null
  exit 1
fi
echo "restored from $(basename "$DUMP"): $(docker exec smartdirect-db psql -U smartdirect -d smartdirect_db -Atc \
  "select (select count(*) from users) || ' users, ' || (select count(*) from shops) || ' shops, ' || (select count(*) from products) || ' products, ' || (select count(*) from orders) || ' orders'")"

echo -n "starting the site"
trap - EXIT
systemctl start smartdirect
for i in $(seq 1 60); do
  curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null && { echo; echo "site is up"; exit 0; }
  echo -n "."; sleep 2
done
echo; echo "site did not start in 2 minutes:"; journalctl -u smartdirect -n 30 --no-pager; exit 1
REMOTE
