import { useState } from 'react';
import { Link } from 'react-router-dom';

// Version of the text below; stored with each registration. Change it whenever the text changes.
export const TERMS_VERSION = '2026-10-02';

// Who runs SmartDirect and how to reach them (shown in the text as is)
const OPERATOR = 'Nizami';
const CONTACT = 'WhatsApp +994 50 467 99 33';
const PILOT_DAYS = 30;
const DELETE_DAYS = 30;

const Placeholder = ({ children }) =>
  children.startsWith('[') ? <mark className="bg-amber-200 px-1 rounded">{children}</mark> : <b>{children}</b>;

const TEXT = {
  ru: {
    title: 'Условия пилота SmartDirect',
    intro: <>Эти условия действуют между владельцем магазина («мерчант») и SmartDirect (<Placeholder>{OPERATOR}</Placeholder>) на время пилота — пробного периода работы сервиса.</>,
    sections: [
      ['1. Что такое пилот', <>
        SmartDirect подключает к WhatsApp магазина AI-продавца, который отвечает покупателям: цены, наличие, доставка, оформление заказа.
        Пилот длится <b>{PILOT_DAYS} дней</b> и <b>бесплатен</b> для мерчанта. Сервис ещё дорабатывается, поэтому от мерчанта ждём обратную связь:
        что работает, а что нет.
      </>],
      ['2. Что делает AI и за что отвечает мерчант', <>
        AI отвечает только по каталогу и настройкам магазина, которые заполнил мерчант, и может ошибаться.
        Мерчант отвечает за правильность цен, наличия, условий доставки и оплаты. Заказ становится сделкой только после того,
        как продавец подтвердил его покупателю. Мерчант видит все ответы AI в WhatsApp и может в любой момент ответить сам — тогда AI в этом чате замолкает.
      </>],
      ['3. Риск для номера WhatsApp', <>
        SmartDirect подключается к WhatsApp как связанное устройство, через сервис Green API. Это не официальный сервис WhatsApp:
        есть небольшой риск, что WhatsApp ограничит или заблокирует номер. SmartDirect не отвечает за такие ограничения.
        Чтобы снизить риск, подключайте номер, который давно работает в WhatsApp, держите телефон в сети и не делайте с него рассылок
        людям, которые вам не писали.
      </>],
      ['4. Какие данные хранятся', <>
        Чтобы отвечать покупателям и принимать заказы, SmartDirect хранит переписку с покупателями, их имена, номера телефонов
        и адреса доставки из заказов, а также каталог и настройки магазина. Данные используются только для работы сервиса
        и не передаются для рекламы. Оператор SmartDirect может просматривать их, чтобы помочь мерчанту или исправить ошибку.
        Мерчант отвечает за то, чтобы его покупатели знали, что на сообщения магазина может отвечать AI.
      </>],
      ['5. Внешние сервисы', <>
        Для работы SmartDirect использует:
        <ul className="list-disc pl-5 mt-2 space-y-1">
          <li><b>Hetzner</b> — сервер в Финляндии, где хранятся данные;</li>
          <li><b>Green API</b> — передача сообщений WhatsApp;</li>
          <li><b>DeepSeek</b> — AI-модель, которая составляет ответы. <b>Тексты сообщений покупателей отправляются ей</b> для составления ответа.</li>
        </ul>
      </>],
      ['6. Условия пилота', <>
        Сервис предоставляется «как есть», без гарантии бесперебойной работы. Он может ненадолго останавливаться для обновлений.
        Оплату инстанса Green API стороны согласуют отдельно.
      </>],
      ['7. Окончание пилота и удаление данных', <>
        Любая сторона может закончить пилот в любой момент. После окончания номер отключается от SmartDirect, а данные магазина
        и его покупателей удаляются по просьбе мерчанта в течение <b>{DELETE_DAYS} дней</b>.
      </>],
      ['8. Связь', <>Вопросы, просьба удалить данные или закончить пилот: <Placeholder>{CONTACT}</Placeholder>.</>],
    ],
    version: 'Версия условий',
    back: 'Вернуться к регистрации',
  },
  az: {
    title: 'SmartDirect pilot şərtləri',
    intro: <>Bu şərtlər pilot müddətində — xidmətin sınaq dövründə mağaza sahibi («satıcı») ilə SmartDirect (<Placeholder>{OPERATOR}</Placeholder>) arasında qüvvədədir.</>,
    sections: [
      ['1. Pilot nədir', <>
        SmartDirect mağazanın WhatsApp-ına müştərilərə cavab verən AI satıcı qoşur: qiymət, stok, çatdırılma, sifarişin rəsmiləşdirilməsi.
        Pilot <b>{PILOT_DAYS} gün</b> davam edir və satıcı üçün <b>pulsuzdur</b>. Xidmət hələ təkmilləşdirilir, ona görə satıcıdan rəy gözləyirik:
        nə işləyir, nə işləmir.
      </>],
      ['2. AI nə edir və satıcı nəyə cavabdehdir', <>
        AI yalnız satıcının doldurduğu kataloq və mağaza ayarlarına əsasən cavab verir və səhv edə bilər.
        Qiymətlərin, stokun, çatdırılma və ödəniş şərtlərinin düzgünlüyünə satıcı cavabdehdir. Sifariş yalnız satıcı onu müştəriyə
        təsdiq etdikdən sonra sövdələşmə sayılır. Satıcı AI-nin bütün cavablarını WhatsApp-da görür və istənilən an özü cavab verə bilər —
        o zaman AI həmin çatda susur.
      </>],
      ['3. WhatsApp nömrəsi üçün risk', <>
        SmartDirect WhatsApp-a Green API xidməti vasitəsilə əlaqəli cihaz kimi qoşulur. Bu, WhatsApp-ın rəsmi xidməti deyil:
        WhatsApp-ın nömrəni məhdudlaşdırması və ya bloklaması üçün kiçik risk var. SmartDirect belə məhdudiyyətlərə görə məsuliyyət daşımır.
        Riski azaltmaq üçün çoxdan WhatsApp-da işləyən nömrəni qoşun, telefonu şəbəkədə saxlayın və sizə yazmayan insanlara bu nömrədən
        kütləvi mesaj göndərməyin.
      </>],
      ['4. Hansı məlumatlar saxlanılır', <>
        Müştərilərə cavab vermək və sifariş qəbul etmək üçün SmartDirect müştərilərlə yazışmanı, onların adlarını, telefon nömrələrini
        və sifarişlərdəki çatdırılma ünvanlarını, həmçinin mağazanın kataloqunu və ayarlarını saxlayır. Məlumatlar yalnız xidmətin
        işi üçün istifadə olunur və reklam üçün ötürülmür. SmartDirect operatoru satıcıya kömək etmək və ya səhvi düzəltmək üçün onlara baxa bilər.
        Satıcı müştərilərinin mağazanın mesajlarına AI-nin cavab verə biləcəyini bilməsinə cavabdehdir.
      </>],
      ['5. Xarici xidmətlər', <>
        SmartDirect işləmək üçün bunlardan istifadə edir:
        <ul className="list-disc pl-5 mt-2 space-y-1">
          <li><b>Hetzner</b> — məlumatların saxlandığı Finlandiyada server;</li>
          <li><b>Green API</b> — WhatsApp mesajlarının ötürülməsi;</li>
          <li><b>DeepSeek</b> — cavabları hazırlayan AI modeli. <b>Müştəri mesajlarının mətnləri</b> cavab hazırlamaq üçün <b>ona göndərilir</b>.</li>
        </ul>
      </>],
      ['6. Pilotun şərtləri', <>
        Xidmət «olduğu kimi», fasiləsiz işləmə zəmanəti olmadan təqdim olunur. Yeniləmələr üçün qısa müddətə dayana bilər.
        Green API instansının ödənişi tərəflər arasında ayrıca razılaşdırılır.
      </>],
      ['7. Pilotun bitməsi və məlumatların silinməsi', <>
        İstənilən tərəf pilotu istənilən an bitirə bilər. Bitdikdən sonra nömrə SmartDirect-dən ayrılır, mağazanın və müştərilərinin
        məlumatları satıcının xahişi ilə <b>{DELETE_DAYS} gün</b> ərzində silinir.
      </>],
      ['8. Əlaqə', <>Suallar, məlumatların silinməsi və ya pilotu bitirmək üçün: <Placeholder>{CONTACT}</Placeholder>.</>],
    ],
    version: 'Şərtlərin versiyası',
    back: 'Qeydiyyata qayıt',
  },
};

