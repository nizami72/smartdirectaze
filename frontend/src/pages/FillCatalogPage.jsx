import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';
import { 
  Package, 
  Plus, 
  ArrowRight, 
  Loader2, 
  AlertCircle,
  Tag,
  AlignLeft,
  DollarSign
} from 'lucide-react';

const FillCatalogPage = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    price: '',
    description: ''
  });
  const [loading, setLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [error, setError] = useState('');
  const [shopId, setShopId] = useState(null);

  React.useEffect(() => {
    const id = sessionStorage.getItem('currentShopId');
    if (!id) {
      // Можно добавить редирект или запрос к API если id нет
      api.get('/api/v1/shops/my').then(res => {
        if (res.data.length > 0) {
          sessionStorage.setItem('currentShopId', res.data[0].id);
          sessionStorage.setItem('currentShopName', res.data[0].shopName);
          setShopId(res.data[0].id);
        } else {
          navigate('/create-shop');
        }
      }).catch(() => navigate('/login'));
    } else {
      setShopId(id);
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAddProduct = async (e) => {
    e.preventDefault();
    if (!formData.name || !formData.price) return;

    setLoading(true);
    setError('');
    try {
      const response = await api.post('/api/v1/shops/catalog', {
        shopId: shopId,
        name: formData.name,
        price: parseFloat(formData.price),
        description: formData.description
      });
      
      setProducts(prev => [...prev, response.data]);
      setFormData({ name: '', price: '', description: '' });
    } catch (err) {
      console.error('Add product error:', err);
      setError(err.response?.data?.message || 'Не удалось добавить товар. Попробуйте снова.');
    } finally {
      setLoading(false);
    }
  };

  const handleFinish = () => {
    if (products.length > 0) {
      navigate('/connect-channels');
    }
  };

  return (
    <div className="min-h-screen bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-4xl mx-auto">
        
        {/* Шапка с прогрессом */}
        <div className="mb-10 text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-100 text-slate-500 text-xs font-bold uppercase tracking-wider mb-4">
            Шаг 3 из 4: Наполнение каталога
          </div>
          <h1 className="text-3xl font-bold text-slate-900">Добавьте первые товары</h1>
          <p className="mt-2 text-slate-500 max-w-md mx-auto">
            ИИ-ассистенту нужны данные о ваших товарах, чтобы он мог отвечать на вопросы клиентов о ценах и наличии.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          
          {/* Форма добавления */}
          <div className="bg-white p-8 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100">
            <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2">
              <Plus className="w-5 h-5 text-slate-900" />
              Новый товар
            </h2>

            <form onSubmit={handleAddProduct} className="space-y-5">
              {error && (
                <div className="bg-red-50 border border-red-100 rounded-xl p-4 flex items-start">
                  <AlertCircle className="h-5 w-5 text-red-500 mr-2.5 mt-0.5 flex-shrink-0" />
                  <span className="text-sm text-red-800">{error}</span>
                </div>
              )}

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wide px-1">Название товара</label>
                <div className="relative">
                  <Tag className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Например: Шелковое платье"
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all"
                    required
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wide px-1">Цена (AZN)</label>
                <div className="relative">
                  <DollarSign className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                  <input
                    type="number"
                    name="price"
                    value={formData.price}
                    onChange={handleChange}
                    placeholder="0.00"
                    step="0.01"
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all"
                    required
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-500 uppercase tracking-wide px-1">Описание</label>
                <div className="relative">
                  <AlignLeft className="absolute left-3.5 top-3 h-4 w-4 text-slate-400" />
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    rows="3"
                    placeholder="Размеры, состав, доступные цвета..."
                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-slate-900/5 focus:border-slate-900 transition-all resize-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 bg-slate-900 hover:bg-slate-800 text-white font-bold py-3.5 rounded-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-slate-200"
              >
                {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Plus className="w-5 h-5" />}
                Добавить в каталог
              </button>
            </form>
          </div>

          {/* Список карточек */}
          <div className="space-y-6">
            <div className="flex items-center justify-between px-2">
              <h2 className="text-xl font-bold text-slate-900">Добавленные товары</h2>
              <span className="bg-slate-100 text-slate-600 text-xs font-bold px-2.5 py-1 rounded-lg">
                {products.length}
              </span>
            </div>

            <div className="space-y-4 max-h-[500px] overflow-y-auto pr-2 custom-scrollbar">
              {products.length === 0 ? (
                <div className="text-center py-20 bg-slate-50 border-2 border-dashed border-slate-200 rounded-2xl">
                  <Package className="w-10 h-10 text-slate-300 mx-auto mb-3" />
                  <p className="text-slate-400 text-sm">Вы еще не добавили ни одного товара</p>
                </div>
              ) : (
                products.map((product, idx) => (
                  <div key={idx} className="bg-white p-5 rounded-2xl border border-slate-100 shadow-sm flex items-start gap-4 animate-in slide-in-from-right duration-300">
                    <div className="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center flex-shrink-0">
                      <Package className="w-6 h-6 text-slate-400" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-2">
                        <h3 className="font-bold text-slate-900 truncate">
                          {product.titles?.ru || 'Без названия'}
                        </h3>
                        <span className="text-slate-900 font-bold whitespace-nowrap">
                          {product.salePrice} AZN
                        </span>
                      </div>
                      <p className="text-sm text-slate-500 mt-1 line-clamp-2 leading-relaxed">
                        {product.descriptions?.ru || 'Описание отсутствует'}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>

            <button
              onClick={handleFinish}
              disabled={products.length === 0}
              className="w-full mt-4 flex items-center justify-center gap-2 bg-white border-2 border-slate-900 text-slate-900 hover:bg-slate-50 font-bold py-3.5 rounded-xl transition-all disabled:opacity-30 disabled:cursor-not-allowed"
            >
              Продолжить
              <ArrowRight className="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default FillCatalogPage;
