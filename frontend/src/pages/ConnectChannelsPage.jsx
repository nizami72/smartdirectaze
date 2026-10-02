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

// Operator's WhatsApp for merchants waiting for activation (digits); no button when not set
const SUPPORT_WHATSAPP = (import.meta.env.VITE_SUPPORT_WHATSAPP || '').replace(/\D/g, '');

const ConnectChannelsPage = () => {
  const navigate = useNavigate();
  const { shopId } = useParams();
  const [status, setStatus] = useState('LOADING'); // LOADING, PENDING_ACTIVATION, WAITING_QR, CONNECTED, ERROR
  const [qrCode, setQrCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [completing, setCompleting] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [connectedPhone, setConnectedPhone] = useState('');
  const [disconnecting, setDisconnecting] = useState(false);
  // The server gives no QR until the merchant has confirmed the WhatsApp ban risk
  const [riskAccepted, setRiskAccepted] = useState(true);
  const [riskChecked, setRiskChecked] = useState(false);
  const [accepting, setAccepting] = useState(false);

  useEffect(() => {
    api.get('/api/v1/admin/me').then(res => setIsAdmin(res.data.admin === true)).catch(() => setIsAdmin(false));
  }, []);


  const checkStatus = useCallback(async () => {
    if (!shopId) return;

    try {
      const response = await api.get(`/api/v1/shops/channels/whatsapp/qr?shopId=${shopId}`);
      const { status: backendStatus, qrCode: backendQr, connectedPhone: backendPhone, riskAccepted: backendRisk } = response.data;
      setRiskAccepted(backendRisk !== false);
      
      console.log('Backend status:', backendStatus); // Добавим лог для отладки
      setStatus(backendStatus);
      // Green API sends bare base64 PNG, the local mock a full data URL
      if (backendQr) setQrCode(backendQr.startsWith('data:') ? backendQr : `data:image/png;base64,${backendQr}`);
      
      if (backendStatus === 'CONNECTED') {
          setError('');
          setConnectedPhone(backendPhone || '');
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

  const acceptRisk = async () => {
    setAccepting(true);
    setError('');
    try {
      await api.post(`/api/v1/shops/channels/whatsapp/accept-risk?shopId=${shopId}`);
      setRiskAccepted(true);
      await checkStatus();
    } catch (err) {
      console.error('Accept risk error:', err);
      setError('Не удалось сохранить. Попробуйте ещё раз.');
    } finally {
      setAccepting(false);
    }
  };

  // Scanned with the wrong phone: log it out and wait for a fresh QR
  const handleConnectAnother = async () => {
    setDisconnecting(true);
    setError('');
    try {
      await api.post(`/api/v1/shops/channels/whatsapp/disconnect?shopId=${shopId}`);
      setConnectedPhone('');
      setQrCode('');
      // Green API needs a few seconds after logout before it hands out a new QR
      await new Promise(resolve => setTimeout(resolve, 3000));
      setStatus('WAITING_QR');
    } catch (err) {
      console.error('Disconnect error:', err);
      setError('Не удалось отключить номер. Попробуйте ещё раз.');
    } finally {
      setDisconnecting(false);
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
              <p className="text-lg font-bold text-slate-900 mb-2">Подключаем WhatsApp</p>
              <p className="text-slate-600 text-sm leading-relaxed mb-4">
                Мы готовим подключение WhatsApp для вашего магазина — обычно в течение рабочего дня.
                Когда всё будет готово, на этой странице появится QR-код.
              </p>
              <p className="text-slate-500 text-sm">
                А пока можно добавить товары и проверить продавца в панели магазина.
              </p>
              {SUPPORT_WHATSAPP && (
                <a href={`https://wa.me/${SUPPORT_WHATSAPP}`} target="_blank" rel="noopener noreferrer"
                   className="mt-6 inline-flex items-center gap-2 bg-[#25D366] hover:bg-[#20ba56] text-white px-6 py-3 rounded-xl font-bold transition-all">
                  <MessageSquare className="w-5 h-5" />
                  Написать в поддержку
                </a>
              )}
              {isAdmin && (
                <button type="button" onClick={() => navigate('/admin')}
                        className="mt-4 block mx-auto text-sm font-semibold text-slate-700 underline">
                  Привязать инстанс (админ)
                </button>
              )}
            </div>
          </div>
        );

      case 'WAITING_QR':
        if (!riskAccepted) {
          return (
            <div className="space-y-5 animate-in fade-in duration-500 text-left">
              <div className="bg-amber-50 border border-amber-200 rounded-2xl p-5">
                <p className="flex items-center gap-2 font-bold text-slate-900 mb-3">
                  <AlertCircle className="w-5 h-5 text-amber-600" />
                  Перед подключением
                </p>
                <div className="space-y-2 text-sm text-slate-700 leading-relaxed">
                  <p>
                    SmartDirect подключается к WhatsApp как связанное устройство — так же, как WhatsApp Web на компьютере.
                    Это не официальный сервис WhatsApp, поэтому есть небольшой риск, что WhatsApp ограничит
                    или заблокирует номер.
                  </p>
                  <p className="font-semibold">Как снизить риск:</p>
                  <ul className="list-disc pl-5 space-y-1">
                    <li>подключайте номер, который давно работает в WhatsApp, а не только что созданный;</li>
                    <li>держите телефон магазина включённым и в сети;</li>
                    <li>не отправляйте с этого номера рассылки людям, которые вам не писали.</li>
                  </ul>
                </div>
              </div>
              <label className="flex items-start gap-3 text-sm text-slate-800 cursor-pointer">
                <input type="checkbox" checked={riskChecked} onChange={e => setRiskChecked(e.target.checked)}
                       className="mt-0.5 w-4 h-4 accent-slate-900" />
                Я понимаю риск и подключаю номер под свою ответственность
              </label>
              <button type="button" onClick={acceptRisk} disabled={!riskChecked || accepting}
                      className="w-full flex items-center justify-center gap-2 bg-slate-900 hover:bg-slate-800 disabled:opacity-40 text-white font-bold py-3 rounded-2xl transition-all">
                {accepting ? <Loader2 className="w-5 h-5 animate-spin" /> : <QrCode className="w-5 h-5" />}
                Показать QR-код
              </button>
              {error && <p className="text-sm text-red-600">{error}</p>}
            </div>
          );
        }
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
              {connectedPhone && (
                <div className="bg-white rounded-2xl border border-emerald-100 px-4 py-4 my-5">
                  <p className="text-3xl font-bold text-slate-900 tracking-wide whitespace-nowrap">+{connectedPhone}</p>
                  <p className="text-slate-600 text-sm mt-2">Это номер, на который пишут покупатели?</p>
                  <button type="button" onClick={handleConnectAnother} disabled={disconnecting}
                          className="mt-3 inline-flex items-center gap-2 text-sm font-semibold text-red-600 hover:underline disabled:opacity-50">
                    {disconnecting && <Loader2 className="w-4 h-4 animate-spin" />}
                    Нет, подключить другой
                  </button>
                  {error && <p className="text-sm text-red-600 mt-2">{error}</p>}
                </div>
              )}
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
