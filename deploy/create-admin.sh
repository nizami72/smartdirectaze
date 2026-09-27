#!/usr/bin/env bash
# Creates the platform admin account on the server. Asks only for a new password.
# Works only with your SSH key: the site itself refuses to register admin emails.
#   bash deploy/create-admin.sh                  # nizami.budagov@gmail.com
#   bash deploy/create-admin.sh other@admin.az   # another email from ADMIN_EMAILS
set -euo pipefail
SERVER=root@157.180.16.28
SSH="ssh -i $HOME/.ssh/key2 -o BatchMode=yes"
EMAIL=${1:-nizami.budagov@gmail.com}

read -r -s -p "New password for $EMAIL: " P1; echo
read -r -s -p "Repeat: " P2; echo
[ "$P1" = "$P2" ] || { echo "Passwords differ, nothing changed."; exit 1; }
[ ${#P1} -ge 10 ] || { echo "At least 10 characters, nothing changed."; exit 1; }

# On the server: the app registers the admin itself (password hashed like for everyone),
# with the bootstrap secret from the root-only env file. The password travels on stdin only.
REMOTE='set -euo pipefail
HDR=$(mktemp); trap "rm -f $HDR" EXIT; chmod 600 $HDR
echo "X-Admin-Bootstrap: $(grep "^ADMIN_BOOTSTRAP_TOKEN=" /etc/smartdirect/smartdirect.env | cut -d= -f2-)" > $HDR
curl -s -o /tmp/sd-admin-resp -w "%{http_code}" -X POST http://127.0.0.1:8083/api/v1/auth/register \
  -H @$HDR -H "Content-Type: application/json" --data-binary @-
echo; python3 -c "import json; d=json.load(open(\"/tmp/sd-admin-resp\")); print(d.get(\"message\") or d.get(\"user\",{}).get(\"email\",\"\"))" 2>/dev/null || true
rm -f /tmp/sd-admin-resp'

RESULT=$(printf '%s' "$P1" | python3 -c 'import json,sys; print(json.dumps({"email": sys.argv[1], "name": "Admin", "password": sys.stdin.read(), "phones": []}))' "$EMAIL" \
  | $SSH "$SERVER" "$REMOTE")
unset P1 P2
STATUS=$(echo "$RESULT" | head -1); DETAIL=$(echo "$RESULT" | tail -1)
case "$STATUS" in
  200) echo "Admin $EMAIL created. Log in at https://smartdirect.qrfood.az/login and open /admin." ;;
  *)   echo "Not created (HTTP $STATUS): $DETAIL" ; exit 1 ;;
esac
