# Вычитка азербайджанских текстов интерфейса

Все фразы, которые видит мерчант: русский оригинал и азербайджанский перевод. Перевод сделан Claude, его нужно проверить носителю языка.

Куда вносить исправления:

- раздел 1 (сайт) — `frontend/src/i18n/az.json`, тот же ключ;
- раздел 2 (страница «Товары и доставка», ошибки сервера) — `backend/src/main/resources/messages_az.properties` и `messages.properties` (одинаково в обоих);
- уведомления в WhatsApp (заказ, «нужна помощь», тестовое) — `backend/.../shop/service/NotificationServiceImpl.java`.

Или прислать Claude списком «ключ → новый текст».

## 1. Сайт

| Ключ | Русский | Azərbaycanca | ✓ |
|---|---|---|---|
| `common.continue` | Продолжить | Davam et | |
| `common.login` | Войти | Daxil ol | |
| `common.logout` | Выйти | Çıxış | |
| `common.admin` | Админ | Admin | |
| `common.save` | Сохранить | Yadda saxla | |
| `common.cancel` | Отмена | Ləğv et | |
| `common.retry` | Повторить | Yenidən cəhd et | |
| `common.open` | Открыть | Aç | |
| `field.email` | Электронная почта | E-poçt | |
| `field.password` | Пароль | Şifrə | |
| `validation.emailRequired` | Email обязателен | E-poçt mütləqdir | |
| `validation.emailFormat` | Некорректный формат email | E-poçt formatı düzgün deyil | |
| `validation.passwordRequired` | Пароль обязателен | Şifrə mütləqdir | |
| `validation.passwordMin` | Минимум 6 символов | Ən azı 6 simvol | |
| `validation.nameRequired` | Имя обязательно | Ad mütləqdir | |
| `validation.phoneFull` | Введите полный номер телефона | Telefon nömrəsini tam daxil edin | |
| `validation.termsRequired` | Примите условия пилота | Pilot şərtlərini qəbul edin | |
| `login.title` | Вход в кабинет | Kabinetə giriş | |
| `login.subtitle` | Авторизуйтесь, чтобы продолжить настройку вашего ИИ-ассистента. | AI köməkçinizin qurulmasına davam etmək üçün daxil olun. | |
| `login.invalid` | Неверный email или пароль | E-poçt və ya şifrə yanlışdır | |
| `login.noAccount` | Нет аккаунта? | Hesabınız yoxdur? | |
| `login.register` | Зарегистрироваться | Qeydiyyatdan keçin | |
| `login.chooseShop` | Выберите магазин | Mağaza seçin | |
| `register.step` | Создание аккаунта | Hesabın yaradılması | |
| `register.title` | Регистрация партнера | Partnyor qeydiyyatı | |
| `register.subtitle` | Зарегистрируйте личный кабинет ИИ-ассистента для автоматизации продаж вашего бизнеса. | Biznesinizin satışlarını avtomatlaşdırmaq üçün AI köməkçinin şəxsi kabinetini qeydiyyatdan keçirin. | |
| `register.name` | Ваше имя | Adınız | |
| `register.namePlaceholder` | Например, Низами | Məsələn, Nizami | |
| `register.phone` | Контактный телефон | Əlaqə telefonu | |
| `register.password` | Пароль для входа | Giriş şifrəsi | |
| `register.acceptTerms` | Я принимаю | Mən | |
| `register.termsLink` | условия пилота | pilot şərtlərini | |
| `register.acceptTermsEnd` |  | qəbul edirəm | |
| `register.failed` | Произошла ошибка при регистрации. Попробуйте еще раз. | Qeydiyyat zamanı xəta baş verdi. Yenidən cəhd edin. | |
| `register.haveAccount` | Уже есть аккаунт? | Artıq hesabınız var? | |
| `shops.title` | Мои магазины | Mağazalarım | |
| `shops.new` | + Новый магазин | + Yeni mağaza | |
| `shops.emptyTitle` | У вас пока нет магазинов. | Hələ mağazanız yoxdur. | |
| `shops.emptyText` | Создайте первый магазин — это займёт минуту. | İlk mağazanızı yaradın — bir dəqiqə çəkəcək. | |
| `shops.create` | Создать магазин | Mağaza yarat | |
| `shops.loadFailed` | Не удалось загрузить ваши магазины. Попробуйте ещё раз. | Mağazalarınızı yükləmək mümkün olmadı. Yenidən cəhd edin. | |
| `shops.shopNumber` | Магазин №{{id}} | Mağaza №{{id}} | |
| `createShop.title` | Создайте магазин | Mağaza yaradın | |
| `createShop.subtitle` | Это займёт минуту. Адрес, часы работы и другие условия можно добавить позже. | Bir dəqiqə çəkəcək. Ünvanı, iş saatlarını və digər şərtləri sonra əlavə etmək olar. | |
| `createShop.name` | Название магазина | Mağazanın adı | |
| `createShop.namePlaceholder` | Напр: My Boutique | Məs: My Boutique | |
| `createShop.nameRequired` | Введите название магазина | Mağazanın adını daxil edin | |
| `createShop.delivery` | Доставка, AZN | Çatdırılma, AZN | |
| `createShop.freeFrom` | Бесплатно от, AZN | Pulsuz, AZN-dən | |
| `createShop.failed` | Не удалось создать магазин. Попробуйте ещё раз. | Mağaza yaratmaq mümkün olmadı. Yenidən cəhd edin. | |
| `onboarding.step` | Шаг {{current}} из {{total}} · {{name}} | Addım {{current}} / {{total}} · {{name}} | |
| `onboarding.shop` | Магазин | Mağaza | |
| `onboarding.products` | Товары | Məhsullar | |
| `onboarding.check` | Проверка | Yoxlama | |
| `onboarding.whatsapp` | WhatsApp | WhatsApp | |
| `connect.statusFailed` | Не удалось получить статус WhatsApp для этого магазина. | Bu mağaza üçün WhatsApp statusunu almaq mümkün olmadı. | |
| `connect.completeFailed` | Не удалось завершить регистрацию. Попробуйте еще раз. | Qeydiyyatı tamamlamaq mümkün olmadı. Yenidən cəhd edin. | |
| `connect.saveFailed` | Не удалось сохранить. Попробуйте ещё раз. | Yadda saxlamaq mümkün olmadı. Yenidən cəhd edin. | |
| `connect.disconnectFailed` | Не удалось отключить номер. Попробуйте ещё раз. | Nömrəni ayırmaq mümkün olmadı. Yenidən cəhd edin. | |
| `connect.loading` | Загрузка конфигурации... | Yüklənir... | |
| `connect.errorTitle` | Упс! Что-то пошло не так | Bir xəta baş verdi | |
| `connect.loadFailed` | Не удалось загрузить данные. | Məlumatları yükləmək mümkün olmadı. | |
| `connect.tryAgain` | Попробовать снова | Yenidən cəhd et | |
| `connect.preparingTitle` | Подключаем WhatsApp | WhatsApp qoşulur | |
| `connect.preparingText` | Мы готовим подключение WhatsApp для вашего магазина — обычно в течение рабочего дня. Когда всё будет готово, на этой странице появится QR-код. | Mağazanız üçün WhatsApp qoşulmasını hazırlayırıq — adətən bir iş günü ərzində. Hazır olanda bu səhifədə QR-kod görünəcək. | |
| `connect.preparingHint` | А пока можно добавить товары и проверить продавца в панели магазина. | Bu vaxt mağaza panelində məhsullar əlavə edib satıcını yoxlaya bilərsiniz. | |
| `connect.support` | Написать в поддержку | Dəstəyə yazın | |
| `connect.bindInstance` | Привязать инстанс (админ) | İnstansı bağla (admin) | |
| `connect.riskTitle` | Перед подключением | Qoşulmadan əvvəl | |
| `connect.riskText` | SmartDirect подключается к WhatsApp как связанное устройство — так же, как WhatsApp Web на компьютере. Это не официальный сервис WhatsApp, поэтому есть небольшой риск, что WhatsApp ограничит или заблокирует номер. | SmartDirect WhatsApp-a əlaqəli cihaz kimi qoşulur — kompüterdəki WhatsApp Web kimi. Bu, WhatsApp-ın rəsmi xidməti deyil, ona görə WhatsApp-ın nömrəni məhdudlaşdırması və ya bloklaması üçün kiçik risk var. | |
| `connect.riskHow` | Как снизить риск: | Riski necə azaltmaq olar: | |
| `connect.riskTip1` | подключайте номер, который давно работает в WhatsApp, а не только что созданный; | yeni yaradılmış deyil, çoxdan WhatsApp-da işləyən nömrəni qoşun; | |
| `connect.riskTip2` | держите телефон магазина включённым и в сети; | mağazanın telefonunu açıq və şəbəkədə saxlayın; | |
| `connect.riskTip3` | не отправляйте с этого номера рассылки людям, которые вам не писали. | bu nömrədən sizə yazmayan insanlara kütləvi mesaj göndərməyin. | |
| `connect.riskAccept` | Я понимаю риск и подключаю номер под свою ответственность | Riski başa düşürəm və nömrəni öz məsuliyyətimlə qoşuram | |
| `connect.showQr` | Показать QR-код | QR-kodu göstər | |
| `connect.scanHowTo` | Откройте WhatsApp на телефоне → Настройки → Связанные устройства → Привязка устройства | Telefonda WhatsApp-ı açın → Ayarlar → Əlaqəli cihazlar → Cihaz əlaqələndir | |
| `connect.waitingScan` | Ожидание сканирования | Skan gözlənilir | |
| `connect.connected` | Успешно подключено! | Uğurla qoşuldu! | |
| `connect.isCustomerNumber` | Это номер, на который пишут покупатели? | Müştərilərin yazdığı nömrə budur? | |
| `connect.connectAnother` | Нет, подключить другой | Xeyr, başqasını qoş | |
| `connect.testModeHint` | AI сейчас в режиме «Тест» и отвечает только вашим тестовым номерам. Добавьте свой второй номер в панели магазина, проверьте ответы и включите AI для всех. | AI hazırda «Test» rejimindədir və yalnız test nömrələrinizə cavab verir. Mağaza panelində ikinci nömrənizi əlavə edin, cavabları yoxlayın və AI-ni hamı üçün açın. | |
| `connect.toDashboard` | Войти в панель управления | İdarə panelinə keç | |
| `connect.updating` | Обновление статуса... | Status yenilənir... | |
| `connect.title` | Подключите WhatsApp | WhatsApp-ı qoşun | |
| `connect.subtitle` | Последний шаг, чтобы AI начал отвечать вашим клиентам. | AI-nin müştərilərinizə cavab verməyə başlaması üçün son addım. | |
| `connect.later` | Подключу позже — перейти в панель магазина | Sonra qoşaram — mağaza panelinə keç | |
| `ai.modeOff` | Выключен | Söndürülüb | |
| `ai.modeOffHint` | AI молчит, вы отвечаете клиентам сами. | AI susur, müştərilərə özünüz cavab verirsiniz. | |
| `ai.modeTest` | Тест | Test | |
| `ai.modeTestHint` | AI отвечает только номерам из списка ниже. Остальные клиенты его не видят. | AI yalnız aşağıdakı siyahıdakı nömrələrə cavab verir. Digər müştərilər onu görmür. | |
| `ai.modeOn` | Для всех | Hamı üçün | |
| `ai.modeOnHint` | AI отвечает всем клиентам, которые пишут на ваш WhatsApp. | AI WhatsApp-ınıza yazan bütün müştərilərə cavab verir. | |
| `ai.saved` | Сохранено | Yadda saxlanıldı | |
| `ai.saveFailed` | Не удалось сохранить, попробуйте ещё раз | Yadda saxlamaq mümkün olmadı, yenidən cəhd edin | |
| `ai.invalidPhone` | Неверный номер: азербайджанский номер — 12 цифр, например +994 50 123 45 67 | Nömrə yanlışdır: Azərbaycan nömrəsi 12 rəqəmdir, məsələn +994 50 123 45 67 | |
| `ai.testSent` | Отправлено на {{phone}}. Проверьте WhatsApp на этом телефоне. | {{phone}} nömrəsinə göndərildi. Bu telefonda WhatsApp-ı yoxlayın. | |
| `ai.testFailed` | Не удалось отправить, попробуйте ещё раз | Göndərmək mümkün olmadı, yenidən cəhd edin | |
| `ai.title` | AI-продавец в WhatsApp | WhatsApp-da AI satıcı | |
| `ai.connected` | WhatsApp подключён | WhatsApp qoşulub | |
| `ai.notConnected` | WhatsApp не подключён — AI не сможет отвечать клиентам. | WhatsApp qoşulmayıb — AI müştərilərə cavab verə bilməyəcək. | |
| `ai.connect` | Подключить | Qoş | |
| `ai.confirmOnBefore` | AI будет отвечать всем, кто пишет на | AI bu nömrəyə yazan hər kəsə cavab verəcək: | |
| `ai.confirmOnNoNumber` | подключённый номер WhatsApp | qoşulmuş WhatsApp nömrəsi | |
| `ai.confirmOnAfter` | . Это номер, на который пишут ваши покупатели? | . Müştərilərinizin yazdığı nömrə budur? | |
| `ai.confirmOnWarning` | Не отправляйте с этого номера рассылки незнакомым людям — это главный повод для блокировки номера в WhatsApp. | Bu nömrədən tanımadığınız insanlara kütləvi mesaj göndərməyin — WhatsApp-da nömrənin bloklanmasının əsas səbəbi budur. | |
| `ai.confirmOnYes` | Да, включить для всех | Bəli, hamı üçün aç | |
| `ai.testPhones` | Тестовые номера | Test nömrələri | |
| `ai.testPhonesEmpty` | Добавьте свой второй номер, чтобы проверить ответы AI. | AI-nin cavablarını yoxlamaq üçün ikinci nömrənizi əlavə edin. | |
| `ai.invalidSaved` |  — неверный, удалите |  — yanlışdır, silin | |
| `ai.remove` | Удалить {{phone}} | {{phone}} sil | |
| `ai.add` | Добавить | Əlavə et | |
| `ai.notifyTitle` | Уведомления о заказах | Sifariş bildirişləri | |
| `ai.notifyText` | Номер, на который придут новые заказы и вопросы покупателей, где нужен ваш ответ. | Yeni sifarişlərin və sizin cavabınız lazım olan müştəri suallarının gələcəyi nömrə. | |
| `ai.notifyEmpty` | Номер не указан: уведомления приходят в чат «Вы» на номере магазина — без звука, их легко пропустить. | Nömrə göstərilməyib: bildirişlər mağaza nömrəsində «Siz» çatına səssiz gəlir, onları asanlıqla qaçırmaq olar. | |
| `ai.useMyNumber` | Использовать мой номер {{phone}} | Mənim nömrəmi istifadə et: {{phone}} | |
| `ai.sendTest` | Отправить тестовое уведомление | Test bildirişi göndər | |
| `dashboard.notFound` | Магазин не найден. | Mağaza tapılmadı. | |
| `dashboard.loadFailed` | Не удалось загрузить панель магазина. Попробуйте ещё раз. | Mağaza panelini yükləmək mümkün olmadı. Yenidən cəhd edin. | |
| `dashboard.loading` | Загружаем панель магазина... | Mağaza paneli yüklənir... | |
| `dashboard.errorTitle` | Не удалось открыть панель | Paneli açmaq mümkün olmadı | |
| `dashboard.logoutFailed` | Не удалось выйти. Проверьте соединение и повторите. | Çıxmaq mümkün olmadı. Bağlantını yoxlayıb yenidən cəhd edin. | |
| `dashboard.activeShop` | Активный магазин | Aktiv mağaza | |
| `dashboard.myShops` | Мои магазины | Mağazalarım | |
| `dashboard.products` | Товары | Məhsullar | |
| `dashboard.orders` | Заказы | Sifarişlər | |
| `testSeller.addMore` | Добавить ещё товары | Daha çox məhsul əlavə et | |
| `testSeller.connectWhatsapp` | Подключить WhatsApp | WhatsApp-ı qoş | |
| `testChat.example1` | Salam! Hansı məhsullar var? | Salam! Hansı məhsullar var? | |
| `testChat.example2` | Сколько стоит доставка? | Çatdırılma neçəyədir? | |
| `testChat.example3` | Можно примерить перед покупкой? | Almadan əvvəl geyinib baxmaq olar? | |
| `testChat.noAnswer` | AI не ответил. Попробуйте ещё раз. | AI cavab vermədi. Yenidən cəhd edin. | |
| `testChat.title` | Проверьте своего продавца | Satıcınızı yoxlayın | |
| `testChat.restart` | Начать заново | Yenidən başla | |
| `testChat.intro` | Напишите как покупатель. AI ответит по вашим товарам и условиям доставки — так же, как клиенту в WhatsApp. | Müştəri kimi yazın. AI məhsullarınıza və çatdırılma şərtlərinizə əsasən cavab verəcək — WhatsApp-da müştəriyə olduğu kimi. | |
| `testChat.placeholder` | Сообщение от покупателя… | Müştəridən mesaj… | |
| `testChat.send` | Отправить | Göndər | |
| `orders.statusNew` | Заявка | Sorğu | |
| `orders.statusConfirmed` | Подтверждён | Təsdiqlənib | |
| `orders.statusInDelivery` | В доставке | Çatdırılmada | |
| `orders.statusCompleted` | Выполнен | Tamamlanıb | |
| `orders.statusCancelled` | Отменён | Ləğv edilib | |
| `orders.request` | Заявка на заказ | Sifariş sorğusu | |
| `orders.order` | Заказ | Sifariş | |
| `orders.confirmHint` | Подтвердите покупателю наличие и окончательную стоимость. | Müştəriyə stoku və yekun qiyməti təsdiqləyin. | |
| `orders.status` | Статус: | Status: | |
| `orders.loadFailed` | Не удалось загрузить заказы. | Sifarişləri yükləmək mümkün olmadı. | |
| `orders.statusFailed` | Не удалось изменить статус. Попробуйте ещё раз. | Statusu dəyişmək mümkün olmadı. Yenidən cəhd edin. | |
| `orders.back` | Панель магазина | Mağaza paneli | |
| `orders.title` | Заказы | Sifarişlər | |
| `orders.active` | Активные ({{count}}) | Aktiv ({{count}}) | |
| `orders.all` | Все ({{count}}) | Hamısı ({{count}}) | |
| `orders.noActive` | Активных заказов нет. | Aktiv sifariş yoxdur. | |
| `orders.none` | Заказов пока нет. Их оформляет AI-продавец, когда клиент готов купить. | Hələ sifariş yoxdur. Müştəri almağa hazır olanda onları AI satıcı rəsmiləşdirir. | |
| `handoff.resumeFailed` | Не удалось вернуть AI. Попробуйте ещё раз. | AI-ni qaytarmaq mümkün olmadı. Yenidən cəhd edin. | |
| `handoff.title` | Нужен ответ продавца ({{count}}) | Satıcının cavabı lazımdır ({{count}}) | |
| `handoff.intro` | Эти клиенты ждут вашего ответа. Где AI на паузе, он молчит, пока вы не вернёте его (или 12 часов). Как только вы сами ответите клиенту в WhatsApp, AI в этом чате замолчит. | Bu müştərilər cavabınızı gözləyir. AI fasilədə olan çatlarda siz onu qaytarana qədər (və ya 12 saat) susur. Müştəriyə WhatsApp-da özünüz cavab verən kimi AI həmin çatda susacaq. | |
| `handoff.paused` | AI на паузе | AI fasilədədir | |
| `handoff.answering` | AI отвечает на другие вопросы | AI digər suallara cavab verir | |
| `handoff.write` | Написать в WhatsApp | WhatsApp-da yaz | |
| `handoff.resume` | Вернуть AI | AI-ni qaytar | |
| `handoff.resolve` | Отметить решённым | Həll olunmuş kimi qeyd et | |
| `catalog.addFailed` | Не удалось добавить товар. Попробуйте снова. | Məhsulu əlavə etmək mümkün olmadı. Yenidən cəhd edin. | |
| `catalog.title` | Добавьте первые товары | İlk məhsulları əlavə edin | |
| `catalog.intro` | ИИ-ассистенту нужны данные о ваших товарах, чтобы он мог отвечать на вопросы клиентов о ценах и наличии. | AI köməkçinin müştərilərin qiymət və stok suallarına cavab verməsi üçün məhsullarınız haqqında məlumat lazımdır. | |
| `catalog.newProduct` | Новый товар | Yeni məhsul | |
| `catalog.name` | Название товара | Məhsulun adı | |
| `catalog.namePlaceholder` | Например: Шелковое платье | Məsələn: İpək don | |
| `catalog.price` | Цена (AZN) | Qiymət (AZN) | |
| `catalog.description` | Описание | Təsvir | |
| `catalog.descriptionPlaceholder` | Размеры, состав, доступные цвета... | Ölçülər, tərkib, mövcud rənglər... | |
| `catalog.add` | Добавить в каталог | Kataloqa əlavə et | |
| `catalog.added` | Добавленные товары | Əlavə edilmiş məhsullar | |
| `catalog.empty` | Вы еще не добавили ни одного товара | Hələ heç bir məhsul əlavə etməmisiniz | |
| `catalog.noName` | Без названия | Adsız | |
| `catalog.noDescription` | Описание отсутствует | Təsvir yoxdur | |
| `catalog.skip` | Пропустить, добавлю позже | Keç, sonra əlavə edəcəm | |

