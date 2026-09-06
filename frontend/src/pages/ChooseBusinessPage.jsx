import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/api.ts';
import { ShoppingBag, PartyPopper, Stethoscope, ArrowRight, Loader2 } from 'lucide-react';

const ChooseBusinessPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(null);
  const [error, setError] = useState('');

  const businessTypes = [
    {
      id: 'STORE',
      title: '🛍️ Online Store',
      description: 'Sell products via WhatsApp',
      icon: <ShoppingBag className="w-6 h-6 text-blue-600" />,
      color: 'blue',
      path: '/create-shop'
    },
    {
      id: 'EVENTS',
      title: '🎉 Events',
      description: 'Parties, weddings, birthdays',
      icon: <PartyPopper className="w-6 h-6 text-purple-600" />,
      color: 'purple',
      path: '/create-event'
    },
    {
      id: 'DENTAL',
      title: '🦷 Dental Clinic',
      description: 'Appointments and patients',
      icon: <Stethoscope className="w-6 h-6 text-teal-600" />,
      color: 'teal',
      path: '/create-clinic'
    }
  ];

  const handleChoose = async (type) => {
    setLoading(type.id);
    setError('');
    try {
      await api.post('/api/v1/businesses/choose-business', { businessType: type.id });
      navigate(type.path);
    } catch (err) {
      console.error('Error choosing business type:', err);
      setError('Failed to save selection. Please try again.');
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4 py-12 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-md w-full bg-white p-10 rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 relative overflow-hidden">
        
        <div className="absolute top-0 left-0 w-full h-1 bg-slate-100">
          <div className="w-1/2 h-full bg-slate-900 transition-all duration-500"></div>
        </div>

        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <span className="text-xs font-semibold uppercase tracking-wider text-slate-400 bg-slate-50 px-2.5 py-1 rounded-md">
              Step 2 of 5
            </span>
            <span className="text-xs text-slate-400">Business Type</span>
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900">
            What type of business do you have?
          </h2>
          <p className="mt-2 text-sm text-slate-500 leading-relaxed">
            We'll customize your experience based on your choice.
          </p>
        </div>

        {error && (
          <div className="mb-6 bg-red-50/70 border border-red-100 rounded-xl p-4 text-sm text-red-800 font-medium">
            {error}
          </div>
        )}

        <div className="space-y-4">
          {businessTypes.map((type) => (
            <button
              key={type.id}
              onClick={() => handleChoose(type)}
              disabled={loading !== null}
              className={`w-full group text-left p-5 rounded-2xl border-2 transition-all duration-200 flex items-center justify-between
                ${loading === type.id 
                  ? 'border-slate-900 bg-slate-50' 
                  : 'border-slate-100 hover:border-slate-300 hover:shadow-md bg-white'
                }`}
            >
              <div className="flex items-center gap-4">
                <div className={`p-3 rounded-xl bg-${type.color}-50 group-hover:scale-110 transition-transform`}>
                  {type.icon}
                </div>
                <div>
                  <h3 className="font-bold text-slate-900">{type.title}</h3>
                  <p className="text-sm text-slate-500">{type.description}</p>
                </div>
              </div>
              {loading === type.id ? (
                <Loader2 className="w-5 h-5 animate-spin text-slate-900" />
              ) : (
                <ArrowRight className="w-5 h-5 text-slate-300 group-hover:text-slate-900 transform group-hover:translate-x-1 transition-all" />
              )}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default ChooseBusinessPage;
