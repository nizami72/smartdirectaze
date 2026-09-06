import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';
import { 
  Store, 
  Clock, 
  MapPin, 
  Banknote, 
  Truck, 
  CheckCircle2, 
  Loader2, 
  AlertCircle, 
  ArrowRight,
  Scissors
} from 'lucide-react';

const CreateShopPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    shopName: '',
    workingHours: '10:00 - 22:00',
    address: '',
    deliveryPrice: 0,
    freeDeliveryThreshold: 0,
    fittingAllowed: false,
    refusalFee: 0,
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState({});

  const validate = () => {
    const errors = {};
    if (!formData.shopName.trim()) errors.shopName = 'Название магазина обязательно';
    if (!formData.workingHours.trim()) errors.workingHours = 'Рабочие часы обязательны';
    if (!formData.address.trim()) errors.address = 'Адрес (ориентир) обязателен';
    
    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    const val = type === 'checkbox' ? checked : (type === 'number' ? parseFloat(value) || 0 : value);
    
    setFormData(prev => ({ ...prev, [name]: val }));
    
    if (validationErrors[name]) {
      setValidationErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;

    setLoading(true);
    try {
      const response = await api.post('/api/v1/shops', formData);
      if (response.data.id) {
          sessionStorage.setItem('currentShopId', response.data.id);
          sessionStorage.setItem('currentShopName', response.data.shopName);
      }
      if (response.data.registrationStep === 'SHOP_CREATED') {
        navigate('/fill-catalog');
      } else {
          // На случай если бэкенд вернет что-то другое, но запрос успешен
          navigate('/fill-catalog');
      }
    } catch (err) {
      console.error('Shop creation error:', err);
      setError(err.response?.data?.message || 'Произошла ошибка при создании магазина. Попробуйте еще раз.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-xl w-full bg-white p-8 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 relative overflow-hidden">
        
        {/* Декоративная тонкая полоска прогресса сверху */}
        <div className="absolute top-0 left-0 w-full h-1 bg-slate-100">
          <div className="w-2/3 h-full bg-slate-900 transition-all duration-500"></div>
        </div>

        {/* Заголовок и Индикатор шага */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 bg-slate-50 px-2.5 py-1 rounded-md">
              Шаг 2 из 4
            </span>
            <span className="text-xs text-slate-400 font-medium">Настройка бизнеса</span>
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Создание магазина
          </h2>
          <p className="mt-2 text-sm text-slate-500 leading-relaxed">
            Расскажите нам о вашем бизнесе, чтобы ИИ мог корректно отвечать клиентам.
          </p>
        </div>

        <form className="space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-50/70 border border-red-100 rounded-xl p-4 flex items-start animate-in fade-in duration-200">
              <AlertCircle className="h-5 w-5 text-red-500 mr-2.5 mt-0.5 flex-shrink-0" />
              <span className="text-sm text-red-800 font-medium">{error}</span>
            </div>
          )}

          {/* Основная информация */}
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Store className="h-4 w-4 text-slate-400" />
              Основная информация
            </h3>
            
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label htmlFor="shopName" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Название бизнеса
                </label>
                <input
                  id="shopName"
                  name="shopName"
                  type="text"
                  required
                  className={`w-full px-4 py-2.5 bg-slate-50 border ${validationErrors.shopName ? 'border-red-300' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 placeholder:text-slate-400 text-sm`}
                  placeholder="Напр: My Boutique"
                  value={formData.shopName}
                  onChange={handleChange}
                />
                {validationErrors.shopName && <p className="mt-1 text-xs text-red-500 font-medium">{validationErrors.shopName}</p>}
              </div>

              <div>
                <label htmlFor="workingHours" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Часы работы
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Clock className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                    id="workingHours"
                    name="workingHours"
                    type="text"
                    required
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 placeholder:text-slate-400 text-sm"
                    placeholder="10:00 - 22:00"
                    value={formData.workingHours}
                    onChange={handleChange}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Логистика */}
          <div className="space-y-4 pt-2">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Truck className="h-4 w-4 text-slate-400" />
              Логистика и Доставка (Баку)
            </h3>
            
            <div>
              <label htmlFor="address" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                Адрес / Ориентир
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <MapPin className="h-4 w-4 text-slate-400" />
                </div>
                <input
                  id="address"
                  name="address"
                  type="text"
                  required
                  className={`w-full pl-10 pr-4 py-2.5 bg-slate-50 border ${validationErrors.address ? 'border-red-300' : 'border-slate-200'} rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 placeholder:text-slate-400 text-sm`}
                  placeholder="Напр: метро Эльмляр, рядом с BDU"
                  value={formData.address}
                  onChange={handleChange}
                />
              </div>
              {validationErrors.address && <p className="mt-1 text-xs text-red-500 font-medium">{validationErrors.address}</p>}
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div>
                <label htmlFor="deliveryPrice" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Цена доставки (AZN)
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Banknote className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                    id="deliveryPrice"
                    name="deliveryPrice"
                    type="number"
                    min="0"
                    step="0.5"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 text-sm"
                    value={formData.deliveryPrice}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div>
                <label htmlFor="freeDeliveryThreshold" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Бесплатно от (AZN)
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <CheckCircle2 className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                    id="freeDeliveryThreshold"
                    name="freeDeliveryThreshold"
                    type="number"
                    min="0"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 text-sm"
                    value={formData.freeDeliveryThreshold}
                    onChange={handleChange}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Параметры Шоурума */}
          <div className="space-y-4 pt-2">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Scissors className="h-4 w-4 text-slate-400" />
              Дополнительно
            </h3>
            
            <div className="flex items-center space-x-3 p-4 bg-slate-50 rounded-xl border border-slate-100">
              <input
                id="fittingAllowed"
                name="fittingAllowed"
                type="checkbox"
                className="h-4 w-4 text-slate-900 focus:ring-slate-900 border-slate-300 rounded"
                checked={formData.fittingAllowed}
                onChange={handleChange}
              />
              <label htmlFor="fittingAllowed" className="text-sm font-medium text-slate-700">
                Разрешена примерка
              </label>
            </div>

            {formData.fittingAllowed && (
              <div className="animate-in slide-in-from-top-2 duration-200">
                <label htmlFor="refusalFee" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Штраф при отказе (AZN)
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <AlertCircle className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                    id="refusalFee"
                    name="refusalFee"
                    type="number"
                    min="0"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all duration-200 text-sm"
                    value={formData.refusalFee}
                    onChange={handleChange}
                  />
                </div>
                <p className="mt-1.5 text-[11px] text-slate-400">Сумма, которую клиент платит курьеру, если товар не подошел после примерки.</p>
              </div>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center py-3.5 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-slate-900 hover:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-slate-900 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 mt-8"
          >
            {loading ? (
              <Loader2 className="animate-spin h-5 w-5" />
            ) : (
              <>
                Продолжить
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateShopPage;