## 2. Страница «Товары и доставка» и ошибки сервера

| Ключ | Русский | Azərbaycanca | ✓ |
|---|---|---|---|
| `inv.jsSaveFailed` | Не удалось сохранить изменения. Попробуйте ещё раз. | Dəyişiklikləri yadda saxlamaq mümkün olmadı. Yenidən cəhd edin. | |
| `inv.jsEditProduct` | Изменить товар | Məhsulu dəyiş | |
| `inv.jsDeleteConfirm` | Удалить этот товар? | Bu məhsul silinsin? | |
| `inv.tabProducts` | Товары | Məhsullar | |
| `inv.tabDelivery` | Доставка | Çatdırılma | |
| `inv.addProduct` | Добавить товар | Məhsul əlavə et | |
| `inv.addZone` | Добавить зону | Zona əlavə et | |
| `inv.yourProducts` | 📦 Ваши товары | 📦 Məhsullarınız | |
| `inv.newProduct` | Новый товар | Yeni məhsul | |
| `inv.saveProduct` | Сохранить товар | Məhsulu yadda saxla | |
| `inv.inStock` | В наличии | Stokda var | |
| `inv.outOfStock` | Нет в наличии | Stokda yoxdur | |
| `inv.unnamed` | Без названия | Adsız | |
| `inv.empty` | Товаров пока нет. Добавьте первый! | Hələ məhsul yoxdur. İlkini əlavə edin! | |
| `inv.deliveryTitle` | Условия доставки | Çatdırılma şərtləri | |
| `inv.secZones` | 1. Основное: зоны и цены | 1. Əsas: zonalar və qiymətlər | |
| `inv.zones` | Зоны доставки по Баку | Bakı üzrə çatdırılma zonaları | |
| `inv.zoneName` | Название зоны (например, Ясамал) | Zonanın adı (məsələn, Yasamal) | |
| `inv.zonePrice` | Цена | Qiymət | |
| `inv.freeFrom` | Бесплатная доставка от (AZN) | Pulsuz çatdırılma (AZN-dən) | |
| `inv.deliveryPrice` | Цена доставки (AZN) | Çatdırılma qiyməti (AZN) | |
| `inv.regions` | Доставка в регионы | Regionlara çatdırılma | |
| `inv.regionsPh` | Например: «В регионы отправляем Azerpost, 5 AZN, 2–3 дня» | Məsələn: «Regionlara Azerpost ilə göndəririk, 5 AZN, 2–3 gün» | |
| `inv.secShop` | 2. Магазин и время работы | 2. Mağaza və iş vaxtı | |
| `inv.address` | Адрес магазина | Mağazanın ünvanı | |
| `inv.addressPh` | Например: «Баку, ул. Низами 10» | Məsələn: «Bakı, Nizami küç. 10» | |
| `inv.hours` | Часы работы магазина | Mağazanın iş saatları | |
| `inv.hoursPh` | Например: «Пн–Пт 09:00–18:00» | Məsələn: «B.e.–Cümə 09:00–18:00» | |
| `inv.payment` | Способы оплаты | Ödəniş üsulları | |
| `inv.payCash` | Наличные | Nağd | |
| `inv.payCard` | Карта | Kart | |
| `inv.payTransfer` | Перевод | Köçürmə | |
| `inv.processing` | Сроки обработки заказа | Sifarişin emal müddəti | |
| `inv.processingPh` | Например: «Заказы до 17:00 доставляем сегодня, позже — завтра до обеда» | Məsələn: «17:00-dək sifarişləri bu gün, sonrakıları sabah günortayadək çatdırırıq» | |
| `inv.deliveryHours` | Часы и дни доставки | Çatdırılma saatları və günləri | |
| `inv.deliveryHoursPh` | Например: «С 10:00 до 20:00, в воскресенье без доставки» | Məsələn: «10:00-dan 20:00-dək, bazar günü çatdırılma yoxdur» | |
| `inv.secAsk` | 3. Что AI спрашивает у клиента при заказе | 3. Sifariş zamanı AI müştəridən nə soruşur | |
| `inv.askPhone` | Номер телефона (+994) | Telefon nömrəsi (+994) | |
| `inv.askAddress` | Точный адрес (улица и т.д.) | Dəqiq ünvan (küçə və s.) | |
| `inv.askLandmark` | Метро / ориентир | Metro / orientir | |
| `inv.askLocation` | Геолокация в WhatsApp | WhatsApp-da geolokasiya | |
| `inv.secFitting` | 4. Примерка и возврат | 4. Geyinib baxma və qaytarma | |
| `inv.courierWait` | Ожидание курьера (мин) | Kuryerin gözləməsi (dəq) | |
| `inv.fittingAllowed` | Примерка разрешена? | Geyinib baxmaq olar? | |
| `inv.yes` | Да | Bəli | |
| `inv.no` | Нет | Xeyr | |
| `inv.refusalFee` | Плата за отказ после примерки (AZN) | Geyindikdən sonra imtina haqqı (AZN) | |
| `inv.fittingRules` | Полные правила примерки и возврата | Geyinib baxma və qaytarmanın tam qaydaları | |
| `inv.fittingRulesPh` | Например: «Можно примерить до 3 вещей. Если ничего не купили — 3 AZN за выезд. Вернуть товар можно в течение 14 дней в исходном виде» | Məsələn: «3 əşyaya qədər geyinib baxmaq olar. Heç nə almasanız — gəliş üçün 3 AZN. Məhsulu ilkin vəziyyətdə 14 gün ərzində qaytarmaq olar» | |
| `inv.saveDelivery` | Сохранить условия доставки | Çatdırılma şərtlərini yadda saxla | |
| `inv.fName` | Название | Ad | |
| `inv.fNamePh` | Введите название | Adı daxil edin | |
| `inv.fPrice` | Цена (AZN) | Qiymət (AZN) | |
| `inv.fMore` | Дополнительно | Əlavə | |
| `inv.fSku` | Артикул | Artikul | |
| `inv.fSkuPh` | Уникальный код | Unikal kod | |
| `inv.fDescription` | Описание | Təsvir | |
| `inv.fDescriptionPh` | Подробности… | Təfərrüatlar… | |
| `inv.fCost` | Себестоимость | Maya dəyəri | |
| `inv.fCurrency` | Валюта | Valyuta | |
| `inv.fBrand` | Бренд | Brend | |
| `inv.fBrandPh` | Название бренда | Brendin adı | |
| `inv.fBarcode` | Штрихкод | Ştrix-kod | |
| `inv.fStock` | Остаток | Qalıq | |
| `inv.fStockPh` | Кол-во | Say | |
| `inv.fWeight` | Вес (кг) | Çəki (kq) | |
| `inv.fSize` | Размер | Ölçü | |
| `inv.fPhoto` | Фото товара | Məhsulun şəkli | |
| `inv.fPhotoUrl` | Ссылка на фото | Şəklin linki | |
| `inv.fUnit` | Единица | Vahid | |
| `inv.fUnitPh` | пара, шт, кг | cüt, əd, kq | |
| `inv.fOnSale` | В продаже | Satışdadır | |
| `error.emailReserved` | Эта почта зарезервирована | Bu e-poçt rezerv edilib | |
| `error.emailTaken` | Пользователь с такой почтой уже зарегистрирован | Bu e-poçtla istifadəçi artıq qeydiyyatdan keçib | |
| `error.termsRequired` | Примите условия пилота, чтобы зарегистрироваться | Qeydiyyatdan keçmək üçün pilot şərtlərini qəbul edin | |
| `error.invalidPhone` | Неверный номер: азербайджанский номер — 12 цифр, например +994 50 123 45 67 | Nömrə yanlışdır: Azərbaycan nömrəsi 12 rəqəmdir, məsələn +994 50 123 45 67 | |
| `error.whatsappNotConnected` | Сначала подключите WhatsApp магазина | Əvvəlcə mağazanın WhatsApp-ını qoşun | |
| `error.shopNumberUnknown` | Номер магазина ещё не известен, обновите страницу | Mağazanın nömrəsi hələ məlum deyil, səhifəni yeniləyin | |
| `error.whatsappRejected` | WhatsApp не принял сообщение, попробуйте ещё раз | WhatsApp mesajı qəbul etmədi, yenidən cəhd edin | |
| `error.whatsappNotPrepared` | WhatsApp магазина ещё не подготовлен | Mağazanın WhatsApp-ı hələ hazır deyil | |
