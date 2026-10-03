import { test, expect } from '@playwright/test';
import { logout, newMerchant, registerMerchant, submitRegistration } from './support/merchant';
import { serverText, text } from './support/texts';

test.describe('Registration', () => {
  test('a new merchant registers and lands on creating a shop', async ({ page }) => {
    const { merchant } = await registerMerchant(page, 'az');

    await expect(page.getByTestId('account-email')).toHaveText(merchant.email);
    await expect(page.getByText(text('az', 'createShop.title'))).toBeVisible();
  });

  test('without the pilot terms ticked the form is not sent', async ({ page }) => {
    await submitRegistration(page, newMerchant(), { acceptTerms: false });

    await expect(page.getByText(text('az', 'validation.termsRequired'))).toBeVisible();
    await expect(page).toHaveURL(/\/register$/);
    await expect(page.getByTestId('account-bar')).toHaveCount(0);
  });

  test('an email that is already registered is refused', async ({ page }) => {
    const { merchant } = await registerMerchant(page, 'az');
    await logout(page);

    // Same email, another name and phone
    await submitRegistration(page, { ...newMerchant(), email: merchant.email });

    await expect(page.getByTestId('register-error')).toHaveText(serverText('az', 'error.emailTaken'));
    await expect(page).toHaveURL(/\/register$/);
  });
});
