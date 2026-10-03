import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';
import { Banknote, CheckCircle2, Loader2, AlertCircle, ArrowRight } from 'lucide-react';
import OnboardingSteps from '../features/shop/components/OnboardingSteps.jsx';
import { useTranslation } from 'react-i18next';

const inputClass = 'w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 placeholder:text-slate-400 text-sm';

// Step 1: only what the AI needs to start; address, hours, fitting etc. are set later in the dashboard
const CreateShopPage = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [formData, setFormData] = useState({ shopName: '', deliveryPrice: '', freeDeliveryThreshold: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!formData.shopName.trim()) {
      setError(t('createShop.nameRequired'));
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/api/v1/shops', {
        shopName: formData.shopName.trim(),
        deliveryPrice: parseFloat(formData.deliveryPrice) || 0,
        freeDeliveryThreshold: parseFloat(formData.freeDeliveryThreshold) || 0,
      });
      navigate(`/shops/${response.data.id}/catalog`);
    } catch (err) {
      console.error('Shop creation error:', err);
      setError(err.response?.data?.message || t('createShop.failed'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4 py-12 font-sans">
      <div className="max-w-md w-full bg-white p-8 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 text-left">
        <OnboardingSteps current={1} />
        <p className="text-2xl font-bold tracking-tight text-slate-900">{t('createShop.title')}</p>
        <p className="mt-2 mb-6 text-sm text-slate-500">
          {t('createShop.subtitle')}
        </p>

        <form className="space-y-5 mt-6" onSubmit={handleSubmit}>
          {error && (
            <div data-testid="create-shop-error" className="bg-red-50/70 border border-red-100 rounded-xl p-4 flex items-start">
              <AlertCircle className="h-5 w-5 text-red-500 mr-2.5 mt-0.5 flex-shrink-0" />
              <span className="text-sm text-red-800 font-medium">{error}</span>
            </div>
          )}

          <div>
            <label htmlFor="shopName" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
              {t('createShop.name')}
            </label>
            <input id="shopName" name="shopName" type="text" autoFocus className={inputClass}
                   placeholder={t('createShop.namePlaceholder')} value={formData.shopName} onChange={handleChange} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="deliveryPrice" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                {t('createShop.delivery')}
              </label>
              <div className="relative">
                <Banknote className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
                <input id="deliveryPrice" name="deliveryPrice" type="number" min="0" step="0.5" placeholder="0"
                       className={`${inputClass} pl-10`} value={formData.deliveryPrice} onChange={handleChange} />
              </div>
            </div>
            <div>
              <label htmlFor="freeDeliveryThreshold" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                {t('createShop.freeFrom')}
              </label>
              <div className="relative">
                <CheckCircle2 className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
                <input id="freeDeliveryThreshold" name="freeDeliveryThreshold" type="number" min="0" placeholder="0"
                       className={`${inputClass} pl-10`} value={formData.freeDeliveryThreshold} onChange={handleChange} />
              </div>
            </div>
          </div>

          <button type="submit" disabled={loading} data-testid="create-shop-submit"
                  className="w-full flex items-center justify-center py-3.5 px-4 rounded-xl text-sm font-bold text-white bg-slate-900 hover:bg-slate-800 disabled:opacity-50 transition-all">
            {loading ? <Loader2 className="animate-spin h-5 w-5" /> : <>{t('common.continue')} <ArrowRight className="ml-2 h-4 w-4" /></>}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateShopPage;
