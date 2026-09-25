const STEPS = ['Магазин', 'Товары', 'Проверка', 'WhatsApp'];

// Progress of the shop onboarding: shop -> products -> test chat -> WhatsApp
const OnboardingSteps = ({ current }) => (
  <div className="mb-8 text-left">
    <div className="flex gap-1.5 mb-3">
      {STEPS.map((step, i) => (
        <div key={step} className={`h-1 flex-1 rounded-full ${i < current ? 'bg-emerald-500' : 'bg-slate-200'}`} />
      ))}
    </div>
    <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
      Шаг {current} из {STEPS.length} · {STEPS[current - 1]}
    </span>
  </div>
);

export default OnboardingSteps;
