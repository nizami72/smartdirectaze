import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

export const formatAzerbaijanPhone = (value) => {
  let digits = value.replace(/\D/g, '');
  // Part of the prefix erased ("+99"): keep the prefix, not "+994 99"
  if ('994'.startsWith(digits)) return '+994';
  // Local format ("050 123 45 67", "501234567"): drop the leading 0 and add the country code
  if (!digits.startsWith('994')) digits = '994' + digits.replace(/^0+/, '');
  
  let formatted = '+994';
  if (digits.length > 3) formatted += ' ' + digits.substring(3, 5);
  if (digits.length > 5) formatted += ' ' + digits.substring(5, 8);
  if (digits.length > 8) formatted += ' ' + digits.substring(8, 10);
  if (digits.length > 10) formatted += ' ' + digits.substring(10, 12);
  
  return formatted.trim();
};
