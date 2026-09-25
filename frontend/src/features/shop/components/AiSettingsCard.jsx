import { useEffect, useState } from 'react';
import api from '../../../api/api.ts';
import { Bot, Loader2, Plus, X } from 'lucide-react';

const MODES = [
  { value: 'OFF', label: 'Выключен', hint: 'AI молчит, вы отвечаете клиентам сами.' },
  { value: 'TEST', label: 'Тест', hint: 'AI отвечает только номерам из списка ниже. Остальные клиенты его не видят.' },
  { value: 'ON', label: 'Для всех', hint: 'AI отвечает всем клиентам, которые пишут на ваш WhatsApp.' },
];

// Settings of the AI seller on the shop's WhatsApp: who it answers
const AiSettingsCard = ({ shopId }) => {
  const [settings, setSettings] = useState(null);
  const [newPhone, setNewPhone] = useState('');
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (!shopId) return;
    api.get(`/api/v1/shops/channels/whatsapp/ai?shopId=${shopId}`)
      .then(res => setSettings(res.data))
      .catch(() => setSettings(null));
  }, [shopId]);

  const save = async (next) => {
    setSaving(true);
    setMessage('');
    try {
      const res = await api.put(`/api/v1/shops/channels/whatsapp/ai?shopId=${shopId}`, next);
      setSettings(res.data);
      setMessage('Сохранено');
    } catch {
      setMessage('Не удалось сохранить, попробуйте ещё раз');
    } finally {
      setSaving(false);
    }
  };

  const addPhone = (e) => {
    e.preventDefault();
    const phone = newPhone.replace(/\D/g, '');
    if (!phone) return;
    setNewPhone('');
    save({ ...settings, testPhones: [...new Set([...settings.testPhones, phone])] });
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

      <div className="grid grid-cols-3 gap-2 bg-slate-100 p-1 rounded-2xl">
        {MODES.map(mode => (
          <button
            key={mode.value}
            type="button"
            disabled={saving}
            onClick={() => save({ ...settings, aiMode: mode.value })}
            className={`py-2 px-3 rounded-xl text-sm font-semibold transition-all ${
              settings.aiMode === mode.value ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            {mode.label}
          </button>
        ))}
      </div>
      <p className="text-sm text-slate-500 mt-3">{currentMode?.hint}</p>

      {settings.aiMode === 'TEST' && (
        <div className="mt-5">
          <p className="text-sm font-semibold text-slate-700 mb-2">Тестовые номера</p>
          {settings.testPhones.length === 0 && (
            <p className="text-sm text-amber-600 mb-2">Добавьте свой второй номер, чтобы проверить ответы AI.</p>
          )}
          <div className="flex flex-wrap gap-2 mb-3">
            {settings.testPhones.map(phone => (
              <span key={phone} className="inline-flex items-center gap-1 bg-slate-100 rounded-full pl-3 pr-1 py-1 text-sm text-slate-700">
                +{phone}
                <button type="button" onClick={() => removePhone(phone)} disabled={saving}
                        className="p-1 rounded-full hover:bg-slate-200" aria-label={`Удалить +${phone}`}>
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))}
          </div>
          <form onSubmit={addPhone} className="flex gap-2">
            <input
              type="tel"
              value={newPhone}
              onChange={e => setNewPhone(e.target.value)}
              placeholder="+994 55 123 45 67"
              className="flex-1 min-w-0 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
            />
            <button type="submit" disabled={saving || !newPhone.trim()}
                    className="inline-flex items-center gap-1 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-xl px-4 py-2 text-sm font-semibold">
              <Plus className="w-4 h-4" />
              Добавить
            </button>
          </form>
        </div>
      )}

      {message && <p className="text-xs text-slate-400 mt-3">{message}</p>}
    </section>
  );
};

export default AiSettingsCard;
