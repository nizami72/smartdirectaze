import az from '../../src/i18n/az.json' with { type: 'json' };
import ru from '../../src/i18n/ru.json' with { type: 'json' };

// Texts of the interface, the same dictionaries the site uses: tests look for what the merchant sees
export type Lang = 'az' | 'ru';
const DICTIONARIES = { az, ru };

/** Text by key, e.g. text('az', 'register.title'); {{name}} placeholders are filled from vars */
export function text(lang: Lang, key: string, vars: Record<string, string | number> = {}): string {
  const value = key.split('.').reduce<unknown>(
    (node, part) => (node && typeof node === 'object' ? (node as Record<string, unknown>)[part] : undefined),
    DICTIONARIES[lang],
  );
  if (typeof value !== 'string') {
    throw new Error(`No text "${key}" in ${lang}.json`);
  }
  return value.replace(/\{\{(\w+)\}\}/g, (_, name) => String(vars[name] ?? `{{${name}}}`));
}
