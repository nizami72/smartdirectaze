#!/usr/bin/env bash
# Builds the backend jar and the frontend and ships them to the server. Run on the laptop from the repo root:
#   bash deploy/deploy.sh
# Builds in a temporary copy, so a backend running from backend/target in IntelliJ is not touched.
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
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
# Commit shown by /webhooks/w/alive; "-dirty" when uncommitted changes went into the build
COMMIT=$(git -C "$ROOT" rev-parse --short HEAD)$(git -C "$ROOT" diff --quiet HEAD -- backend frontend || echo -dirty)
# Released code carries the tag v<pom version> (deploy/release.sh); anything else is shipped with a warning
VERSION=$(sed -n '/<artifactId>smartdirectaze<\/artifactId>/{n;s/.*<version>\(.*\)<\/version>.*/\1/p;}' "$ROOT/backend/pom.xml")
if [ "$(git -C "$ROOT" describe --tags --exact-match HEAD 2>/dev/null || true)" = "v$VERSION" ] && [[ $COMMIT != *-dirty ]]; then
  echo "   version $VERSION = tag v$VERSION ($COMMIT)"
else
  echo "   WARNING: not a release: pom says $VERSION, commit $COMMIT has no tag v$VERSION (bash deploy/release.sh <version>)"
fi
(cd "$BUILD/backend" && ./mvnw -o -q package -DskipTests -Dgit.commit="$COMMIT" || ./mvnw -q package -DskipTests -Dgit.commit="$COMMIT")
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
# Each step prints itself, so a failure shows exactly where it stopped
$SSH "$SERVER" bash -s <<'REMOTE'
set -euo pipefail
echo "  install new jar"
chown smartdirect:smartdirect /opt/smartdirect/smartdirect.jar.new
mv /opt/smartdirect/smartdirect.jar.new /opt/smartdirect/smartdirect.jar
echo "  restart service"
systemctl restart smartdirect
echo -n "  waiting for the app"
for i in $(seq 1 60); do
  curl -sf http://127.0.0.1:8083/webhooks/w/alive >/dev/null && { echo; echo "backend is up"; exit 0; }
  echo -n "."; sleep 2
done
echo; echo "backend did not start in 2 minutes:"
journalctl -u smartdirect -n 30 --no-pager
exit 1
REMOTE

echo "== running version"
curl -s -m 10 "https://$DOMAIN/webhooks/w/alive" || echo "(no answer from https://$DOMAIN)"; echo
