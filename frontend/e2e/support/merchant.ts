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
  await submitRegistration(page, merchant, { lang });

  await expectLoggedIn(page, merchant.email);
  await expect(page).toHaveURL(/\/shops\/new$/);
  return { page, merchant };
}

/**
 * Opens the registration page, fills the form and sends it, without checking the outcome:
 * for tests of refused registrations. acceptTerms: false leaves the pilot terms unticked.
 */
export async function submitRegistration(page: Page, merchant: Merchant,
                                         { lang = 'az', acceptTerms = true }: { lang?: Lang; acceptTerms?: boolean } = {}) {
  await page.goto('/register');
  await chooseLanguage(page, lang);
  await expect(page.getByRole('heading', { name: text(lang, 'register.title') })).toBeVisible();

  await page.locator('#name').fill(merchant.name);
  await page.locator('#email').fill(merchant.email);
  await page.locator('#phone').fill(merchant.phone);
  await page.locator('#password').fill(merchant.password);
  if (acceptTerms) {
    await page.getByTestId('register-terms').check();
  }
  await page.getByTestId('register-submit').click();
}

/** Logs an existing merchant in through the login page */
export async function login(page: Page, merchant: Pick<Merchant, 'email' | 'password'>, lang: Lang | null = 'az') {
  await submitLogin(page, merchant, lang);
  await expectLoggedIn(page, merchant.email);
  return page;
}

/** Fills and sends the login form without checking the outcome. lang: null keeps the language already chosen */
export async function submitLogin(page: Page, merchant: Pick<Merchant, 'email' | 'password'>, lang: Lang | null = 'az') {
  await page.goto('/login');
  if (lang) {
    await chooseLanguage(page, lang);
  }
  await expect(page.getByTestId('login-submit')).toBeVisible();
  await page.locator('#email').fill(merchant.email);
  await page.locator('#password').fill(merchant.password);
  await page.getByTestId('login-submit').click();
}

/** Logout with the button in the account bar */
export async function logout(page: Page) {
  await page.getByTestId('logout').click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByTestId('account-bar')).toHaveCount(0);
}

/** Logged in = the account bar shows this email */
export async function expectLoggedIn(page: Page, email: string) {
  await expect(page.getByTestId('register-error').or(page.getByTestId('login-error'))).toHaveCount(0);
  await expect(page.getByTestId('account-email')).toHaveText(email, { timeout: 15_000 });
}

/** Creates a shop through the real form and opens its dashboard; returns the shop id */
export async function createShopAndOpenDashboard(page: Page, name = `E2E Shop ${Date.now()}`) {
  await page.goto('/shops/new');
  await page.locator('#shopName').fill(name);
  await page.locator('#deliveryPrice').fill('3');
  await page.getByTestId('create-shop-submit').click();
  await expect(page).toHaveURL(/\/shops\/\d+\/catalog$/);
  const shopId = page.url().match(/\/shops\/(\d+)\//)![1];

  await page.goto(`/shops/${shopId}`);
  await expect(page.getByTestId('shop-name')).toHaveText(name);
  // The "Products and delivery" part is a server page loaded into the dashboard
  await expect(page.getByTestId('product-form')).toBeVisible({ timeout: 15_000 });
  return shopId;
}

/** Adds a product with the form on the "Products and delivery" page and waits for its card */
export async function addProduct(page: Page, name: string, price: string) {
  const form = page.getByTestId('product-form');
  await form.locator('[name="name"]').fill(name);
  await form.locator('[name="salePrice"]').fill(price);
  await form.locator('button[type="submit"]').click();
  await expect(page.getByTestId('product-card').filter({ hasText: name })).toBeVisible({ timeout: 15_000 });
}
