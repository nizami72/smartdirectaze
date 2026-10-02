import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/api.ts';
import { AlertTriangle, Bot, CheckCircle2, Loader2, Plus, Send, X } from 'lucide-react';
import { normalizePhone, prettyPhone, PHONE_HINT } from '../../../utils/utils';

const MODES = [
  { value: 'OFF', label: 'Выключен', hint: 'AI молчит, вы отвечаете клиентам сами.' },
  { value: 'TEST', label: 'Тест', hint: 'AI отвечает только номерам из списка ниже. Остальные клиенты его не видят.' },
  { value: 'ON', label: 'Для всех', hint: 'AI отвечает всем клиентам, которые пишут на ваш WhatsApp.' },
];

// Settings of the AI seller on the shop's WhatsApp: who it answers
const AiSettingsCard = ({ shopId }) => {
  const navigate = useNavigate();
  const [settings, setSettings] = useState(null);
  const [newPhone, setNewPhone] = useState('');
  const [notificationPhone, setNotificationPhone] = useState('');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [messageIsError, setMessageIsError] = useState(false);
  // "For everyone" is switched on only after the merchant sees which number the AI will answer on
  const [confirmOn, setConfirmOn] = useState(false);
  const [phoneError, setPhoneError] = useState('');
  const [notificationError, setNotificationError] = useState('');
  const [testResult, setTestResult] = useState(null); // { ok, text }
  const [testing, setTesting] = useState(false);

  useEffect(() => {
    if (!shopId) return;
    api.get(`/api/v1/shops/channels/whatsapp/ai?shopId=${shopId}`)
      .then(res => {
        setSettings(res.data);
        setNotificationPhone(prettyPhone(res.data.notificationPhone));
      })
      .catch(() => setSettings(null));
  }, [shopId]);

  // Returns true when saved; the server's own explanation is shown when it refuses
  const save = async (next) => {
    setSaving(true);
    setMessage('');
    try {
      const res = await api.put(`/api/v1/shops/channels/whatsapp/ai?shopId=${shopId}`, next);
      setSettings(res.data);
      setNotificationPhone(prettyPhone(res.data.notificationPhone));
      setMessage('Сохранено');
      setMessageIsError(false);
      return true;
    } catch (err) {
      setMessage(err.response?.data?.message || 'Не удалось сохранить, попробуйте ещё раз');
      setMessageIsError(true);
      return false;
    } finally {
      setSaving(false);
    }
  };

  const addPhone = async (e) => {
    e.preventDefault();
    if (!newPhone.trim()) return;
    const phone = normalizePhone(newPhone);
    if (!phone) {
      setPhoneError(`Неверный номер: ${PHONE_HINT}`);
      return;
    }
    setPhoneError('');
    if (await save({ ...settings, testPhones: [...new Set([...settings.testPhones, phone])] })) setNewPhone('');
  };

  const saveNotificationPhone = async (value) => {
    setTestResult(null);
    if (!value.trim()) {
      setNotificationError('');
      return save({ ...settings, notificationPhone: '' });
    }
    const phone = normalizePhone(value);
    if (!phone) {
      setNotificationError(`Неверный номер: ${PHONE_HINT}`);
      return false;
    }
    setNotificationError('');
    return save({ ...settings, notificationPhone: phone });
  };

  const sendTest = async () => {
    setTesting(true);
    setTestResult(null);
    try {
      const res = await api.post(`/api/v1/shops/channels/whatsapp/test-notification?shopId=${shopId}`);
      setTestResult({ ok: true, text: `Отправлено на ${prettyPhone(res.data.sentTo)}. Проверьте WhatsApp на этом телефоне.` });
    } catch (err) {
      setTestResult({ ok: false, text: err.response?.data?.message || 'Не удалось отправить, попробуйте ещё раз' });
    } finally {
      setTesting(false);
    }
  };

  const removePhone = (phone) =>
    save({ ...settings, testPhones: settings.testPhones.filter(p => p !== phone) });

  // No WhatsApp channel for this shop yet
  if (!settings) return null;

  const currentMode = MODES.find(m => m.value === settings.aiMode);

  return (
    <section className="bg-white rounded-3xl border border-slate-200 p-6 mb-8 text-left">
      <div className="flex items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center">
            <Bot className="w-5 h-5 text-emerald-600" />
          </div>
          <h2 className="text-lg font-bold text-slate-900">AI-продавец в WhatsApp</h2>
        </div>
        {saving && <Loader2 className="w-5 h-5 text-slate-400 animate-spin" />}
      </div>

      {settings.channelStatus === 'CONNECTED' ? (
        <p className="flex items-center gap-2 text-sm text-emerald-700 mb-4">
          <CheckCircle2 className="w-4 h-4" />
          WhatsApp подключён{settings.connectedPhone ? `: +${settings.connectedPhone}` : ''}
        </p>
      ) : (
        <div className="flex flex-wrap items-center justify-between gap-2 bg-amber-50 border border-amber-200 rounded-2xl px-4 py-3 mb-4">
          <p className="flex items-center gap-2 text-sm text-amber-800">
            <AlertTriangle className="w-4 h-4" />
            WhatsApp не подключён — AI не сможет отвечать клиентам.
          </p>
          <button type="button" onClick={() => navigate(`/shops/${shopId}/connect`)}
                  className="text-sm font-semibold text-amber-900 underline">
            Подключить
          </button>
        </div>
      )}

      <div className="grid grid-cols-3 gap-2 bg-slate-100 p-1 rounded-2xl">
        {MODES.map(mode => (
          <button
            key={mode.value}
            type="button"
            disabled={saving}
            onClick={() => {
              if (mode.value === 'ON' && settings.aiMode !== 'ON') { setConfirmOn(true); return; }
              setConfirmOn(false);
              save({ ...settings, aiMode: mode.value });
            }}
            className={`py-2 px-3 rounded-xl text-sm font-semibold transition-all ${
              settings.aiMode === mode.value ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            {mode.label}
          </button>
        ))}
      </div>
      {confirmOn ? (
        <div className="mt-3 bg-amber-50 border border-amber-200 rounded-2xl p-4">
          <p className="text-sm text-amber-900">
            AI будет отвечать всем, кто пишет на{' '}
            {settings.connectedPhone
              ? <b className="whitespace-nowrap">+{settings.connectedPhone}</b>
              : 'подключённый номер WhatsApp'}
            . Это номер, на который пишут ваши покупатели?
          </p>
          <p className="text-sm text-amber-900 mt-2">
            Не отправляйте с этого номера рассылки незнакомым людям — это главный повод для блокировки номера в WhatsApp.
          </p>
          <div className="flex flex-wrap gap-2 mt-3">
            <button type="button" disabled={saving}
                    onClick={() => { setConfirmOn(false); save({ ...settings, aiMode: 'ON' }); }}
                    className="bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-xl px-4 py-2 text-sm font-semibold">
              Да, включить для всех
            </button>
            <button type="button" onClick={() => setConfirmOn(false)}
                    className="bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 rounded-xl px-4 py-2 text-sm font-semibold">
              Отмена
            </button>
          </div>
        </div>
      ) : (
        <p className="text-sm text-slate-500 mt-3">{currentMode?.hint}</p>
      )}

      {settings.aiMode === 'TEST' && (
        <div className="mt-5">
          <p className="text-sm font-semibold text-slate-700 mb-2">Тестовые номера</p>
          {settings.testPhones.length === 0 && (
            <p className="text-sm text-amber-600 mb-2">Добавьте свой второй номер, чтобы проверить ответы AI.</p>
          )}
          <div className="flex flex-wrap gap-2 mb-3">
            {settings.testPhones.map(phone => (
              <span key={phone} title={normalizePhone(phone) === phone ? '' : `Неверный номер: ${PHONE_HINT}`}
                    className={`inline-flex items-center gap-1 rounded-full pl-3 pr-1 py-1 text-sm ${
                      normalizePhone(phone) === phone ? 'bg-slate-100 text-slate-700' : 'bg-red-50 text-red-700 ring-1 ring-red-200'
                    }`}>
                {prettyPhone(phone)}{normalizePhone(phone) === phone ? '' : ' — неверный, удалите'}
                <button type="button" onClick={() => removePhone(phone)} disabled={saving}
                        className="p-1 rounded-full hover:bg-slate-200" aria-label={`Удалить ${prettyPhone(phone)}`}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>
          <form onSubmit={addPhone} className="flex gap-2">
            <input
              type="tel"
              value={newPhone}
              onChange={e => { setNewPhone(e.target.value); setPhoneError(''); }}
              placeholder="+994 55 123 45 67"
              className="flex-1 min-w-0 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <button type="submit" disabled={saving || !newPhone.trim()}
                    className="inline-flex items-center gap-1 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-xl px-4 py-2 text-sm font-semibold">
              <Plus className="w-4 h-4" />
              Добавить
            </button>
          </form>
          {phoneError && <p className="text-sm text-red-600 mt-2">{phoneError}</p>}
        </div>
      )}

      <div className="mt-6 pt-5 border-t border-slate-100">
        <p className="text-sm font-semibold text-slate-700 mb-1">Уведомления о заказах</p>
        <p className="text-sm text-slate-500 mb-2">
          Номер, на который придут новые заказы и вопросы покупателей, где нужен ваш ответ.
        </p>
        {!settings.notificationPhone && (
          <div className="bg-amber-50 border border-amber-200 rounded-2xl px-4 py-3 mb-3">
            <p className="flex items-start gap-2 text-sm text-amber-900">
              <AlertTriangle className="w-4 h-4 mt-0.5 shrink-0" />
              Номер не указан: уведомления приходят в чат «Вы» на номере магазина — без звука, их легко пропустить.
            </p>
            {settings.ownerPhone && (
              <button type="button" disabled={saving}
                      onClick={() => saveNotificationPhone(settings.ownerPhone)}
                      className="mt-2 text-sm font-semibold text-amber-900 underline disabled:opacity-50">
                Использовать мой номер {prettyPhone(settings.ownerPhone)}
              </button>
            )}
          </div>
        )}
        <form className="flex gap-2"
              onSubmit={e => { e.preventDefault(); saveNotificationPhone(notificationPhone); }}>
          <input
            type="tel"
            value={notificationPhone}
            onChange={e => { setNotificationPhone(e.target.value); setNotificationError(''); }}
            placeholder="+994 55 123 45 67"
            className="flex-1 min-w-0 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />
          <button type="submit" disabled={saving}
                  className="bg-slate-900 hover:bg-slate-800 disabled:opacity-50 text-white rounded-xl px-4 py-2 text-sm font-semibold">
            Сохранить
          </button>
        </form>
        {notificationError && <p className="text-sm text-red-600 mt-2">{notificationError}</p>}

        {settings.channelStatus === 'CONNECTED' && (
          <div className="mt-3">
            <button type="button" onClick={sendTest} disabled={testing}
                    className="inline-flex items-center gap-1.5 text-sm font-semibold text-emerald-700 hover:underline disabled:opacity-50">
              {testing ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
              Отправить тестовое уведомление
            </button>
            {testResult && (
              <p className={`text-sm mt-1 ${testResult.ok ? 'text-emerald-700' : 'text-red-600'}`}>{testResult.text}</p>
            )}
          </div>
        )}
      </div>

      {message && <p className={`mt-3 ${messageIsError ? 'text-sm text-red-600' : 'text-xs text-slate-400'}`}>{message}</p>}
    </section>
  );
};

export default AiSettingsCard;
