# Project Context: Smart-Direct AZE

## 1. Архитектурные правила (КРИТИЧЕСКИ ВАЖНО)
- **Архитектура:** Модульный монолит на базе **Spring Modulith**.
- **Правило изоляции:** Модули абсолютно изолированы. Связи между сущностями из разных модулей (например, `shop` и `user`) реализуются **строго через ID (Long)**. Никаких ORM-связей (`@ManyToOne`, `@OneToOne`) между разными модулями!
- **Событийная модель:** Межмодульное взаимодействие происходит асинхронно/транзакционно через `ApplicationEventPublisher` и `@ApplicationModuleListener` от Spring Modulith.
- **Внешние API:** Интеграции с мессенджерами вынесены в отдельные модули (`whatsapp`, `telegram`, `instagram`).

## 2. Стек технологий
- **Backend:** Java 21, Spring Boot 3.x, Spring Modulith.
- **Database:** PostgreSQL, Liquibase (миграции).
- **AI:** LangChain4j (RAG, Tools), системные промпты хранятся в `ShopEntity.knowledgeBase`.
- **Integrations:** Green-API / Whapi (WhatsApp), Telegram Bots.
- **Frontend:** React (Админка), Vite.

## 3. Основные модули и бизнес-логика
- **auth / user:** Управление пользователями (`User`), профилями (`OwnerProfile`) и RBAC. Статус регистрации: `registration_step` (ACCOUNT_CREATED, SHOP_CREATED, COMPLETED).
- **shop (ранее eatery):** Центр управления магазином.
    - `ShopEntity`: Настройки магазина, логистика, ИИ-промпты.
    - `ProductEntity`: Каталог товаров (мультиязычность в `titles`/`descriptions`).
    - `OrderEntity`: Заказы (хранят `shopId`).
    - `AiChannelEntity`: Каналы связи (WHATSAPP, TELEGRAM, INSTAGRAM).
- **ai:** Логика RAG, CatalogTools для поиска товаров ИИ, обработка промптов.
- **telegram / whatsapp / instagram:** Транспортные модули для связи с клиентами и мастер-бот для регистрации.

## 4. Логистика и Специфика рынка (Баку)
Учитывать поля в `ShopEntity`:
- `landmark` (ориентир), `zones_text`.
- `delivery_price`, `free_delivery_threshold`.
- `fitting_allowed` (примерка), `refusal_fee` (плата при отказе).
- Способы оплаты: `payment_methods_json` (включая m10, нал, карты).

## 5. Операционный контекст (Local Dev)
- **DB:** Админка БД доступна локально на порту 8081.
- **Reverse Tunnel:** Для получения вебхуков от WhatsApp/Telegram используется SSH-реверс-туннель (`sudo systemctl start ssh-reverse-tunnel`).
- **Data Sync:** Имеется сервис `CatalogSyncService` для синхронизации товаров.