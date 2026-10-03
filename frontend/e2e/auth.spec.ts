import { test, expect } from './fixtures';
import { login, registerMerchant } from './support/merchant';
import { text } from './support/texts';

// Checks the shared setup itself: registration, login, logout

test('a new merchant is registered and logged in', async ({ merchantPage, merchant }) => {
  await expect(merchantPage.getByTestId('account-email')).toHaveText(merchant.email);
  await expect(merchantPage.getByText(text('az', 'createShop.title'))).toBeVisible();
});

test('the same merchant logs in again after logout, in Russian', async ({ page }) => {
  const { merchant } = await registerMerchant(page, 'ru');
  await page.getByTestId('logout').click();
  await expect(page).toHaveURL(/\/login$/);

  await login(page, merchant, 'ru');
  await expect(page.getByText(text('ru', 'createShop.title'))).toBeVisible();
});
