import { expect, type Page } from '@playwright/test';
import { text, type Lang } from './texts';

export interface Merchant {
  email: string;
  password: string;
  name: string;
  phone: string;
}

/**
 * A new merchant for one test: test<time>@example.com, so runs never collide and test accounts are easy
 * to find and delete (all of them end with @example.com).
 */
export function newMerchant(): Merchant {
  const stamp = `${Date.now()}${Math.floor(Math.random() * 1000)}`;
  return {
    email: `test${stamp}@example.com`,
    password: 'Test-12345!',
    name: 'E2E Test',
    // 994 50 + 7 digits from the time: a valid Azerbaijani number, different every run
    phone: `+99450${stamp.slice(-7)}`,
  };
}

/** Picks the interface language on a login or registration page */
export async function chooseLanguage(page: Page, lang: Lang) {
  await page.getByTestId(`lang-${lang}`).click();
  await expect(page.getByTestId(`lang-${lang}`)).toHaveAttribute('aria-pressed', 'true');
}

/**
 * Registers a new merchant through the real registration form (with the pilot terms ticked)
 * and returns the page, already logged in. A new merchant has no shops, so the site
 * lands on "create a shop".
 */
export async function registerMerchant(page: Page, lang: Lang = 'az', merchant: Merchant = newMerchant()) {
  await page.goto('/register');
  await chooseLanguage(page, lang);
  await expect(page.getByRole('heading', { name: text(lang, 'register.title') })).toBeVisible();

  await page.locator('#name').fill(merchant.name);
  await page.locator('#email').fill(merchant.email);
  await page.locator('#phone').fill(merchant.phone);
  await page.locator('#password').fill(merchant.password);
  await page.getByTestId('register-terms').check();
  await page.getByTestId('register-submit').click();

  await expectLoggedIn(page, merchant.email);
  await expect(page).toHaveURL(/\/shops\/new$/);
  return { page, merchant };
}

/** Logs an existing merchant in through the login page */
export async function login(page: Page, merchant: Pick<Merchant, 'email' | 'password'>, lang: Lang = 'az') {
  await page.goto('/login');
  await chooseLanguage(page, lang);
  await expect(page.getByRole('heading', { name: text(lang, 'login.title') })).toBeVisible();

  await page.locator('#email').fill(merchant.email);
  await page.locator('#password').fill(merchant.password);
  await page.getByTestId('login-submit').click();

  await expectLoggedIn(page, merchant.email);
  return page;
}

/** Logged in = the account bar shows this email */
export async function expectLoggedIn(page: Page, email: string) {
  await expect(page.getByTestId('register-error').or(page.getByTestId('login-error'))).toHaveCount(0);
  await expect(page.getByTestId('account-email')).toHaveText(email, { timeout: 15_000 });
}
