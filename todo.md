### m10
Implement m10 or not?

### Clean up
Run Gemini or Junie to clean up.

### Быстрый старт (запуск проекта локально)

Запускать по порядку — 4 вещи:

**1. Туннель** (вебхуки Telegram/WhatsApp/Instagram: сервер Hetzner `157.180.16.28:9000` → мой `localhost:8080`)
```
sudo systemctl start ssh-reverse-tunnel
# проверить:  systemctl status ssh-reverse-tunnel
# вручную (если без systemd):  ssh -i ~/.ssh/key2 -R 9000:localhost:8080 root@157.180.16.28
```

**2. База данных** (Postgres :5432 + pgAdmin :8081, из `~/.config/smartdirect/.env`)
```
bash shell/docker-up.sh
# pgAdmin:  http://localhost:8081   (логин/пароль из PGADMIN_DEFAULT_EMAIL/PASSWORD в том же .env)
```

**3. Backend** — Spring Boot на `:8080`
```
# из IntelliJ: run-конфигурация "Application"
# или из терминала:
cd backend && ./mvnw spring-boot:run
```

**4. Frontend** — Vite dev-сервер (обычно `:5173`, ходит на VITE_API_URL=http://localhost:8080)
```
cd frontend && npm run dev
```

Проверка живости backend: `http://localhost:8080` (эндпоинт alive), Telegram-веб: https://web.telegram.org/a/

---

### How to run the project on a local machine. (старое)

1. Run the app
2. [Open the browser and go to](https://web.telegram.org/a/)
3. [Open the PG DB page at](http://localhost:8081/login)
4. In alacrity run `sudo systemctl start ssh-reverse-tunnel`
