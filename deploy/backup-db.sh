#!/usr/bin/env bash
# Nightly dump of the SmartDirect database on the server; keeps the 4 newest.
# Cron (server time, Asia/Baku): 30 3 * * * /opt/smartdirect/backup-db.sh
# The laptop takes the newest one with deploy/fetch-dump.sh. Dumps made by reset-server.sh (before-reset_*) are kept.
set -euo pipefail
DIR=/var/backups/smartdirect
KEEP=4
mkdir -p "$DIR"
FILE="$DIR/smartdirect_$(date +%F).sql.gz"
# Written under a temporary name: a copy taken at this moment never gets a half-written dump
docker exec smartdirect-db pg_dump -U smartdirect smartdirect_db | gzip > "$FILE.tmp"
gzip -t "$FILE.tmp"
mv "$FILE.tmp" "$FILE"
ls -1t "$DIR"/smartdirect_*.sql.gz | tail -n +$((KEEP + 1)) | xargs -r rm --
