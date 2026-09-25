import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/api.ts';
import { 
  MessageSquare, 
  Loader2, 
  CheckCircle2, 
  AlertCircle, 
  ExternalLink,
  ArrowRight,
  RefreshCw,
  QrCode,
  Clock
} from 'lucide-react';
import OnboardingSteps from '../features/shop/components/OnboardingSteps.jsx';

const ConnectChannelsPage = () => {
  const navigate = useNavigate();
  const { shopId } = useParams();
  const [status, setStatus] = useState('LOADING'); // LOADING, PENDING_ACTIVATION, WAITING_QR, CONNECTED, ERROR
  const [qrCode, setQrCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [completing, setCompleting] = useState(false);


  const checkStatus = useCallback(async () => {
    if (!shopId) return;

    try {
      const response = await api.get(`/api/v1/shops/channels/whatsapp/qr?shopId=${shopId}`);
      const { status: backendStatus, qrCode: backendQr } = response.data;
      
      console.log('Backend status:', backendStatus); // Добавим лог для отладки
      setStatus(backendStatus);
      if (backendQr) setQrCode(backendQr);
      
      if (backendStatus === 'CONNECTED') {
          setError('');
      }
    } catch (err) {
      console.error('Check status error:', err);
      // Не прерываем опрос при временных ошибках сети, но показываем ответ сервера с ошибкой
      if (err.response) {
        setStatus('ERROR');
        setError('Не удалось получить статус WhatsApp для этого магазина.');
      }
    } finally {
      setLoading(false);
    }
  }, [shopId]);

  useEffect(() => {
    if (!shopId) return;

    checkStatus();
    const interval = setInterval(() => {
      if (status === 'WAITING_QR' || status === 'PENDING_ACTIVATION' || status === 'LOADING') {
        checkStatus();
      }
    }, 4000);

    return () => clearInterval(interval);
  }, [shopId, status, checkStatus]);

  const handleComplete = async () => {
    setCompleting(true);
    try {
      await api.post(`/api/v1/shops/channels/onboarding/complete?shopId=${shopId}`);
      navigate(`/shops/${shopId}`);
    } catch (err) {
      console.error('Complete onboarding error:', err);
      setError('Не удалось завершить регистрацию. Попробуйте еще раз.');
    } finally {
      setCompleting(false);
    }
  };

  const renderContent = () => {
    // Если мы еще в процессе первичной загрузки (нет shopId) или API еще не вернул первый ответ
    if (loading && status === 'LOADING') {
      return (
        <div className="flex flex-col items-center justify-center py-12">
          <Loader2 className="w-12 h-12 text-slate-900 animate-spin mb-4" />
          <p className="text-slate-500 font-medium">Загрузка конфигурации...</p>
        </div>
      );
    }

    // Если произошла ошибка при получении shopId или первый запрос упал
    if (status === 'ERROR') {
      return (
        <div className="text-center py-8">
          <div className="w-16 h-16 bg-red-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <AlertCircle className="w-8 h-8 text-red-500" />
          </div>
          <p className="text-slate-800 font-bold mb-2">Упс! Что-то пошло не так</p>
          <p className="text-slate-500 text-sm mb-6 max-w-xs mx-auto">{error || 'Не удалось загрузить данные.'}</p>
          <button 
              onClick={() => window.location.reload()}
              className="text-slate-900 font-bold text-sm flex items-center gap-2 mx-auto hover:underline"
          >
              <RefreshCw className="w-4 h-4" />
              Попробовать снова
          </button>
        </div>
      );
    }

    switch (status) {
      case 'PENDING_ACTIVATION':
        return (
          <div className="space-y-6 animate-in fade-in zoom-in duration-500">
            <div className="bg-amber-50 border border-amber-100 rounded-2xl p-6 text-center">
              <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-sm">
                <Clock className="w-8 h-8 text-amber-500 animate-pulse" />
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Сервер готовится</h3>
              <p className="text-slate-600 text-sm leading-relaxed mb-6">
                Ваш выделенный ИИ-сервер готовится. Это обычно занимает от 5 до 15 минут. 
                Мы пришлем уведомление, когда все будет готово.
              </p>
              <a 
                href="https://wa.me/994508434303" 
                target="_blank" 
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 bg-[#25D366] hover:bg-[#20ba56] text-white px-6 py-3 rounded-xl font-bold transition-all shadow-lg shadow-green-100"
              >
                <MessageSquare className="w-5 h-5" />
                Ускорить активацию
              </a>
            </div>
          </div>
        );

      case 'WAITING_QR':
        return (
          <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div className="text-center">
              <p className="text-slate-500 text-sm mb-6">
                Откройте WhatsApp на телефоне → Настройки → Связанные устройства → Привязка устройства
              </p>
              
              <div className="relative mx-auto w-64 h-64 bg-white p-4 rounded-3xl shadow-[0_10px_40px_rgba(0,0,0,0.08)] border border-slate-100 flex items-center justify-center overflow-hidden">
                {qrCode ? (
                  <img src={qrCode} alt="WhatsApp QR Code" className="w-full h-full object-contain" />
                ) : (
                  <div className="flex flex-col items-center">
                    <Loader2 className="w-8 h-8 text-slate-200 animate-spin" />
                  </div>
                )}
                <div className="absolute inset-0 bg-slate-900/0 hover:bg-slate-900/5 transition-colors cursor-pointer flex items-center justify-center group">
                    <RefreshCw className="w-8 h-8 text-white opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
              </div>
              
              <div className="mt-8 flex items-center justify-center gap-2 text-xs font-bold text-slate-400 uppercase tracking-widest">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-slate-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-slate-400"></span>
                </span>
                Ожидание сканирования
              </div>
            </div>
          </div>
        );

      case 'CONNECTED':
        return (
          <div className="space-y-6 animate-in zoom-in duration-500">
            <div className="bg-emerald-50 border border-emerald-100 rounded-3xl p-8 text-center">
              <div className="w-20 h-20 bg-emerald-500 rounded-full flex items-center justify-center mx-auto mb-6 shadow-lg shadow-emerald-200">
                <CheckCircle2 className="w-10 h-10 text-white" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 mb-2">Успешно подключено!</h3>
              <p className="text-emerald-700 font-medium">
                AI сейчас в режиме «Тест» и отвечает только вашим тестовым номерам.
                Добавьте свой второй номер в панели магазина, проверьте ответы и включите AI для всех.
              </p>
            </div>

            <button
              onClick={handleComplete}
              disabled={completing}
              className="w-full flex items-center justify-center gap-3 bg-slate-900 hover:bg-slate-800 text-white font-bold py-4 rounded-2xl transition-all shadow-xl shadow-slate-200 group"
            >
              {completing ? (
                <Loader2 className="w-6 h-6 animate-spin" />
              ) : (
                <>
                  Войти в панель управления
                  <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </div>
        );

      case 'ERROR':
        return null; // Уже обработано выше

      default:
        // В случае неизвестного статуса или если что-то проскочило, показываем лоадер вместо пустого экрана
        return (
          <div className="flex flex-col items-center justify-center py-12">
            <Loader2 className="w-12 h-12 text-slate-900 animate-spin mb-4" />
            <p className="text-slate-500 font-medium">Обновление статуса...</p>
          </div>
        );
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans flex items-center justify-center">
      <div className="max-w-md w-full">
        
        <div className="text-left">
          <OnboardingSteps current={4} />
        </div>
        <div className="mb-8 text-center">
          <p className="text-3xl font-bold text-slate-900 tracking-tight">Подключите WhatsApp</p>
          <p className="mt-3 text-slate-500 text-sm">
            Последний шаг, чтобы AI начал отвечать вашим клиентам.
          </p>
        </div>

        {/* Основная карточка */}
        <div className="bg-white p-8 sm:p-10 rounded-[2.5rem] shadow-[0_20px_50px_rgba(0,0,0,0.04)] border border-slate-50 relative overflow-hidden">
          {renderContent()}
        </div>

        {status !== 'CONNECTED' && (
          <button type="button" onClick={() => navigate(`/shops/${shopId}`)}
                  className="mt-6 w-full text-sm font-semibold text-slate-500 hover:text-slate-800">
            Подключу позже — перейти в панель магазина
          </button>
        )}

        {/* Дополнительная информация */}
        <div className="mt-10 flex items-center justify-center gap-8 opacity-40 grayscale">
            <div className="flex items-center gap-2">
                <QrCode className="w-4 h-4" />
                <span className="text-[10px] font-bold uppercase tracking-widest">Secure Connect</span>
            </div>
            <div className="flex items-center gap-2">
                <MessageSquare className="w-4 h-4" />
                <span className="text-[10px] font-bold uppercase tracking-widest">WhatsApp Business</span>
            </div>
        </div>
      </div>
    </div>
  );
};

export default ConnectChannelsPage;
