Как это запустить и использовать
Запуск: В терминале в папке с файлом выполни:
docker-compose up -d

Доступ к UI: Открой в браузере http://localhost:8081.

Логин: admin@smartdirect.aze
Пароль: admin_password

Подключение к базе внутри pgAdmin:

Нажми "Add New Server".

Во вкладке General назови его SmartDirect-Local.

Во вкладке Connection:

Host: db (именно имя сервиса из docker-compose, если внутри сети Docker) или host.docker.internal.

Port: 5432

Maintenance database: smartdirect_db

Username: nizamidev

Password: your_strong_password