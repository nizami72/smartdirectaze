#!/usr/bin/env bash
# DEBUG ONLY: fills a shop on the server with a realistic demo catalog (default: shoe shop).
# Adds nothing twice (products matched by SKU). Works only while /etc/smartdirect/allow-reset exists.
#   bash deploy/seed-demo-shop.sh 2                               # shop #2, deploy/demo/shoe-shop.sql
#   bash deploy/seed-demo-shop.sh 2 deploy/demo/other-catalog.sql
set -euo pipefail
SHOP_ID=${1:?usage: seed-demo-shop.sh <shop id> [catalog.sql]}
CATALOG=${2:-$(dirname "$0")/demo/shoe-shop.sql}
[[ "$SHOP_ID" =~ ^[0-9]+$ ]] || { echo "Shop id must be a number"; exit 1; }
[ -f "$CATALOG" ] || { echo "No catalog file $CATALOG"; exit 1; }
SERVER=root@157.180.16.28
SSH="ssh -i $HOME/.ssh/key2 -o BatchMode=yes"
PSQL="docker exec -i smartdirect-db psql -q -At -U smartdirect -d smartdirect_db -v shop_id=$SHOP_ID"

$SSH "$SERVER" "[ -f /etc/smartdirect/allow-reset ]" || { echo "Demo data is closed on this server (/etc/smartdirect/allow-reset missing)."; exit 1; }
SHOP=$(echo "SELECT shop_name || ' (#' || id || '), products: ' || (SELECT count(*) FROM products WHERE shop_id = :shop_id) FROM shops WHERE id = :shop_id;" | $SSH "$SERVER" "$PSQL")
[ -n "$SHOP" ] || { echo "No shop #$SHOP_ID on the server"; exit 1; }
echo "Before: $SHOP"
$SSH "$SERVER" "$PSQL" < "$CATALOG"
echo "After:  $(echo "SELECT shop_name || ' (#' || id || '), products: ' || (SELECT count(*) FROM products WHERE shop_id = :shop_id) FROM shops WHERE id = :shop_id;" | $SSH "$SERVER" "$PSQL")"
