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

// Same rules as PhoneUtils.normalize on the server: full international digits, or null when the number is wrong.
// Azerbaijan: "050 467 99 33", "50 467 99 33", "+994 50 467 99 33" -> "994504679933" (994 + 9 digits)
export const normalizePhone = (value) => {
  let d = (value || '').replace(/\D/g, '');
  if (d.startsWith('00')) d = d.slice(2);
  if (d.length === 10 && d.startsWith('0')) d = '994' + d.slice(1);
  else if (d.length === 9 && !d.startsWith('994')) d = '994' + d;
  if (d.startsWith('994')) return d.length === 12 ? d : null;
  return d.length >= 11 && d.length <= 15 ? d : null;
};

// "994504679933" -> "+994 50 467 99 33"
export const prettyPhone = (digits) => {
  const d = (digits || '').replace(/\D/g, '');
  if (d.length === 12 && d.startsWith('994')) {
    return `+994 ${d.slice(3, 5)} ${d.slice(5, 8)} ${d.slice(8, 10)} ${d.slice(10)}`;
  }
  return d ? `+${d}` : '';
};
