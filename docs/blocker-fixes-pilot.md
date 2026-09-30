# Blocker Fixes: первый пилот Smart Direct

Реализованы пункты 1–7. OrderDraft, брокер и поддержка нескольких backend instances не добавлены.
Deployment не выполнялся; тесты используют существующие зависимости.

## Поведение

- Dashboard вставляет каталог после появления DOM; устаревшие ответы при смене магазина игнорируются.
- Logout вызывает сервер и удаляет HttpOnly cookie; при сетевой ошибке выход не считается успешным.
- Пустые условия магазина не заменяются бесплатной доставкой, вымышленными часами, адресом или оплатой.
- Пустой webhook secret запрещён по умолчанию и в prod/production. Исключение только для явно включённых local/mock/test.
- Логи WhatsApp/Telegram, AI, handoff и ошибок не выводят тела сообщений/ответов провайдера. Логи LangChain4j и SQL exception detail отключены, чтобы исключить обход через retry/SQL ошибки. Старые лог-файлы не изменены.
- Входящее сообщение сохраняется в whatsapp_inbox до HTTP 200. UNIQUE(instance_id, message_id) действует в БД. Дубли не запускают AI повторно, в том числе после рестарта.
- До четырёх чатов обрабатываются параллельно. Внутри (instanceId, chatId) обработка последовательна в порядке приёма backend, включая попытку отправки ответа.
- Заявка остаётся текстовой. Инструкции запрещают модели рассчитывать окончательную сумму; уведомление продавцу и панель требуют подтверждения наличия и стоимости.

## Перед запуском

1. Установить одинаковый WHATSAPP_WEBHOOK_TOKEN в приложении и настройках Green API. Production-профиль указан в systemd и примере env. Не включать local/mock/test на сервере.
2. Проверить наличие таблицы и ограничения UNIQUE из backend/migration_2026_09_30.sql. Текущий ddl-auto:update также создаёт сущность и индекс; SQL приложен для контролируемого применения. На production он в этой сессии не выполнялся.
3. Проверить начальную загрузку, F5, смену магазина, сохранение товара/доставки, медленную сеть.
4. Проверить logout, возврат Back/F5, /api/v1/auth/me без cookie (401), истёкшую cookie и сетевую ошибку выхода.
5. Со второго телефона проверить AZ/RU, неизвестные условия, заявку и текст о подтверждении продавцом.
6. Повторить один webhook с тем же idMessage: одна запись, один ответ/заявка. Отправить два разных сообщения быстро в один чат и сообщения в другой чат.
7. Проверить ответ продавца во время AI: после обнаруженной паузы готовый AI-ответ подавляется. Проверка и отправка не атомарны относительно внешнего WhatsApp.
8. Проверить реальные production logs на отсутствие тестовой фразы покупателя и ответа провайдера.

## Неопределённые результаты и ручное сопровождение

FAILED блокирует последующие сообщения только своего чата. PROCESSING после перезапуска становится FAILED. PENDING, перед которыми нет блокирующей записи, обрабатываются автоматически. Автоматического повторного вызова AI/отправки при неопределённом результате нет.

Для диагностики оператор читает id, instance_id, message_id, chat_id и status из whatsapp_inbox со status FAILED/PROCESSING. Тело переписки не нужно копировать в логи. После проверки заказа и фактической переписки оператор отвечает покупателю вручную. Только после разрешения ситуации конкретную запись можно вручную отметить DONE; последующие PENDING возобновятся. Перед этим проверить, что они ещё актуальны. Не переводить неопределённую запись в PENDING и не удалять её: это может повторить заявку/отправку. UI для recovery не добавлен.

## Оставшиеся риски

- Свободный ответ LLM может содержать ошибочную цену/сумму: принят для пилота. Серверного расчёта, резервирования остатков и структурированных позиций нет.
- Дедупликация действует на один idMessage. Новое сообщение с другим ID или повторный вызов инструмента моделью не является дублем webhook и всё ещё может создать новую заявку.
- Нет гарантии exactly-once доставки в Green API и автоматического восстановления неопределённых отправок. Уведомления продавцу также не имеют durable retry.
- После сбоя один чат может требовать ручного разбора; автоматический alert вне логов не добавлен.
- Только один backend. Память AI по-прежнему теряется при рестарте; сохранённые PENDING не восстанавливают весь предыдущий контекст.
- Содержимое сообщений хранится в inbox; автоматическая очистка/retention не добавлена. Удаление receipt уничтожает защиту от старых дублей.
- Уже скопированный JWT действует до истечения срока; logout очищает текущую браузерную cookie.
- Реальные Green API/AI и браузерный end-to-end сценарий этим прогоном тестов не проверены.

## Результаты проверок