// Public page with the pilot terms; the registration form links here and requires accepting them
const TermsPage = () => {
  const [lang, setLang] = useState('ru');
  const t = TEXT[lang];

  return (
    <div className="min-h-screen bg-[#f8fafc] px-4 py-10 font-sans">
      <article className="max-w-2xl mx-auto bg-white rounded-2xl border border-slate-100 shadow-sm p-6 sm:p-10 text-left">
        <div className="flex items-center justify-between gap-4 mb-6">
          <h1 className="text-2xl font-bold text-slate-900">{t.title}</h1>
          <div className="flex shrink-0 bg-slate-100 p-1 rounded-xl text-sm font-semibold">
            {[['az', 'AZ'], ['ru', 'RU']].map(([code, label]) => (
              <button key={code} type="button" onClick={() => setLang(code)}
                      className={`px-3 py-1 rounded-lg ${lang === code ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500'}`}>
                {label}
              </button>
            ))}
          </div>
        </div>
        <p className="text-slate-700 leading-relaxed mb-6">{t.intro}</p>
        <div className="space-y-5">
          {t.sections.map(([heading, body]) => (
            <section key={heading}>
              <h2 className="font-bold text-slate-900 mb-1">{heading}</h2>
              <div className="text-slate-700 leading-relaxed text-sm">{body}</div>
            </section>
          ))}
        </div>
        <p className="text-xs text-slate-400 mt-8">{t.version}: {TERMS_VERSION}</p>
        <Link to="/register" className="inline-block mt-4 text-sm font-semibold text-slate-900 underline underline-offset-4">
          {t.back}
        </Link>
      </article>
    </div>
  );
};

export default TermsPage;
