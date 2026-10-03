import { test, expect } from './fixtures';
import { text } from './support/texts';

test.describe('Creating a shop', () => {
  test('a new shop opens its catalog step', async ({ merchantPage: page }) => {
    await expect(page.getByText(text('az', 'onboarding.step', { current: 1, total: 4, name: text('az', 'onboarding.shop') }))).toBeVisible();

    await page.locator('#shopName').fill(`E2E Shop ${Date.now()}`);
    await page.locator('#deliveryPrice').fill('3');
    await page.locator('#freeDeliveryThreshold').fill('50');
    await page.getByTestId('create-shop-submit').click();

    await expect(page).toHaveURL(/\/shops\/\d+\/catalog$/);
    await expect(page.getByRole('heading', { name: text('az', 'catalog.title') })).toBeVisible();
    await expect(page.getByText(text('az', 'onboarding.step', { current: 2, total: 4, name: text('az', 'onboarding.products') }))).toBeVisible();
  });

  test('a shop without a name is not created', async ({ merchantPage: page }) => {
    await page.getByTestId('create-shop-submit').click();

    await expect(page.getByTestId('create-shop-error')).toHaveText(text('az', 'createShop.nameRequired'));
    await expect(page).toHaveURL(/\/shops\/new$/);
  });
});
