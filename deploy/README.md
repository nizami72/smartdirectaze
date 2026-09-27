# Deploying SmartDirect

Server: Hetzner 157.180.16.28 (shared with qrfood, carhub, legalai). Address: https://smartdirect.qrfood.az

| What | Where |
|---|---|
| Backend | systemd `smartdirect`, `/opt/smartdirect/smartdirect.jar`, 127.0.0.1:8083, max ~500 MB |
| Settings | `/etc/smartdirect/smartdirect.env` (see `smartdirect.env.example`) |
| Database | Docker `smartdirect-db` (Postgres 16), 127.0.0.1:5433, volume `smartdirect_pgdata` |
| Backups | `/var/backups/smartdirect`, daily 02:30, 14 days |
| Frontend | `/var/www/smartdirect.qrfood.az/dist` |
| Nginx | `/etc/nginx/sites-available/smartdirect.qrfood.az.conf` |
| Logs | `journalctl -u smartdirect`, `/opt/smartdirect/logs` |

## First time

1. DNS (Hetzner DNS, zone qrfood.az): A record `smartdirect` → 157.180.16.28.
2. Copy `deploy/` to the server and run `DB_PASSWORD=... bash /tmp/sd-deploy/server-setup.sh`.
3. Certificate (standalone like the other sites, nginx stops for a few seconds):
   `certbot certonly --standalone -d smartdirect.qrfood.az --pre-hook "systemctl stop nginx" --post-hook "systemctl start nginx"`.
   Renewed by the existing 03:00 cron.
4. Enable the site: `ln -s /etc/nginx/sites-available/smartdirect.qrfood.az.conf /etc/nginx/sites-enabled/ && nginx -t && systemctl reload nginx`.
5. Create `/etc/smartdirect/smartdirect.env` (chmod 600).
6. From the laptop: `bash deploy/deploy.sh`.
7. Admin page → "Обновить вебхуки у всех инстансов": Green API sends webhooks to the new address.

## Every release

`bash deploy/deploy.sh` from the repo root.

## Wipe to a fresh install (debug only)

`bash deploy/reset-server.sh` — backup first, then empty database and photos. Works only with the SSH key and
while `/etc/smartdirect/allow-reset` exists on the server. After debugging close it:
`ssh -i ~/.ssh/key2 root@157.180.16.28 rm /etc/smartdirect/allow-reset`.

The admin is any user whose email is in `ADMIN_EMAILS`: after a wipe register with it right away
(emails are not verified yet).

