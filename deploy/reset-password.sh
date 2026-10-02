#!/usr/bin/env bash
# Sets a new password for any user on the server (merchant or admin), e.g. when a merchant forgot it.
# Works only with your SSH key; the database hashes the password with bcrypt, like the app does.
#   bash deploy/reset-password.sh merchant@mail.az
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
SERVER=root@157.180.16.28
SSH="ssh -i $HOME/.ssh/key2 -o BatchMode=yes"
EMAIL=${1:?usage: reset-password.sh <email>}

read -r -s -p "New password for $EMAIL: " P1; echo
read -r -s -p "Repeat: " P2; echo
[ "$P1" = "$P2" ] || { echo "Passwords differ, nothing changed."; exit 1; }
[ ${#P1} -ge 10 ] || { echo "At least 10 characters, nothing changed."; exit 1; }

# Email and password go on stdin only; random dollar-quote tags keep any characters safe in SQL
UPDATED=$(printf '%s' "$P1" | python3 -c '
import secrets, sys
email, password = sys.argv[1], sys.stdin.read()
tag = lambda: "t" + secrets.token_hex(8)
pt, et = tag(), tag()
assert f"${pt}$" not in password and f"${et}$" not in email
print("SET client_min_messages TO warning; CREATE EXTENSION IF NOT EXISTS pgcrypto;")
print(f"UPDATE users SET password = crypt(${pt}${password}${pt}$, gen_salt(\x27bf\x27, 10)) "
      f"WHERE lower(email) = lower(${et}${email}${et}$) RETURNING 1;")
' "$EMAIL" | $SSH "$SERVER" "docker exec -i smartdirect-db psql -q -At -v ON_ERROR_STOP=1 -U smartdirect -d smartdirect_db" 2>&1 | tail -1 || true)
unset P1 P2
case "$UPDATED" in
  "1") echo "Password of $EMAIL changed. Already open sessions stay valid up to 24 h." ;;
  "") echo "No user $EMAIL on the server, nothing changed."; exit 1 ;;
  *) echo "Failed: $UPDATED"; exit 1 ;;
esac
