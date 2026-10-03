import { useEffect, useState } from 'react';
import api from '../../../api/api.ts';
import { Bot, Hand, Loader2, MessageCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { formatShortDateTime } from '../../../i18n';

const formatTime = formatShortDateTime;

// Chats handed over to the seller: the AI is silent there until the seller gives them back
const HandoffCard = ({ shopId }) => {
  const { t } = useTranslation();
  const [chats, setChats] = useState([]);
  const [resumingId, setResumingId] = useState(null);

  useEffect(() => {
    if (!shopId) return;
    api.get(`/api/v1/shops/${shopId}/conversations/waiting`)
      .then(res => setChats(res.data || []))
      .catch(() => setChats([]));
  }, [shopId]);

  const resumeAi = async (id) => {
    setResumingId(id);
    try {
      await api.post(`/api/v1/shops/${shopId}/conversations/${id}/resume-ai`);
      setChats(prev => prev.filter(c => c.id !== id));
    } catch {
      alert(t('handoff.resumeFailed'));
    } finally {
      setResumingId(null);
    }
  };

  if (chats.length === 0) return null;

  return (
    <section className="bg-white rounded-3xl border-2 border-amber-300 p-6 mb-8 text-left">
      <div className="flex items-center gap-3 mb-1">
        <div className="w-10 h-10 bg-amber-50 rounded-xl flex items-center justify-center">
          <Hand className="w-5 h-5 text-amber-600" />
        </div>
        <p className="text-lg font-bold text-slate-900">{t('handoff.title', { count: chats.length })}</p>
      </div>
      <p className="text-sm text-slate-500 mb-4">
        {t('handoff.intro')}
      </p>

      <div className="space-y-3">
        {chats.map(chat => (
          <div key={chat.id} className="rounded-2xl border border-slate-200 p-4">
            <div className="flex flex-wrap items-start justify-between gap-2">
              <div className="min-w-0">
                <p className="font-semibold text-slate-900">
                  +{chat.customerPhone}{chat.customerName ? ` · ${chat.customerName}` : ''}
                </p>
                <p className="text-sm text-amber-700">{chat.reason}</p>
                <p className="text-xs text-slate-400 mt-0.5">
                  {chat.aiPaused ? t('handoff.paused') : t('handoff.answering')}
                </p>
              </div>
              <span className="text-xs text-slate-400">{formatTime(chat.lastMessageAt)}</span>
            </div>
            {chat.lastMessage && (
              <p className="mt-2 text-sm text-slate-600 bg-slate-50 rounded-xl px-3 py-2 break-words">«{chat.lastMessage}»</p>
            )}
            <div className="mt-3 flex flex-wrap gap-2">
              <a href={`https://wa.me/${chat.customerPhone}`} target="_blank" rel="noopener noreferrer"
                 style={{ textDecoration: 'none' }}
                 className="inline-flex items-center gap-1.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl px-3 py-2 text-sm font-semibold">
                <MessageCircle className="w-4 h-4" /> {t('handoff.write')}
              </a>
              <button type="button" onClick={() => resumeAi(chat.id)} disabled={resumingId === chat.id}
                      className="inline-flex items-center gap-1.5 border border-slate-200 hover:bg-slate-50 rounded-xl px-3 py-2 text-sm font-semibold text-slate-700 disabled:opacity-50">
                {resumingId === chat.id ? <Loader2 className="w-4 h-4 animate-spin" /> : <Bot className="w-4 h-4" />}
                {chat.aiPaused ? t('handoff.resume') : t('handoff.resolve')}
              </button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
};

export default HandoffCard;
