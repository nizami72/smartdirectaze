import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

export const formatAzerbaijanPhone = (value) => {
  const digits = value.replace(/\D/g, '');
  if (!digits.startsWith('994')) return '+994 ';
  
  let formatted = '+994';
  if (digits.length > 3) formatted += ' ' + digits.substring(3, 5);
  if (digits.length > 5) formatted += ' ' + digits.substring(5, 8);
  if (digits.length > 8) formatted += ' ' + digits.substring(8, 10);
  if (digits.length > 10) formatted += ' ' + digits.substring(10, 12);
  
  return formatted.trim();
};
