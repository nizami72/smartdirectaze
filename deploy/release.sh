#!/usr/bin/env bash
# Releases a new version: sets it in backend/pom.xml, commits and tags v<version>, so the pom number
# always matches a git tag. Run on the laptop from the repo root on a clean master:
#   bash deploy/release.sh 0.2.0     # new features
#   bash deploy/release.sh 0.1.1     # fixes only
# Then: bash deploy/deploy.sh
# Started from zsh (e.g. "zsh script" or an IDE run button): rerun in bash, the script relies on it
[ -n "${BASH_VERSION:-}" ] || exec bash "$0" "$@"
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
POM="$ROOT/backend/pom.xml"
VERSION=${1:?usage: release.sh <version>, e.g. 0.2.0}

[[ $VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || { echo "Version must look like 0.2.0"; exit 1; }
[ -z "$(git -C "$ROOT" status --porcelain)" ] || { echo "Uncommitted changes: commit them first, nothing released."; exit 1; }
! git -C "$ROOT" rev-parse -q --verify "refs/tags/v$VERSION" >/dev/null || { echo "Tag v$VERSION already exists."; exit 1; }

# The project's own <version> is the first one after <artifactId>smartdirectaze</artifactId>
CURRENT=$(python3 - "$POM" "$VERSION" <<'PY'
import re, sys
path, new = sys.argv[1], sys.argv[2]
text = open(path).read()
m = re.search(r'(<artifactId>smartdirectaze</artifactId>\s*<version>)([^<]+)(</version>)', text)
if not m:
    sys.exit("project version not found in pom.xml")
open(path, 'w').write(text[:m.start(2)] + new + text[m.end(2):])
print(m.group(2))
PY
)
echo "Version $CURRENT -> $VERSION"

git -C "$ROOT" add backend/pom.xml
git -C "$ROOT" commit -q -m "release: $VERSION"
git -C "$ROOT" tag -a "v$VERSION" -m "SmartDirect $VERSION"
git -C "$ROOT" push -q origin HEAD "v$VERSION"
echo "Released v$VERSION ($(git -C "$ROOT" rev-parse --short HEAD)). Deploy: bash deploy/deploy.sh"
