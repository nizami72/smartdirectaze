#!/usr/bin/env bash
# Daily dump of the SmartDirect database; keeps 14 days. Cron: 30 2 * * * /opt/smartdirect/backup-db.sh
set -euo pipefail
DIR=/var/backups/smartdirect
mkdir -p "$DIR"
docker exec smartdirect-db pg_dump -U smartdirect smartdirect_db | gzip > "$DIR/smartdirect_$(date +%F).sql.gz"
find "$DIR" -name "smartdirect_*.sql.gz" -mtime +14 -delete
