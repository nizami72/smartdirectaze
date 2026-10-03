import { useEffect, useRef, useState } from 'react';
import api from '../../../api/api.ts';
import { Loader2, MessageSquare, RotateCcw, Send } from 'lucide-react';
import { useTranslation } from 'react-i18next';

const newSessionId = () =>
  (crypto.randomUUID ? crypto.randomUUID() : String(Date.now()) + Math.random().toString(16).slice(2));

const EXAMPLES = ['testChat.example1', 'testChat.example2', 'testChat.example3'];

// Renders WhatsApp formatting (*bold*) the way the customer will see it
const WhatsappText = ({ text }) =>
  text.split(/(\*[^*\n]+\*)/g).map((part, i) =>
    part.length > 2 && part.startsWith('*') && part.endsWith('*')
      ? <strong key={i}>{part.slice(1, -1)}</strong>
      : <span key={i}>{part}</span>
  );

// Lets the merchant chat with his AI seller as a customer, before connecting WhatsApp
const TestChatCard = ({ shopId }) => {
  const { t } = useTranslation();
  const [sessionId, setSessionId] = useState(newSessionId);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const listRef = useRef(null);

  // Scroll only the message list: scrollIntoView would move the whole dashboard to the chat on load
  useEffect(() => {
    const list = listRef.current;
    if (list) list.scrollTo({ top: list.scrollHeight, behavior: 'smooth' });
  }, [messages, sending]);

  const send = async (text) => {
    const message = text.trim();
    if (!message || sending || !shopId) return;
    setInput('');
    setMessages(prev => [...prev, { from: 'customer', text: message }]);
    setSending(true);
    try {
      const res = await api.post(`/api/v1/shops/${shopId}/ai/test-chat`, { message, sessionId });
      setMessages(prev => [...prev, { from: 'ai', text: res.data.reply }]);
    } catch {
      setMessages(prev => [...prev, { from: 'error', text: t('testChat.noAnswer') }]);
    } finally {
      setSending(false);
    }
  };

  const restart = () => {
    setSessionId(newSessionId());
    setMessages([]);
  };

  return (
    <section className="bg-white rounded-3xl border border-slate-200 p-6 mb-8 text-left">
      <div className="flex items-center justify-between gap-3 mb-1">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center">
            <MessageSquare className="w-5 h-5 text-emerald-600" />
          </div>
          <h2 className="text-lg font-bold text-slate-900">{t('testChat.title')}</h2>
        </div>
        {messages.length > 0 && (
          <button type="button" onClick={restart} disabled={sending}
                  className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-slate-800">
            <RotateCcw className="w-4 h-4" />
            {t('testChat.restart')}
          </button>
        )}
      </div>
      <p className="text-sm text-slate-500 mb-4">
        {t('testChat.intro')}
      </p>

      <div ref={listRef} className="bg-[#EFEAE2] rounded-2xl p-4 h-80 overflow-y-auto space-y-2">
        {messages.length === 0 && (
          <div className="flex flex-wrap gap-2">
            {EXAMPLES.map(example => (
              <button key={example} type="button" onClick={() => send(t(example))}
                      className="bg-white/80 hover:bg-white rounded-full px-3 py-1.5 text-sm text-slate-700 shadow-sm">
                {t(example)}
              </button>
            ))}
          </div>
        )}
        {messages.map((m, i) => (
          <div key={i} className={`flex ${m.from === 'customer' ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[85%] rounded-xl px-3 py-2 text-sm whitespace-pre-wrap break-words shadow-sm ${
              m.from === 'customer' ? 'bg-[#D9FDD3] text-slate-900'
                : m.from === 'error' ? 'bg-red-50 text-red-700' : 'bg-white text-slate-900'
            }`}>
              <WhatsappText text={m.text} />
            </div>
          </div>
        ))}
        {sending && (
          <div className="flex justify-start">
            <div className="bg-white rounded-xl px-3 py-2 shadow-sm">
              <Loader2 className="w-4 h-4 text-slate-400 animate-spin" />
            </div>
          </div>
        )}
      </div>

      <form onSubmit={e => { e.preventDefault(); send(input); }} className="flex gap-2 mt-3">
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          maxLength={500}
          placeholder={t('testChat.placeholder')}
          className="flex-1 min-w-0 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
        />
        <button type="submit" disabled={sending || !input.trim()} aria-label={t('testChat.send')}
                className="inline-flex items-center justify-center bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-xl px-4">
          <Send className="w-4 h-4" />
        </button>
      </form>
    </section>
  );
};

export default TestChatCard;
