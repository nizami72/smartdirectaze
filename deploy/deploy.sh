#!/usr/bin/env bash
# Builds the backend jar and the frontend and ships them to the server. Run on the laptop from the repo root:
#   bash deploy/deploy.sh
# Builds in a temporary copy, so a backend running from backend/target in IntelliJ is not touched.
set -euo pipefail
SERVER=root@157.180.16.28
SSH="ssh -i $HOME/.ssh/key2 -o BatchMode=yes"
DOMAIN=smartdirect.qrfood.az
ROOT=$(cd "$(dirname "$0")/.." && pwd)
# Vite needs Node 20+: prefer the newest nvm Node over the system one
if [ -d "$HOME/.nvm/versions/node" ]; then
  export PATH="$HOME/.nvm/versions/node/$(ls "$HOME/.nvm/versions/node" | sort -V | tail -1)/bin:$PATH"
fi
BUILD=$(mktemp -d)
trap 'rm -rf "$BUILD"' EXIT

echo "== backend"
rsync -a --exclude target --exclude logs "$ROOT/backend/" "$BUILD/backend/"
(cd "$BUILD/backend" && ./mvnw -q package -DskipTests)
JAR=$(ls "$BUILD"/backend/target/smartdirectaze-*.jar | grep -v plain | head -1)

echo "== frontend"
rsync -a --exclude node_modules --exclude dist "$ROOT/frontend/" "$BUILD/frontend/"
ln -s "$ROOT/frontend/node_modules" "$BUILD/frontend/node_modules"
# Same origin on the server: API calls go to /api on the same host
(cd "$BUILD/frontend" && VITE_API_URL= npx vite build >/dev/null)

echo "== upload"
rsync -a -e "$SSH" "$JAR" "$SERVER:/opt/smartdirect/smartdirect.jar.new"
rsync -a --delete -e "$SSH" "$BUILD/frontend/dist/" "$SERVER:/var/www/$DOMAIN/dist/"

echo "== restart"
$SSH "$SERVER" "chown smartdirect:smartdirect /opt/smartdirect/smartdirect.jar.new \
  && mv /opt/smartdirect/smartdirect.jar.new /opt/smartdirect/smartdirect.jar \
  && systemctl restart smartdirect \
  && for i in \$(seq 1 60); do curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null && break; sleep 2; done \
  && curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null && echo 'backend is up' || (journalctl -u smartdirect -n 30 --no-pager; exit 1)"
