import { useNavigate, useParams } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
import OnboardingSteps from '../features/shop/components/OnboardingSteps.jsx';
import TestChatCard from '../features/shop/components/TestChatCard.jsx';

// Step 3: the merchant checks the AI seller before connecting WhatsApp
const TestSellerPage = () => {
  const navigate = useNavigate();
  const { shopId } = useParams();

  return (
    <div className="min-h-screen bg-[#f8fafc] px-4 py-12 font-sans">
      <div className="max-w-2xl mx-auto text-left">
        <OnboardingSteps current={3} />
        <TestChatCard shopId={shopId} />
        <div className="flex flex-col sm:flex-row gap-3">
          <button type="button" onClick={() => navigate(`/shops/${shopId}/catalog`)}
                  className="flex-1 py-3.5 rounded-xl border-2 border-slate-200 text-slate-700 font-bold hover:bg-white transition-all">
            Добавить ещё товары
          </button>
          <button type="button" onClick={() => navigate(`/shops/${shopId}/connect`)}
                  className="flex-1 flex items-center justify-center gap-2 py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold transition-all">
            Подключить WhatsApp
            <ArrowRight className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
};

export default TestSellerPage;
