import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/api.ts';
import AiSettingsCard from '../features/shop/components/AiSettingsCard.jsx';
import TestChatCard from '../features/shop/components/TestChatCard.jsx';
import { 
  Loader2,
  AlertCircle, 
  LogOut, 
  Store,
  ArrowLeft,
  MessageCircle
} from 'lucide-react';

const DashboardPage = () => {
  const navigate = useNavigate();
  const { shopId } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [shopInfo, setShopInfo] = useState({ id: null, name: '' });
  const contentRef = useRef(null);

  useEffect(() => {
    const initDashboard = async () => {
      try {
        // 1. The shop comes from the URL; it must be one of the user's shops
        const response = await api.get('/api/v1/shops/my');
        const shop = (response.data || []).find(s => String(s.id) === shopId);
        if (!shop) {
          setError('Магазин не найден.');
          setLoading(false);
          return;
        }
        const currentShopId = shop.id;
        setShopInfo({ id: shop.id, name: shop.shopName });

        // 2. Fetch inventory fragment
        const fragmentResponse = await api.get(`/api/v1/dashboard/inventory?shopId=${currentShopId}`, {
          headers: { 'Accept': 'text/html' },
          responseType: 'text'
        });

        if (contentRef.current) {
          // The fragment's forms and photos use backend paths (/webhooks/...): point them to the backend
          const apiBase = api.defaults.baseURL || '';
          window.INVENTORY_API_BASE = apiBase;
          contentRef.current.innerHTML = fragmentResponse.data;
          contentRef.current.querySelectorAll('img[src^="/webhooks/"]').forEach(img => {
            img.src = apiBase + img.getAttribute('src');
          });
          
          // Execute scripts in the injected HTML (similar to auth.html logic)
          const scripts = contentRef.current.querySelectorAll('script');
          scripts.forEach(oldScript => {
            const newScript = document.createElement('script');
            Array.from(oldScript.attributes).forEach(attr => newScript.setAttribute(attr.name, attr.value));
            newScript.appendChild(document.createTextNode(oldScript.innerHTML));
            oldScript.parentNode.replaceChild(newScript, oldScript);
          });
        }
        
        setLoading(false);
      } catch (err) {
        console.error('Dashboard init error:', err);
        setError('Failed to load dashboard. Please try again.');
        setLoading(false);
      }
    };

    initDashboard();
  }, [shopId]);

  const handleLogout = () => {
    sessionStorage.clear();
    // Potentially call logout API
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
        <Loader2 className="w-10 h-10 text-slate-900 animate-spin mb-4" />
        <p className="text-slate-500 font-medium animate-pulse">Loading your shop dashboard...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-4">
        <div className="bg-white p-8 rounded-3xl shadow-sm border border-slate-100 max-w-md w-full text-center">
          <div className="w-16 h-16 bg-red-50 rounded-2xl flex items-center justify-center mx-auto mb-4">
            <AlertCircle className="w-8 h-8 text-red-500" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 mb-2">Error Loading Dashboard</h2>
          <p className="text-slate-500 mb-6">{error}</p>
          <button 
            onClick={() => window.location.reload()}
            className="w-full bg-slate-900 text-white font-bold py-3 rounded-xl hover:bg-slate-800 transition-colors"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F3F4F6] font-sans">
      {/* Navigation / Header */}
      <nav className="bg-white border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center gap-4 py-3">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-emerald-500 rounded-xl flex items-center justify-center shadow-lg shadow-emerald-100">
                <Store className="w-6 h-6 text-white" />
              </div>
              <div className="min-w-0 text-left">
                {/* Not an h1: the global h1 style (index.css) would blow it up and push the subtitle onto the border */}
                <p className="text-lg font-bold text-slate-900 leading-tight truncate" data-testid="shop-name">{shopInfo.name}</p>
                <div className="flex items-center gap-1.5 mt-1 text-[10px] font-bold text-emerald-600 uppercase tracking-wider">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                  Active Shop
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2">
              {/* Buttons, not links: the embedded fragment's Bootstrap CSS restyles every <a> on the page */}
              <button type="button" onClick={() => navigate('/shops')}
                      className="flex items-center gap-2 px-3 py-2 text-sm font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-50 rounded-xl transition-all">
                <ArrowLeft className="w-4 h-4" />
                <span className="hidden sm:inline">Мои магазины</span>
              </button>
              <button type="button" onClick={() => navigate(`/shops/${shopId}/connect`)}
                      className="flex items-center gap-2 px-3 py-2 text-sm font-semibold text-emerald-700 hover:bg-emerald-50 rounded-xl transition-all">
                <MessageCircle className="w-4 h-4" />
                <span className="hidden sm:inline">WhatsApp</span>
              </button>
              <button 
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-50 rounded-xl transition-all"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content Area */}
      <main className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
        <TestChatCard shopId={shopInfo.id} />
        <AiSettingsCard shopId={shopInfo.id} />

        {/* We inject the Thymeleaf fragment here */}
        {/* --inventory-sticky-top: the fragment's sticky form must stay below this page's sticky header */}
        <div ref={contentRef} className="thymeleaf-container" style={{ '--inventory-sticky-top': '6rem' }}>
          {/* Fragment content will be injected here */}
        </div>
      </main>

      {/* Footer / Mobile Nav could go here */}
    </div>
  );
};

export default DashboardPage;
