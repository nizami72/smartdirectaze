import { test, expect } from './fixtures';
import { login, logout, submitLogin } from './support/merchant';
import { text } from './support/texts';

test.describe('Login and logout with the account bar', () => {
  test('logout leaves the account and closes the merchant pages', async ({ merchantPage: page }) => {
    await logout(page);

    // A merchant page without a session sends back to the login page
    await page.goto('/shops');
    await expect(page).toHaveURL(/\/login$/);
  });

  test('the merchant logs in again after logout', async ({ merchantPage: page, merchant }) => {
    await logout(page);
    await login(page, merchant);

    await expect(page.getByTestId('account-email')).toHaveText(merchant.email);
    await expect(page.getByText(text('az', 'createShop.title'))).toBeVisible();
  });

  test('a wrong password is refused', async ({ merchantPage: page, merchant }) => {
    await logout(page);
    await submitLogin(page, { email: merchant.email, password: 'wrong-password-1' });

    await expect(page.getByTestId('login-error')).toHaveText(text('az', 'login.invalid'));
    await expect(page.getByTestId('account-bar')).toHaveCount(0);
  });
});
