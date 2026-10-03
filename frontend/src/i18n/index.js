import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import az from './az.json';
import ru from './ru.json';
import api from '../api/api.ts';

// Interface languages; Azerbaijani first: most Baku merchants read it before Russian
export const LANGUAGES = [
  { code: 'az', label: 'AZ' },
  { code: 'ru', label: 'RU' },
];
const STORAGE_KEY = 'lang';

const saved = (() => {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
})();

i18n.use(initReactI18next).init({
  resources: { az: { translation: az }, ru: { translation: ru } },
  lng: LANGUAGES.some(l => l.code === saved) ? saved : 'az',
  fallbackLng: 'az',
  interpolation: { escapeValue: false },
});

// The server answers in the same language: dashboard page, error messages
const applyToPage = (lng) => {
  document.documentElement.lang = lng;
  api.defaults.headers.common['Accept-Language'] = lng;
};
applyToPage(i18n.language);
i18n.on('languageChanged', applyToPage);

// Switch the language in this browser; for a logged-in user also in the profile (WhatsApp alerts use it)
export const setLanguage = async (code, { saveToProfile = false } = {}) => {
  await i18n.changeLanguage(code);
  try {
    localStorage.setItem(STORAGE_KEY, code);
  } catch {
    // private window: the choice lives until the tab is closed
  }
  if (saveToProfile) {
    api.put('/api/v1/auth/me/locale', { locale: code }).catch(() => {});
  }
};

// After login: the language saved in the profile wins, unless this browser already chose one
export const applyProfileLanguage = (profileLocale) => {
  if (saved || !profileLocale || profileLocale === i18n.language) return;
  if (LANGUAGES.some(l => l.code === profileLocale)) i18n.changeLanguage(profileLocale);
};

// Short day.month hh:mm in the interface language (orders, chats waiting for the seller)
export const formatShortDateTime = (value) =>
  value ? new Date(value).toLocaleString(i18n.language === 'ru' ? 'ru-RU' : 'az-AZ',
    { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' }) : '';

// Date and time in the interface language
export const formatDateTime = (value) =>
  new Date(value).toLocaleString(i18n.language === 'ru' ? 'ru-RU' : 'az-AZ', { dateStyle: 'short', timeStyle: 'short' });

export default i18n;
