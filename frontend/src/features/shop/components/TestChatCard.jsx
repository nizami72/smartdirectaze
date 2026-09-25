import { useEffect, useRef, useState } from 'react';
import api from '../../../api/api.ts';
import { Loader2, MessageSquare, RotateCcw, Send } from 'lucide-react';

const newSessionId = () =>
  (crypto.randomUUID ? crypto.randomUUID() : String(Date.now()) + Math.random().toString(16).slice(2));

const EXAMPLES = ['Salam! Hansı məhsullar var?', 'Сколько стоит доставка?', 'Можно примерить перед покупкой?'];

// Renders WhatsApp formatting (*bold*) the way the customer will see it
const WhatsappText = ({ text }) =>
  text.split(/(\*[^*\n]+\*)/g).map((part, i) =>
    part.length > 2 && part.startsWith('*') && part.endsWith('*')
      ? <strong key={i}>{part.slice(1, -1)}</strong>
      : <span key={i}>{part}</span>
  );

// Lets the merchant chat with his AI seller as a customer, before connecting WhatsApp
const TestChatCard = ({ shopId }) => {
  const [sessionId, setSessionId] = useState(newSessionId);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
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
      setMessages(prev => [...prev, { from: 'error', text: 'AI не ответил. Попробуйте ещё раз.' }]);
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
          <h2 className="text-lg font-bold text-slate-900">Проверьте своего продавца</h2>
        </div>
        {messages.length > 0 && (
          <button type="button" onClick={restart} disabled={sending}
                  className="inline-flex items-center gap-1 text-sm font-semibold text-slate-500 hover:text-slate-800">
            <RotateCcw className="w-4 h-4" />
            Начать заново
          </button>
        )}
      </div>
      <p className="text-sm text-slate-500 mb-4">
        Напишите как покупатель. AI ответит по вашим товарам и условиям доставки — так же, как клиенту в WhatsApp.
      </p>

      <div className="bg-[#EFEAE2] rounded-2xl p-4 h-80 overflow-y-auto space-y-2">
        {messages.length === 0 && (
          <div className="flex flex-wrap gap-2">
            {EXAMPLES.map(example => (
              <button key={example} type="button" onClick={() => send(example)}
                      className="bg-white/80 hover:bg-white rounded-full px-3 py-1.5 text-sm text-slate-700 shadow-sm">
                {example}
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
        <div ref={bottomRef} />
      </div>

      <form onSubmit={e => { e.preventDefault(); send(input); }} className="flex gap-2 mt-3">
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          maxLength={500}
          placeholder="Сообщение от покупателя…"
          className="flex-1 min-w-0 border border-slate-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
        />
        <button type="submit" disabled={sending || !input.trim()} aria-label="Отправить"
                className="inline-flex items-center justify-center bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white rounded-xl px-4">
          <Send className="w-4 h-4" />
        </button>
      </form>
    </section>
  );
};

export default TestChatCard;
