import { useTranslation } from 'react-i18next';

const STEPS = ['onboarding.shop', 'onboarding.products', 'onboarding.check', 'onboarding.whatsapp'];

// Progress of the shop onboarding: shop -> products -> test chat -> WhatsApp
const OnboardingSteps = ({ current }) => {
  const { t } = useTranslation();
  return (
    <div className="mb-8 text-left">
      <div className="flex gap-1.5 mb-3">
        {STEPS.map((step, i) => (
          <div key={step} className={`h-1 flex-1 rounded-full ${i < current ? 'bg-emerald-500' : 'bg-slate-200'}`} />
        ))}
      </div>
      <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
        {t('onboarding.step', { current, total: STEPS.length, name: t(STEPS[current - 1]) })}
      </span>
    </div>
  );
};

export default OnboardingSteps;
