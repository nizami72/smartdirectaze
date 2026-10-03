#!/usr/bin/env bash
# Installs the daily dump download on this laptop (systemd user timer, no root needed):
# a few minutes after login and then every hour runs deploy/fetch-dump.sh, which works once a day.
#   bash deploy/install-fetch-dump.sh
#   systemctl --user list-timers smartdirect-fetch-dump.timer     # when it runs next
#   journalctl --user -u smartdirect-fetch-dump -n 20               # what it did
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
SCRIPT="$(cd "$(dirname "$0")" && pwd)/fetch-dump.sh"
UNITS="$HOME/.config/systemd/user"
mkdir -p "$UNITS"

cat > "$UNITS/smartdirect-fetch-dump.service" <<UNIT
[Unit]
Description=SmartDirect: take the nightly database dump from the server
After=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/bin/bash $SCRIPT
UNIT

cat > "$UNITS/smartdirect-fetch-dump.timer" <<UNIT
[Unit]
Description=SmartDirect: nightly dump to ~/.dumps/smartdirectaze, once a day when the computer is on

[Timer]
# Shortly after login, then hourly for days when the computer stays on past midnight
OnStartupSec=3min
OnCalendar=hourly
Persistent=true

[Install]
WantedBy=timers.target
UNIT

systemctl --user daemon-reload
systemctl --user enable --now smartdirect-fetch-dump.timer
systemctl --user list-timers smartdirect-fetch-dump.timer --no-pager
