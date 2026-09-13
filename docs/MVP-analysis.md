# Анализ MVP: Магазины vs Ивенты

_Дата: 2026-09-08. Автор анализа: Claude (сессия с Nizami)._

## Вывод

**Выпускать MVP «Магазины» (AI-продавец), а не «Ивенты».** По обоим критериям:
быстрее до MVP и выше финансовая значимость.

## Состояние проекта

Модульный монолит Spring Modulith (Java 21) + React/Vite фронт + Telegram-мастербот
+ интеграции WhatsApp/Instagram. Схема БД генерируется через `ddl-auto: update`
(Liquibase-changelog отсутствует — общий технический долг обеих веток).

## Ветка 1 — Магазины (AI-продавец) — готовность ~60–65%

**Ядро ценности уже работает end-to-end:**
- Регистрация → Telegram-бот (токен → имя магазина → webhook) → React-админка → каталог.
- AI-продавец: сообщение клиента → LangChain4j-агент достаёт товары и политику магазина
  → `createFinalOrder` сохраняет заказ → уведомление владельцу.
- Товары (CRUD + фото, изоляция по shopId); мультиканал (Telegram готов, WhatsApp QR-флоу).

**Пробелы (это «день 2», не ядро):**
- ~~Нет вывода/ведения заказов~~ → **сделано** (см. ниже).
- Нет edit/delete магазина, нет UI для knowledge base.
- Дубль путей создания магазина (`WaitingForShopNameHandler` на legacy `ShopDto`
  vs `ShopCreateDto`); заглушка валидации токена `ShopCreationHandler:70`.
- Нет Liquibase baseline.

## Ветка 2 — Ивенты — backend ~60%, но ядро ценности не построено

- ✅ Полный CRUD events + guests + dashboard, есть тесты, аккуратный TypeScript-фронт.
- ❌ **Рассылка приглашений (главный смысл) не реализована нигде.** `GreenApiClient`
  умеет только QR/state/settings/logout — метода отправки сообщений нет.
  Кнопка «Send Invitations» → `«not implemented in MVP»` (`EventPage.tsx:193`).
- ❌ Рассинхрон фронт/бэк: форма собирает `type, endDate, venue, status, visibility,
  organizer*, notes, maxGuests`, а бэк хранит только 4 поля (`name, dateTime, place,
  description`) — остальное теряется при сохранении.
- ❌ `GuestPage` во view/edit на mock-данных; нет обновления RSVP; guest-stats бэк не отдаёт.

## Почему магазины

| Критерий | Магазины | Ивенты |
|---|---|---|
| Работает ядро ценности | ✅ AI-продажи → заказы | ❌ рассылки нет |
| До реального MVP | малый объём | средне-большой (WhatsApp-отправка + поля + RSVP) |
| Рискованная часть | нет | WhatsApp bulk: внешнее API, шаблоны, комплаенс, доставляемость |
| Финансовая модель | подписка + рост с GMV, повторяющаяся | per-event, нишевая, разовая |
| Соответствие тезису | флагман (README, AI.md, рынок Баку) | смежный модуль |

## Критический путь до релиза магазинов

1. **[СДЕЛАНО]** Заказы: список + статусы + эндпоинты (владелец видит и ведёт заказы).
2. `GET/PUT/DELETE /api/v1/shops/{id}` + экран настроек магазина (доставка, политика, KB).
3. Убрать дубль путей создания; закрыть заглушку валидации токена (`ShopCreationHandler:70`).
4. Liquibase-baseline вместо `ddl-auto: update` (блокер прод-деплоя).
5. Тесты на `ShopService`/контроллер и e2e регистрации.

## Что уже сделано в этой сессии (пункт 1)

Заказы магазина — backend end-to-end:
- `OrderStatus` enum: `NEW, CONFIRMED, IN_DELIVERY, COMPLETED, CANCELLED`
  (`shop/entities/OrderStatus.java`).
- `OrderEntity.status` (по умолчанию `NEW`).
- `OrderRepository`: `findByShopIdOrderByCreatedAtDesc`, `findByIdAndShopId` (изоляция).
- `OrderService`: `getOrdersForShop`, `updateOrderStatus`; `OrderDTO.status`.
- `OrderController`: `GET /api/v1/shops/{shopId}/orders`,
  `PATCH /api/v1/shops/{shopId}/orders/{orderId}/status`; проверка владельца (404 для чужих).
- Тест `OrderServiceTest` (4 кейса, включая изоляцию по магазину) — зелёный.

**Осталось для полноты пункта 1:** экран заказов на фронте (список + смена статуса)
поверх уже готовых эндпоинтов.
