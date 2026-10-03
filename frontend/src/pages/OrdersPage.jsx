import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/api.ts';
import { ArrowLeft, Check, ChevronDown, Loader2, MapPin, MessageCircle, Package, Phone, Wallet } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { formatShortDateTime } from '../i18n';

const ORDER_STATUSES = [
  { value: 'NEW', label: 'orders.statusNew', className: 'bg-amber-100 text-amber-800' },
  { value: 'CONFIRMED', label: 'orders.statusConfirmed', className: 'bg-blue-100 text-blue-800' },
  { value: 'IN_DELIVERY', label: 'orders.statusInDelivery', className: 'bg-violet-100 text-violet-800' },
  { value: 'COMPLETED', label: 'orders.statusCompleted', className: 'bg-emerald-100 text-emerald-800' },
  { value: 'CANCELLED', label: 'orders.statusCancelled', className: 'bg-slate-100 text-slate-600' },
];

const ACTIVE = ['NEW', 'CONFIRMED', 'IN_DELIVERY'];
// Final statuses: asked to confirm before they are set, the others change at once
const FINAL = ['COMPLETED', 'CANCELLED'];

const formatDate = formatShortDateTime;

// wa.me needs digits only
const whatsappLink = (phone) => `https://wa.me/${(phone || '').replace(/\D/g, '')}`;

// Status picker. Not a native <select>: in Chrome on Linux it opens over the field and selects whatever
// is under the cursor when the mouse button is released, so one click could silently change the status.
// Here a click only opens the list, a second click picks; a click outside or Esc closes it unchanged.
const StatusMenu = ({ status, disabled, onChange }) => {
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const current = ORDER_STATUSES.find(s => s.value === status) || ORDER_STATUSES[0];

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => {
      if (e.type === 'keydown' ? e.key === 'Escape' : !ref.current?.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', close);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', close);
    };
  }, [open]);

  return (
    <div ref={ref} className="relative flex-1">
      <button type="button" disabled={disabled} onClick={() => setOpen(o => !o)} data-testid="order-status"
              aria-haspopup="listbox" aria-expanded={open}
              className="w-full flex items-center justify-between gap-2 border border-slate-200 rounded-xl px-3 py-2 bg-white hover:bg-slate-50 disabled:opacity-50 focus:outline-none focus:ring-2 focus:ring-emerald-500">
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${current.className}`}>{t(current.label)}</span>
        <ChevronDown className={`w-4 h-4 text-slate-400 transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>
      {open && (
        <ul role="listbox" className="absolute z-20 mt-1 w-full bg-white border border-slate-200 rounded-xl shadow-lg py-1">
          {ORDER_STATUSES.map(s => (
            <li key={s.value} role="option" aria-selected={s.value === status}>
              <button type="button" data-testid={`order-status-${s.value}`}
                      onClick={() => { setOpen(false); if (s.value !== status) onChange(s.value); }}
                      className={`w-full flex items-center justify-between px-3 py-2 text-left hover:bg-slate-50 ${s.value === status ? 'font-semibold' : ''}`}>
                <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${s.className}`}>{t(s.label)}</span>
                {s.value === status && <Check className="w-4 h-4 text-emerald-600" />}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

const OrderCard = ({ order, onStatusChange, saving }) => {
  const { t } = useTranslation();
  // A final status waiting for "Yes" (null = nothing to confirm)
  const [pending, setPending] = useState(null);
  const choose = (status) => (FINAL.includes(status) ? setPending(status) : onStatusChange(order.id, status));
  const pendingLabel = pending && t(ORDER_STATUSES.find(s => s.value === pending).label);
  const status = ORDER_STATUSES.find(s => s.value === order.status) || ORDER_STATUSES[0];
  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-5 text-left">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <p className="font-bold text-slate-900">{order.status === 'NEW' ? t('orders.request') : t('orders.order')} #{order.id}</p>
          <p className="text-xs text-slate-400">{formatDate(order.createdAt)}</p>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${status.className}`}>{t(status.label)}</span>
      </div>

      {order.status === 'NEW' && <p className="text-sm text-amber-800 mb-3">{t('orders.confirmHint')}</p>}
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

      <div className="mt-4 flex items-center gap-2 text-sm">
        <span className="text-slate-500">{t('orders.status')}</span>
        <StatusMenu status={order.status} disabled={saving || pending !== null} onChange={choose} />
        {saving && <Loader2 className="w-4 h-4 text-slate-400 animate-spin" />}
      </div>
      {pending && (
        <div className="mt-3 bg-amber-50 border border-amber-200 rounded-xl p-3 text-sm" data-testid="order-status-confirm">
          <p className="text-amber-900">{t('orders.confirmFinal', { id: order.id, status: pendingLabel })}</p>
          <div className="flex flex-wrap gap-2 mt-2">
            <button type="button" data-testid="order-status-confirm-yes"
                    onClick={() => { onStatusChange(order.id, pending); setPending(null); }}
                    className="bg-slate-900 hover:bg-slate-800 text-white rounded-lg px-3 py-1.5 font-semibold">
              {t('orders.confirmYes')}
            </button>
            <button type="button" onClick={() => setPending(null)}
                    className="bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 rounded-lg px-3 py-1.5 font-semibold">
              {t('common.cancel')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

// Orders the AI seller created for the shop; the merchant moves them through the statuses
const OrdersPage = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { shopId } = useParams();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ACTIVE');
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    api.get(`/api/v1/shops/${shopId}/orders`)
      .then(res => setOrders(res.data || []))
      .catch(() => setError(t('orders.loadFailed')))
      .finally(() => setLoading(false));
  }, [shopId, t]);

  const changeStatus = async (orderId, status) => {
    setSavingId(orderId);
    try {
      const res = await api.patch(`/api/v1/shops/${shopId}/orders/${orderId}/status`, { status });
      setOrders(prev => prev.map(o => (o.id === orderId ? res.data : o)));
    } catch {
      alert(t('orders.statusFailed'));
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
          <ArrowLeft className="w-4 h-4" /> {t('orders.back')}
        </button>
        <p className="text-2xl font-bold text-slate-900 mb-4">{t('orders.title')}</p>

        <div className="inline-flex gap-1 bg-slate-200/60 p-1 rounded-xl mb-6">
          {[['ACTIVE', t('orders.active', { count: activeCount })], ['ALL', t('orders.all', { count: orders.length })]].map(([value, label]) => (
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
            {filter === 'ACTIVE' ? t('orders.noActive') : t('orders.none')}
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
