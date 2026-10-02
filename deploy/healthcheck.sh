#!/usr/bin/env bash
# Runs every minute on the server (cron). Tells the operator in Telegram when SmartDirect stops working
# and when it is back. Alerts after 2 failed checks in a row (a restart during a deploy is not an alert).
set -uo pipefail
ENV_FILE=/etc/smartdirect/smartdirect.env
STATE=/var/lib/smartdirect-health
mkdir -p "$STATE"
TOKEN=$(grep '^SD_TELEGRAM_BOT_TOKEN=' "$ENV_FILE" | cut -d= -f2-)
CHAT=$(grep '^SD_TELEGRAM_ADMIN_ID=' "$ENV_FILE" | cut -d= -f2-)

problems=()
curl -sf -m 10 https://smartdirect.qrfood.az/webhooks/w/alive >/dev/null || problems+=("сайт или бэкенд не отвечает")
docker exec smartdirect-db pg_isready -q -U smartdirect >/dev/null 2>&1 || problems+=("база данных не отвечает")
disk=$(df --output=pcent / | tail -1 | tr -dc '0-9')
[ "${disk:-0}" -lt 90 ] || problems+=("диск заполнен на ${disk}%")

notify() {
  curl -s -m 10 "https://api.telegram.org/bot${TOKEN}/sendMessage" \
    --data-urlencode "chat_id=${CHAT}" --data-urlencode "text=$1" >/dev/null || true
}

fails=$(cat "$STATE/fails" 2>/dev/null || echo 0)
alerted=$(cat "$STATE/alerted" 2>/dev/null || echo 0)
if [ ${#problems[@]} -gt 0 ]; then
  fails=$((fails + 1)); echo "$fails" > "$STATE/fails"
  if [ "$fails" -ge 2 ] && [ "$alerted" = 0 ]; then
    notify "🔴 SmartDirect: $(IFS=';'; echo "${problems[*]}" | sed 's/;/; /g'). Проверьте: journalctl -u smartdirect -n 50"
    echo 1 > "$STATE/alerted"
  fi
else
  echo 0 > "$STATE/fails"
  if [ "$alerted" = 1 ]; then
    notify "🟢 SmartDirect снова работает."
    echo 0 > "$STATE/alerted"
  fi
fi
