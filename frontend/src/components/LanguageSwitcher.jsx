import { useTranslation } from 'react-i18next';
import { LANGUAGES, setLanguage } from '../i18n';

// AZ / RU toggle; dark = for the account bar, light = for login and registration pages
const LanguageSwitcher = ({ dark = false, saveToProfile = false }) => {
  const { i18n } = useTranslation();
  return (
    <div className={`flex rounded-lg p-0.5 text-xs font-semibold ${dark ? 'bg-slate-800' : 'bg-slate-100'}`}>
      {LANGUAGES.map(({ code, label }) => {
        const active = i18n.language === code;
        return (
          <button key={code} type="button" onClick={() => setLanguage(code, { saveToProfile })} data-testid={`lang-${code}`}
                  aria-pressed={active}
                  className={`px-2 py-0.5 rounded-md transition-colors ${
                    active
                      ? (dark ? 'bg-slate-100 text-slate-900' : 'bg-white text-slate-900 shadow-sm')
                      : (dark ? 'text-slate-400 hover:text-white' : 'text-slate-500 hover:text-slate-800')
                  }`}>
            {label}
          </button>
        );
      })}
    </div>
  );
};

export default LanguageSwitcher;
