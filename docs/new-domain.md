# Переезд SmartDirect на собственный домен

План работ на случай, когда у сервиса появится свой домен вместо поддомена `smartdirect.qrfood.az`. В примерах новый домен — **`smartdirect.az`**; подставьте настоящий.

Сервер остаётся тем же (Hetzner `157.180.16.28`), меняется только адрес. Время работ — около часа, сайт не останавливается: старый адрес работает до конца переезда и потом перенаправляет на новый.

## Оглавление

1. [Что зависит от адреса](#depends)
2. [До переезда](#before)
3. [Переезд по шагам](#steps)
4. [После переезда](#after)
5. [Откат](#rollback)
6. [Чек-лист](#checklist)

---

<a id="depends"></a>
## 1. Что зависит от адреса

Проверено по репозиторию и серверу 2026-10-03.

| Где | Что | Что будет, если забыть |
|---|---|---|
| DNS | A-запись домена → `157.180.16.28` | Сайт не открывается |
| Сертификат | Let's Encrypt для нового домена (`/etc/letsencrypt/live/<домен>/`) | Браузер не откроет сайт по HTTPS |
| nginx | `deploy/nginx/smartdirect.qrfood.az.conf` → `server_name`, пути к сертификату, `root /var/www/<домен>/dist` | Сайт не отвечает на новом адресе |
| Настройки сервера `/etc/smartdirect/smartdirect.env` | `FRONTEND_BASE_URL`, `WHATSAPP_WEBHOOK_URL`, `CORS_ALLOWED_ORIGINS` | Ссылки в уведомлениях на старый адрес; браузер отказывает в запросах к API (CORS 403); новые инстансы Green API получают старый адрес вебхука |
| **Вебхуки Green API каждого магазина** | В настройках каждого инстанса записан адрес `…/webhooks/w` | **AI перестаёт отвечать покупателям**, как только старый адрес отключат |
| Скрипты `deploy/` | `DOMAIN=` в `deploy.sh` и `server-setup.sh`, `SITE=` в `reset-server.sh` и `restore-db.sh`, адрес в `healthcheck.sh` и `create-admin.sh` | Выкладка кладёт фронт в старую папку; проверка здоровья проверяет не тот адрес; подтверждение сброса и восстановления ждёт старое имя |
| Документы | `docs/how-to.md`, `deploy/README.md`, `docs/client-flow.md`, `docs/backups.md` | Инструкции со старыми ссылками |
| Условия пилота | Текст `/terms` адрес не содержит — менять не нужно | — |
| Сессии мерчантов | Cookie входа привязана к адресу сайта | После переезда мерчанты входят заново один раз — это нормально |

Не зависят от адреса: база данных, бэкапы (`~/.dumps/smartdirectaze`, `/var/backups/smartdirect`), номера WhatsApp магазинов, сами инстансы Green API, Telegram-сообщения оператору.

---

<a id="before"></a>
## 2. До переезда

1. **Купить домен** (например, `.az` у местного регистратора или `.com`). Нужен доступ к управлению DNS.
2. **A-запись**: `smartdirect.az` → `157.180.16.28` (и `www.smartdirect.az` → тот же IP, если нужен `www`). TTL 300.
3. Дождаться, пока запись видна: `dig +short smartdirect.az` → `157.180.16.28`. Обычно от минут до нескольких часов.
4. Свежий бэкап перед работами: `ssh -i ~/.ssh/key2 root@157.180.16.28 /opt/smartdirect/backup-db.sh`.
5. Выбрать время: вечер, когда покупателей меньше.

---

<a id="steps"></a>
## 3. Переезд по шагам

Команды на сервере — через `ssh -i ~/.ssh/key2 root@157.180.16.28`. Остальное — на компьютере в репозитории.

### Шаг 1. Сертификат для нового домена
Так же, как для текущего (standalone, nginx останавливается на несколько секунд):
```bash
certbot certonly --standalone -d smartdirect.az -d www.smartdirect.az \
  --pre-hook "systemctl stop nginx" --post-hook "systemctl start nginx"
```
Продлевается тем же ночным cron certbot.

### Шаг 2. nginx: новый сайт, старый пока работает как раньше
В репозитории: скопировать `deploy/nginx/smartdirect.qrfood.az.conf` в `deploy/nginx/smartdirect.az.conf` и заменить в нём `server_name`, пути `ssl_certificate*` и `root /var/www/smartdirect.az/dist`. На сервере:
```bash
mkdir -p /var/www/smartdirect.az && cp -a /var/www/smartdirect.qrfood.az/dist /var/www/smartdirect.az/
# скопировать новый conf в /etc/nginx/sites-available/, затем:
ln -s /etc/nginx/sites-available/smartdirect.az.conf /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```
Проверка: `curl -s https://smartdirect.az/webhooks/w/alive` → `"status":"UP"`. Сейчас сайт отвечает **на обоих адресах**.

### Шаг 3. Настройки приложения
В `/etc/smartdirect/smartdirect.env`:
```
FRONTEND_BASE_URL=https://smartdirect.az
WHATSAPP_WEBHOOK_URL=https://smartdirect.az/webhooks/w
CORS_ALLOWED_ORIGINS=https://smartdirect.az,https://smartdirect.qrfood.az
```
Старый адрес в CORS остаётся на время переезда, чтобы открытые у мерчантов вкладки не ломались. Затем `systemctl restart smartdirect` (~15 секунд).

### Шаг 4. Скрипты и выкладка
В репозитории заменить `smartdirect.qrfood.az` на новый домен в: `deploy/deploy.sh`, `deploy/server-setup.sh`, `deploy/healthcheck.sh`, `deploy/reset-server.sh`, `deploy/restore-db.sh`, `deploy/create-admin.sh`, `deploy/smartdirect.env.example`, документах. Найти все места:
```bash
grep -rn "smartdirect.qrfood.az" --exclude-dir=node_modules --exclude-dir=target .
```
Затем:
```bash
bash deploy/deploy.sh
scp -i ~/.ssh/key2 deploy/healthcheck.sh root@157.180.16.28:/opt/smartdirect/healthcheck.sh
```

### Шаг 5. Вебхуки Green API — главный шаг
Войти админом на **новом** адресе → `/admin` → **«Обновить вебхуки у всех инстансов»**. Каждый подключённый инстанс получит адрес `https://smartdirect.az/webhooks/w` (берётся из `WHATSAPP_WEBHOOK_URL`). Результат показывается по каждому магазину — у всех должно быть `ok`.

Проверка: написать магазину со второго телефона — AI отвечает; в логе nginx запросы Green API идут на новый домен:
```bash
grep "POST /webhooks/w " /var/log/nginx/access.log | tail -3
```

### Шаг 6. Старый адрес перенаправляет на новый
Когда шаги 1–5 работают: в конфиге `smartdirect.qrfood.az` (блок `server { listen 443 … }`) **оставить** `location /webhooks/ { … }` как есть — на случай, если какой-то инстанс не обновился, — а остальные `location` (`/api/`, `/assets/`, `/`) заменить одним перенаправлением:
```nginx
location / { return 301 https://smartdirect.az$request_uri; }
```
`nginx -t && systemctl reload nginx`. Мерчанты со старыми закладками попадут на новый сайт и войдут заново.

---

<a id="after"></a>
## 4. После переезда

- **Сообщить мерчантам** новый адрес (WhatsApp поддержки).
- **Через 2–4 недели**, если в логе nginx нет запросов на старый адрес, кроме перенаправлений:
  - убрать старый домен из `CORS_ALLOWED_ORIGINS`, перезапустить сервис;
  - решить, оставлять ли перенаправление со `smartdirect.qrfood.az` (дёшево, можно навсегда) или удалить сайт и сертификат (`certbot delete --cert-name smartdirect.qrfood.az`).
- Обновить память и документы: `docs/how-to.md`, `deploy/README.md`, `docs/backups.md`.
- Поменять в Telegram-боте оператора и любых внешних местах (визитки, Instagram) ссылки на сайт.

---

<a id="rollback"></a>
## 5. Откат

До шага 6 откат простой — старый адрес не трогали:
1. Вернуть в `smartdirect.env` старые `FRONTEND_BASE_URL` и `WHATSAPP_WEBHOOK_URL`, перезапустить сервис.
2. `/admin` → «Обновить вебхуки у всех инстансов» — инстансы снова на старом адресе.
3. Новый сайт nginx можно оставить или отключить (`rm /etc/nginx/sites-enabled/smartdirect.az.conf && systemctl reload nginx`).

После шага 6 — сначала вернуть старый конфиг nginx `smartdirect.qrfood.az` из репозитория, затем то же самое.

---

<a id="checklist"></a>
## 6. Чек-лист

- [ ] Домен куплен, A-запись → `157.180.16.28`, `dig` показывает IP
- [ ] Свежий бэкап базы
- [ ] Сертификат на новый домен
- [ ] nginx: новый сайт, `alive` отвечает на новом адресе
- [ ] `smartdirect.env`: `FRONTEND_BASE_URL`, `WHATSAPP_WEBHOOK_URL`, `CORS_ALLOWED_ORIGINS`; сервис перезапущен
- [ ] Скрипты `deploy/` и документы на новый домен; `deploy.sh` выложил; `healthcheck.sh` обновлён на сервере
- [ ] `/admin` → «Обновить вебхуки у всех инстансов» — у всех `ok`
- [ ] Проверка с телефона: AI отвечает, заказ уведомляет продавца, ссылка в уведомлении ведёт на новый адрес
- [ ] Старый адрес перенаправляет на новый (вебхуки на старом адресе ещё принимаются)
- [ ] Мерчанты знают новый адрес
- [ ] Через 2–4 недели: убрать старый адрес из CORS, решить судьбу старого сайта
