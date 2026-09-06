import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/api.ts';
import { formatAzerbaijanPhone } from '../utils/utils';
import { Mail, Lock, User, Phone, Loader2, AlertCircle, ArrowRight } from 'lucide-react';

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
    phone: '+994',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState({});

  const validate = () => {
    const errors = {};
    if (!formData.email) errors.email = 'Email обязателен';
    else if (!/\S+@\S+\.\S+/.test(formData.email)) errors.email = 'Некорректный формат email';

    if (!formData.password) errors.password = 'Пароль обязателен';
    else if (formData.password.length < 6) errors.password = 'Минимум 6 символов';

    if (!formData.name) errors.name = 'Имя обязательно';

    if (!formData.phone || formData.phone.length < 13) errors.phone = 'Введите полный номер телефона';

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'phone') {
      setFormData(prev => ({ ...prev, [name]: formatAzerbaijanPhone(value) }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
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
      const response = await api.post('/api/v1/auth/register', formData);
      // After successful registration, always redirect to MyBusinessesPage
      navigate('/my-businesses');
      return;
    } catch (err) {
      console.error('Registration error:', err);
      setError(err.response?.data?.message || 'Произошла ошибка при регистрации. Попробуйте еще раз.');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans">
        <div className="max-w-md w-full bg-white p-10 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 relative overflow-hidden">

          {/* Декоративная тонкая полоска прогресса сверху */}
          <div className="absolute top-0 left-0 w-full h-1 bg-slate-100">
            <div className="w-1/3 h-full bg-slate-900 transition-all duration-500"></div>
          </div>

          {/* Заголовок и Индикатор шага */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 bg-slate-50 px-2.5 py-1 rounded-md">
              Шаг 1 из 5
            </span>
              <span className="text-xs text-slate-400">Создание аккаунта</span>
            </div>
            <h2 className="text-2xl font-bold tracking-tight text-slate-900">
              Регистрация партнера
            </h2>
            <p className="mt-2 text-sm text-slate-500 leading-relaxed">
              Зарегистрируйте личный кабинет ИИ-ассистента для автоматизации продаж вашего бизнеса.
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
              {/* Имя */}
              <div>
                <label htmlFor="name" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Ваше имя
                </label>
                <div className="relative rounded-xl shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <User className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                      id="name"
                      name="name"
                      type="text"
                      required
                      className={`block w-full pl-10 pr-4 py-2.5 bg-slate-50/50 border ${
                          validationErrors.name ? 'border-red-300 focus:ring-red-50' : 'border-slate-200 focus:border-slate-400 focus:ring-slate-100'
                      } rounded-xl text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-4 transition-all sm:text-sm`}
                      placeholder="Например, Низами"
                      value={formData.name}
                      onChange={handleChange}
                  />
                </div>
                {validationErrors.name && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{validationErrors.name}</p>
                )}
              </div>

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

              {/* Телефон */}
              <div>
                <label htmlFor="phone" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Контактный телефон
                </label>
                <div className="relative rounded-xl shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Phone className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                      id="phone"
                      name="phone"
                      type="tel"
                      required
                      className={`block w-full pl-10 pr-4 py-2.5 bg-slate-50/50 border ${
                          validationErrors.phone ? 'border-red-300 focus:ring-red-50' : 'border-slate-200 focus:border-slate-400 focus:ring-slate-100'
                      } rounded-xl text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-4 transition-all sm:text-sm`}
                      placeholder="+994 50 123 45 67"
                      value={formData.phone}
                      onChange={handleChange}
                  />
                </div>
                {validationErrors.phone && (
                    <p className="mt-1 text-xs text-red-600 font-medium">{validationErrors.phone}</p>
                )}
              </div>

              {/* Пароль */}
              <div>
                <label htmlFor="password" className="block text-xs font-semibold uppercase tracking-wide text-slate-600 mb-1.5">
                  Пароль для входа
                </label>
                <div className="relative rounded-xl shadow-sm">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                    <Lock className="h-4 w-4 text-slate-400" />
                  </div>
                  <input
                      id="password"
                      name="password"
                      type="password"
                      autoComplete="new-password"
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
                  Продолжить <ArrowRight className="h-4 w-4" />
                </span>
                )}
              </button>
            </div>
          </form>

          {/* Ссылка на Авторизацию */}
          <div className="mt-6 text-center">
            <p className="text-sm text-slate-500">
              Уже есть аккаунт?{' '}
              <Link to="/login" className="font-semibold text-slate-900 hover:underline underline-offset-4">
                Войти
              </Link>
            </p>
          </div>

        </div>
      </div>
  );
};

export default RegisterPage;
