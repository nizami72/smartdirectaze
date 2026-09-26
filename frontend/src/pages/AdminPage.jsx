import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';
import { ArrowLeft, Loader2 } from 'lucide-react';

const STATUS = {
  CONNECTED: { label: 'Подключён', className: 'bg-emerald-100 text-emerald-800' },
  WAITING_QR: { label: 'Ждёт QR', className: 'bg-blue-100 text-blue-800' },
  DISCONNECTED: { label: 'Отключён', className: 'bg-red-100 text-red-700' },
  WAITING_FOR_INSTANCE: { label: 'Нет инстанса', className: 'bg-slate-100 text-slate-600' },
};

const inputClass = 'w-full border border-slate-200 rounded-lg px-2.5 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500';

const ShopRow = ({ shop, onChanged }) => {
  const [instanceId, setInstanceId] = useState('');
  const [apiToken, setApiToken] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const status = STATUS[shop.channelStatus] || { label: shop.channelStatus || '—', className: 'bg-slate-100 text-slate-600' };

  const run = async (request) => {
    setBusy(true);
    setError('');
    try {
      onChanged((await request()).data);
      setInstanceId('');
      setApiToken('');
    } catch (err) {
      setError(err.response?.data?.message || 'Не получилось. Попробуйте ещё раз.');
    } finally {
      setBusy(false);
    }
  };

  const bind = (e) => {
    e.preventDefault();
    run(() => api.put(`/api/v1/admin/whatsapp/shops/${shop.shopId}`, { instanceId, apiToken }));
  };

  const unbind = () => {
    if (!confirm(`Отвязать инстанс ${shop.instanceId} от «${shop.shopName}»? Номер выйдет из WhatsApp, AI перестанет отвечать.`)) return;
    run(() => api.delete(`/api/v1/admin/whatsapp/shops/${shop.shopId}`));
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-4 text-left">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <p className="font-bold text-slate-900">#{shop.shopId} {shop.shopName}</p>
          <p className="text-xs text-slate-500">{shop.ownerEmail || 'владелец не найден'} · AI: {shop.aiMode || '—'}</p>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${status.className}`}>{status.label}</span>
      </div>

      {shop.instanceId ? (
        <div className="mt-3 flex flex-wrap items-center justify-between gap-2 text-sm text-slate-700">
          <span>
            Инстанс <b>{shop.instanceId}</b>{shop.connectedPhone ? ` · номер +${shop.connectedPhone}` : ''}
          </span>
          <button type="button" onClick={unbind} disabled={busy}
                  className="text-sm font-semibold text-red-600 hover:underline disabled:opacity-50">
            Отвязать
          </button>
        </div>
      ) : (
        <form onSubmit={bind} className="mt-3 grid grid-cols-1 sm:grid-cols-[1fr_2fr_auto] gap-2">
          <input className={inputClass} placeholder="idInstance" value={instanceId} onChange={e => setInstanceId(e.target.value)} required />
          <input className={inputClass} placeholder="apiTokenInstance" value={apiToken} onChange={e => setApiToken(e.target.value)} required />
          <button type="submit" disabled={busy}
                  className="inline-flex items-center justify-center gap-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg px-3 py-1.5 text-sm font-semibold disabled:opacity-50">
            {busy && <Loader2 className="w-4 h-4 animate-spin" />}
            Привязать
          </button>
        </form>
      )}
      {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
    </div>
  );
};

// Operator page: Green API instances of shops (ADMIN_EMAILS only)
const AdminPage = () => {
  const navigate = useNavigate();
  const [shops, setShops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/v1/admin/whatsapp/shops')
      .then(res => setShops(res.data || []))
      .catch(err => setError(err.response?.status === 403 ? 'Эта страница только для администраторов.' : 'Не удалось загрузить магазины.'))
      .finally(() => setLoading(false));
  }, []);

  const replace = (updated) => setShops(prev => prev.map(s => (s.shopId === updated.shopId ? updated : s)));

  return (
    <div className="min-h-screen bg-[#F3F4F6] font-sans">
      <div className="max-w-4xl mx-auto px-4 py-8 text-left">
        <button type="button" onClick={() => navigate('/shops')}
                className="flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-slate-900 mb-4">
          <ArrowLeft className="w-4 h-4" /> Мои магазины
        </button>
        <p className="text-2xl font-bold text-slate-900">Админ: WhatsApp магазинов</p>
        <p className="text-sm text-slate-500 mt-1 mb-6">
          Привязка настраивает вебхуки инстанса в Green API автоматически. Затем мерчант сканирует QR у себя на странице «WhatsApp».
        </p>
        {loading && <Loader2 className="w-8 h-8 text-slate-400 animate-spin" />}
        {error && <p className="text-red-600">{error}</p>}
        <div className="space-y-3">
          {shops.map(shop => <ShopRow key={shop.shopId} shop={shop} onChanged={replace} />)}
        </div>
      </div>
    </div>
  );
};

export default AdminPage;
