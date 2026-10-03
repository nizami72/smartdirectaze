import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
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

// Texts the server sends (error messages): backend/src/main/resources/messages_<lang>.properties
const PROPERTIES_DIR = fileURLToPath(new URL('../../../backend/src/main/resources/', import.meta.url));

export function serverText(lang: Lang, key: string): string {
  for (const line of readFileSync(`${PROPERTIES_DIR}messages_${lang}.properties`, 'utf8').split('\n')) {
    const at = line.indexOf('=');
    if (!line.startsWith('#') && at > 0 && line.slice(0, at) === key) return line.slice(at + 1);
  }
  throw new Error(`No server text "${key}" in messages_${lang}.properties`);
}
