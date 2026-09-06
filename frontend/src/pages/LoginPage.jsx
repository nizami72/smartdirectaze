import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/api.ts';
import { Mail, Lock, Loader2, AlertCircle, ArrowRight, Store, X } from 'lucide-react';

const LoginPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState({});
  const [shops, setShops] = useState([]);
  const [showShopSelection, setShowShopSelection] = useState(false);

  const validate = () => {
    const errors = {};
    if (!formData.email) errors.email = 'Email обязателен';
    else if (!/\S+@\S+\.\S+/.test(formData.email)) errors.email = 'Некорректный формат email';

    if (!formData.password) errors.password = 'Пароль обязателен';

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (validationErrors[name]) {
      setValidationErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleShopSelect = (shop) => {
    sessionStorage.setItem('currentShopId', shop.id);
    sessionStorage.setItem('currentShopName', shop.shopName);
    navigate('/dashboard');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;

    setLoading(true);
    try {
      const response = await api.post('/api/v1/auth/login', formData);
      // After successful authentication, always redirect to MyBusinessesPage
      navigate('/my-businesses');
      return;
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Неверный email или пароль');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-md w-full bg-white p-10 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 relative overflow-hidden">
        
        <div className="absolute top-0 left-0 w-full h-1 bg-slate-100">
          <div className="w-full h-full bg-slate-900 transition-all duration-500"></div>
        </div>

        <div className="mb-8">
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            Вход в кабинет
          </h2>
          <p className="mt-2 text-sm text-slate-500 leading-relaxed">
            Авторизуйтесь, чтобы продолжить настройку вашего ИИ-ассистента.
          </p>
        </div>

        <form className="space-y-5" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-50/70 border border-red-100 rounded-xl p-4 flex items-start animate-in fade-in duration-200">
              <AlertCircle className="h-5 w-5 text-red-500 mr-2.5 mt-0.5 flex-shrink-0" />
              <span className="text-sm text-red-800 font-medium">{error}</span>
            </div>
          )}

          <div className="space-y-4">
            {/* Email */}
            <div>
              <label htmlFor="email" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                Электронная почта
              </label>
              <div className="relative rounded-xl shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <Mail className="h-4 w-4 text-slate-400" />
                </div>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className={`block w-full pl-10 pr-4 py-2.5 bg-slate-50/50 border ${
                    validationErrors.email ? 'border-red-300 focus:ring-red-50' : 'border-slate-200 focus:border-slate-400 focus:ring-slate-100'
                  } rounded-xl text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-4 transition-all sm:text-sm`}
                  placeholder="name@company.com"
                  value={formData.email}
                  onChange={handleChange}
                />
              </div>
              {validationErrors.email && (
                <p className="mt-1 text-xs text-red-600 font-medium">{validationErrors.email}</p>
              )}
            </div>

            {/* Пароль */}
            <div>
              <label htmlFor="password" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                Пароль
              </label>
              <div className="relative rounded-xl shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                  <Lock className="h-4 w-4 text-slate-400" />
                </div>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  className={`block w-full pl-10 pr-4 py-2.5 bg-slate-50/50 border ${
                    validationErrors.password ? 'border-red-300 focus:ring-red-50' : 'border-slate-200 focus:border-slate-400 focus:ring-slate-100'
                  } rounded-xl text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-4 transition-all sm:text-sm`}
                  placeholder="••••••••"
                  value={formData.password}
                  onChange={handleChange}
                />
              </div>
              {validationErrors.password && (
                <p className="mt-1 text-xs text-red-600 font-medium">{validationErrors.password}</p>
              )}
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="w-full flex justify-center items-center py-3 px-4 border border-transparent text-sm font-semibold rounded-xl text-white bg-slate-900 hover:bg-slate-800 focus:outline-none focus:ring-4 focus:ring-slate-100 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm active:scale-[0.98]"
            >
              {loading ? (
                <Loader2 className="animate-spin h-5 w-5" />
              ) : (
                <span className="flex items-center gap-2">
                  Войти <ArrowRight className="h-4 w-4" />
                </span>
              )}
            </button>
          </div>
        </form>

        <div className="mt-6 text-center">
          <p className="text-sm text-slate-500">
            Нет аккаунта?{' '}
            <Link to="/register" className="font-semibold text-slate-900 hover:underline underline-offset-4">
              Зарегистрироваться
            </Link>
          </p>
        </div>

      </div>

      {/* Shop Selection Modal */}
      {showShopSelection && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4 animate-in fade-in duration-200">
          <div className="bg-white w-full max-w-sm rounded-2xl shadow-2xl border border-slate-100 overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-slate-50 flex items-center justify-between">
              <h3 className="font-bold text-slate-900">Выберите магазин</h3>
              <button onClick={() => setShowShopSelection(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="p-2 max-h-[60vh] overflow-y-auto">
              {shops.map((shop) => (
                <button
                  key={shop.id}
                  onClick={() => handleShopSelect(shop)}
                  className="w-full flex items-center gap-4 p-4 hover:bg-slate-50 rounded-xl transition-all group text-left"
                >
                  <div className="w-10 h-10 bg-slate-100 rounded-lg flex items-center justify-center group-hover:bg-white transition-colors">
                    <Store className="h-5 w-5 text-slate-600" />
                  </div>
                  <div>
                    <div className="font-semibold text-slate-900">{shop.shopName}</div>
                    <div className="text-xs text-slate-400">ID: {shop.id}</div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LoginPage;
