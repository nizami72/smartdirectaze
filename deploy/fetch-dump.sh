#!/usr/bin/env bash
# Laptop side of the backups: once a day, whenever the computer is on, takes the newest nightly dump from the server
# into ~/.dumps/smartdirectaze/ and shows a desktop notification. Started by a systemd user timer
# (deploy/install-fetch-dump.sh): a few minutes after login and every hour; the hourly runs do nothing
# once today's check is done. A day starts at 00:00 local time.
#   bash deploy/fetch-dump.sh           # by hand: same check
#   bash deploy/fetch-dump.sh --force   # fetch again even if today's check is done
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
SERVER=root@157.180.16.28
REMOTE_DIR=/var/backups/smartdirect
LOCAL_DIR="$HOME/.dumps/smartdirectaze"
STAMP="$LOCAL_DIR/.checked"   # holds the date of the last successful check
SSH=(ssh -i "$HOME/.ssh/key2" -o BatchMode=yes -o ConnectTimeout=15)

notify() {
  # Desktop notification when a session is there; the line also goes to the journal
  command -v notify-send >/dev/null && notify-send -a SmartDirect -i "$2" "SmartDirect" "$1" 2>/dev/null || true
  echo "$1"
}

mkdir -p "$LOCAL_DIR"
TODAY=$(date +%F)
if [ "${1:-}" != "--force" ] && [ "$(cat "$STAMP" 2>/dev/null || true)" = "$TODAY" ]; then
  exit 0
fi

# Newest nightly dump on the server; no network = try again on the next hourly run
if ! LATEST=$("${SSH[@]}" "$SERVER" "ls -1t $REMOTE_DIR/smartdirect_*.sql.gz 2>/dev/null | head -1"); then
  echo "Server not reachable, will try again later"
  exit 0
fi
if [ -z "$LATEST" ]; then
  notify "No nightly dump on the server" dialog-warning
  exit 1
fi
NAME=$(basename "$LATEST")

if [ -f "$LOCAL_DIR/$NAME" ] && [ "${1:-}" != "--force" ]; then
  echo "$NAME is already here"
else
  rsync -a -e "${SSH[*]}" "$SERVER:$LATEST" "$LOCAL_DIR/$NAME.part"
  if ! gzip -t "$LOCAL_DIR/$NAME.part"; then
    rm -f "$LOCAL_DIR/$NAME.part"
    notify "Nightly dump $NAME is damaged, not saved" dialog-error
    exit 1
  fi
  mv "$LOCAL_DIR/$NAME.part" "$LOCAL_DIR/$NAME"
  notify "Nightly database dump saved: $NAME ($(du -h "$LOCAL_DIR/$NAME" | cut -f1)) in ~/.dumps/smartdirectaze" document-save
fi
echo "$TODAY" > "$STAMP"
