import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/api.ts';
import { ArrowLeft, Loader2, MapPin, MessageCircle, Package, Phone, Wallet } from 'lucide-react';

const ORDER_STATUSES = [
  { value: 'NEW', label: 'Новый', className: 'bg-amber-100 text-amber-800' },
  { value: 'CONFIRMED', label: 'Подтверждён', className: 'bg-blue-100 text-blue-800' },
  { value: 'IN_DELIVERY', label: 'В доставке', className: 'bg-violet-100 text-violet-800' },
  { value: 'COMPLETED', label: 'Выполнен', className: 'bg-emerald-100 text-emerald-800' },
  { value: 'CANCELLED', label: 'Отменён', className: 'bg-slate-100 text-slate-600' },
];

const ACTIVE = ['NEW', 'CONFIRMED', 'IN_DELIVERY'];

const formatDate = (value) =>
  value ? new Date(value).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '';

// wa.me needs digits only
const whatsappLink = (phone) => `https://wa.me/${(phone || '').replace(/\D/g, '')}`;

const OrderCard = ({ order, onStatusChange, saving }) => {
  const status = ORDER_STATUSES.find(s => s.value === order.status) || ORDER_STATUSES[0];
  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 text-left">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <p className="font-bold text-slate-900">Заказ #{order.id}</p>
          <p className="text-xs text-slate-400">{formatDate(order.createdAt)}</p>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${status.className}`}>{status.label}</span>
      </div>

      <p className="font-semibold text-slate-800">{order.customerName}</p>
      <div className="mt-2 space-y-1.5 text-sm text-slate-600">
        {order.phoneNumber && (
          <div className="flex items-center gap-2">
            <Phone className="w-4 h-4 text-slate-400 flex-shrink-0" />
            <a href={`tel:${order.phoneNumber}`} className="hover:underline">{order.phoneNumber}</a>
            <a href={whatsappLink(order.phoneNumber)} target="_blank" rel="noopener noreferrer"
               className="inline-flex items-center gap-1 text-emerald-700 font-semibold hover:underline">
              <MessageCircle className="w-4 h-4" /> WhatsApp
            </a>
          </div>
        )}
        {order.deliveryAddress && (
          <div className="flex items-start gap-2">
            <MapPin className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
            <span>{order.deliveryAddress}</span>
          </div>
        )}
        {order.itemsSummary && (
          <div className="flex items-start gap-2">
            <Package className="w-4 h-4 text-slate-400 flex-shrink-0 mt-0.5" />
            <span className="whitespace-pre-wrap">{order.itemsSummary}</span>
          </div>
        )}
        {order.paymentMethod && (
          <div className="flex items-center gap-2">
            <Wallet className="w-4 h-4 text-slate-400 flex-shrink-0" />
            <span>{order.paymentMethod}</span>
          </div>
        )}
      </div>

      <label className="mt-4 flex items-center gap-2 text-sm">
        <span className="text-slate-500">Статус:</span>
        <select value={order.status} disabled={saving} onChange={e => onStatusChange(order.id, e.target.value)}
                className="flex-1 border border-slate-200 rounded-xl px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-emerald-500">
          {ORDER_STATUSES.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
        </select>
        {saving && <Loader2 className="w-4 h-4 text-slate-400 animate-spin" />}
      </label>
    </div>
  );
};

// Orders the AI seller created for the shop; the merchant moves them through the statuses
const OrdersPage = () => {
  const navigate = useNavigate();
  const { shopId } = useParams();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ACTIVE');
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    api.get(`/api/v1/shops/${shopId}/orders`)
      .then(res => setOrders(res.data || []))
      .catch(() => setError('Не удалось загрузить заказы.'))
      .finally(() => setLoading(false));
  }, [shopId]);

  const changeStatus = async (orderId, status) => {
    setSavingId(orderId);
    try {
      const res = await api.patch(`/api/v1/shops/${shopId}/orders/${orderId}/status`, { status });
      setOrders(prev => prev.map(o => (o.id === orderId ? res.data : o)));
    } catch {
      alert('Не удалось изменить статус. Попробуйте ещё раз.');
    } finally {
      setSavingId(null);
    }
  };

  const visible = filter === 'ACTIVE' ? orders.filter(o => ACTIVE.includes(o.status)) : orders;
  const activeCount = orders.filter(o => ACTIVE.includes(o.status)).length;

  return (
    <div className="min-h-screen bg-[#F3F4F6] font-sans">
      <div className="max-w-5xl mx-auto px-4 py-8 text-left">
        <button type="button" onClick={() => navigate(`/shops/${shopId}`)}
                className="flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-slate-900 mb-4">
          <ArrowLeft className="w-4 h-4" /> Панель магазина
        </button>
        <p className="text-2xl font-bold text-slate-900 mb-4">Заказы</p>

        <div className="inline-flex gap-1 bg-slate-200/60 p-1 rounded-xl mb-6">
          {[['ACTIVE', `Активные (${activeCount})`], ['ALL', `Все (${orders.length})`]].map(([value, label]) => (
            <button key={value} type="button" onClick={() => setFilter(value)}
                    className={`px-4 py-1.5 rounded-lg text-sm font-semibold ${filter === value ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500'}`}>
              {label}
            </button>
          ))}
        </div>

        {loading && <Loader2 className="w-8 h-8 text-slate-400 animate-spin" />}
        {error && <p className="text-red-600">{error}</p>}
        {!loading && !error && visible.length === 0 && (
          <div className="bg-white rounded-2xl border border-dashed border-slate-300 p-10 text-center text-slate-500">
            {filter === 'ACTIVE' ? 'Активных заказов нет.' : 'Заказов пока нет. Их оформляет AI-продавец, когда клиент готов купить.'}
          </div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {visible.map(order => (
            <OrderCard key={order.id} order={order} onStatusChange={changeStatus} saving={savingId === order.id} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default OrdersPage;