- Целевой Maven-прогон: 40 тестов, 0 failures/errors (H2 для repository-тестов).
- Повтор пяти WhatsappInboxTest на PostgreSQL 15: 5 passed, включая конкурентный UNIQUE, FIFO, параллельные чаты и отказ от replay после ошибок/рестарта. Временная схема удалена.
- PostgreSQL SQL-проверка migration_2026_09_30.sql: успешна, транзакция отменена.
- Frontend production build: успешен на уже установленном Node 22.20.0; предупреждение о chunk >500 kB.
- git diff --check: успешно.
- Первоначальные ограничения/ошибки: sandbox мешал Mockito attach; системный Node 18 несовместим с Vite. Повтор вне sandbox и на Node 22 успешен. Исправлены две ошибки новых тестов: предположение о порядке атрибутов cookie и о порядке строк findAll().

## Изменённые файлы

- [backend/migration_2026_09_30.sql](/home/nizami/projects/java/smartdirectaze/backend/migration_2026_09_30.sql)
- [backend/src/main/java/az/nizami/smartdirectaze/ai/internal/CatalogTools.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/ai/internal/CatalogTools.java)
- [backend/src/main/java/az/nizami/smartdirectaze/ai/internal/DeepSeekConfig.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/ai/internal/DeepSeekConfig.java)
- [backend/src/main/java/az/nizami/smartdirectaze/ai/internal/SmartAssistantAgent.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/ai/internal/SmartAssistantAgent.java)
- [backend/src/main/java/az/nizami/smartdirectaze/ai/internal/WhatsappAiResponder.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/ai/internal/WhatsappAiResponder.java)
- [backend/src/main/java/az/nizami/smartdirectaze/exception/GlobalExceptionHandler.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/exception/GlobalExceptionHandler.java)
- [backend/src/main/java/az/nizami/smartdirectaze/identity/config/JwtAuthenticationFilter.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/identity/config/JwtAuthenticationFilter.java)
- [backend/src/main/java/az/nizami/smartdirectaze/identity/controller/AuthController.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/identity/controller/AuthController.java)
- [backend/src/main/java/az/nizami/smartdirectaze/shop/ConversationService.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/shop/ConversationService.java)
- [backend/src/main/java/az/nizami/smartdirectaze/shop/service/AiChannelServiceImpl.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/shop/service/AiChannelServiceImpl.java)
- [backend/src/main/java/az/nizami/smartdirectaze/shop/service/ConversationServiceImpl.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/shop/service/ConversationServiceImpl.java)
- [backend/src/main/java/az/nizami/smartdirectaze/shop/service/NotificationServiceImpl.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/shop/service/NotificationServiceImpl.java)
- [backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/controller/MasterBotController.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/controller/MasterBotController.java)
- [backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/controller/TelegramWebhookController.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/controller/TelegramWebhookController.java)
- [backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/service/MasterBotRouter.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/telegram/masterbot/service/MasterBotRouter.java)
- [backend/src/main/java/az/nizami/smartdirectaze/telegram/service/TelegramIntegration.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/telegram/service/TelegramIntegration.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessage.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessage.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessageRepository.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessageRepository.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessageService.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/IncomingWhatsappMessageService.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/WhatsappMessageDispatcher.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/WhatsappMessageDispatcher.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/client/GreenApiClient.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/client/GreenApiClient.java)
- [backend/src/main/java/az/nizami/smartdirectaze/whatsapp/controller/WhatsappWebhookController.java](/home/nizami/projects/java/smartdirectaze/backend/src/main/java/az/nizami/smartdirectaze/whatsapp/controller/WhatsappWebhookController.java)
- [backend/src/main/resources/application.yaml](/home/nizami/projects/java/smartdirectaze/backend/src/main/resources/application.yaml)
- [backend/src/main/resources/logback.xml](/home/nizami/projects/java/smartdirectaze/backend/src/main/resources/logback.xml)
- [backend/src/main/resources/templates/fragments/product-list.html](/home/nizami/projects/java/smartdirectaze/backend/src/main/resources/templates/fragments/product-list.html)
- [backend/src/test/java/az/nizami/smartdirectaze/ai/internal/CatalogToolsTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/ai/internal/CatalogToolsTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/ai/internal/MessageLoggingTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/ai/internal/MessageLoggingTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/ai/internal/WhatsappAiResponderTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/ai/internal/WhatsappAiResponderTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/identity/LogoutTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/identity/LogoutTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/shop/service/NotificationServiceImplTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/shop/service/NotificationServiceImplTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/whatsapp/WhatsappInboxTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/whatsapp/WhatsappInboxTest.java)
- [backend/src/test/java/az/nizami/smartdirectaze/whatsapp/WhatsappWebhookControllerTest.java](/home/nizami/projects/java/smartdirectaze/backend/src/test/java/az/nizami/smartdirectaze/whatsapp/WhatsappWebhookControllerTest.java)
- [deploy/smartdirect.env.example](/home/nizami/projects/java/smartdirectaze/deploy/smartdirect.env.example)
- [deploy/smartdirect.service](/home/nizami/projects/java/smartdirectaze/deploy/smartdirect.service)
- [frontend/src/pages/DashboardPage.jsx](/home/nizami/projects/java/smartdirectaze/frontend/src/pages/DashboardPage.jsx)
- [frontend/src/pages/OrdersPage.jsx](/home/nizami/projects/java/smartdirectaze/frontend/src/pages/OrdersPage.jsx)
