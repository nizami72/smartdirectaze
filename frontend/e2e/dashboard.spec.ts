import { test, expect } from './fixtures';
import { createShopAndOpenDashboard } from './support/merchant';
import { serverText, text } from './support/texts';

test.describe('Shop dashboard: products and delivery', () => {
  test('an added product appears in the list; the in-stock switch is saved', async ({ merchantPage: page }) => {
    await createShopAndOpenDashboard(page);
    await expect(page.getByText(serverText('az', 'inv.empty'))).toBeVisible();

    const form = page.getByTestId('product-form');
    await form.locator('[name="name"]').fill('Qara dəri çanta');
    await form.locator('[name="salePrice"]').fill('45');
    await form.getByRole('button', { name: serverText('az', 'inv.saveProduct') }).click();

    const card = page.getByTestId('product-card').filter({ hasText: 'Qara dəri çanta' });
    await expect(card).toBeVisible({ timeout: 15_000 });
    await expect(card).toContainText('45');
    await expect(card).toContainText(serverText('az', 'inv.inStock'));

    // Switch off: saved at once, the dashboard reloads and still shows "out of stock"
    await card.getByTestId('stock-switch').click();
    await expect(card).toContainText(serverText('az', 'inv.outOfStock'), { timeout: 15_000 });
    await page.reload();
    const reloaded = page.getByTestId('product-card').filter({ hasText: 'Qara dəri çanta' });
    await expect(reloaded).toContainText(serverText('az', 'inv.outOfStock'), { timeout: 15_000 });
    await expect(reloaded.getByTestId('stock-switch')).not.toBeChecked();

    // And back on
    await reloaded.getByTestId('stock-switch').click();
    await expect(page.getByTestId('product-card').filter({ hasText: 'Qara dəri çanta' }))
      .toContainText(serverText('az', 'inv.inStock'), { timeout: 15_000 });
  });

  test('delivery conditions are saved', async ({ merchantPage: page }) => {
    await createShopAndOpenDashboard(page);
    await page.getByTestId('delivery-tab').click();

    const form = page.getByTestId('delivery-form');
    await expect(form).toBeVisible();
    await form.locator('[name="freeDeliveryThreshold"]').fill('60');
    await form.locator('[name="regionalDeliveryCost"]').fill('4');
    await form.locator('[name="address"]').fill('Bakı, Nizami küç. 10');
    await form.locator('[name="workingHours"]').fill('10:00–20:00');
    await form.getByRole('button', { name: serverText('az', 'inv.saveDelivery') }).click();

    // Saved = after the reload the form shows the new values
    await page.waitForLoadState('load');
    await expect(page.getByTestId('product-form')).toBeVisible({ timeout: 15_000 });
    await page.getByTestId('delivery-tab').click();
    const saved = page.getByTestId('delivery-form');
    await expect(saved.locator('[name="freeDeliveryThreshold"]')).toHaveValue(/^60(\.0+)?$/);
    await expect(saved.locator('[name="regionalDeliveryCost"]')).toHaveValue(/^4(\.0+)?$/);
    await expect(saved.locator('[name="address"]')).toHaveValue('Bakı, Nizami küç. 10');
    await expect(saved.locator('[name="workingHours"]')).toHaveValue('10:00–20:00');
  });
});

test.describe('Shop dashboard: AI seller card', () => {
  test('modes Off / Test / For everyone, with a confirmation before "For everyone"', async ({ merchantPage: page }) => {
    await createShopAndOpenDashboard(page);
    // A new shop starts in Test
    await expect(page.getByTestId('ai-mode-TEST')).toHaveAttribute('aria-pressed', 'true');

    await page.getByTestId('ai-mode-OFF').click();
    await expect(page.getByTestId('ai-mode-OFF')).toHaveAttribute('aria-pressed', 'true');
    await expect(page.getByTestId('ai-mode-hint')).toHaveText(text('az', 'ai.modeOffHint'));

    // "For everyone" first asks to confirm, Cancel keeps the mode
    await page.getByTestId('ai-mode-ON').click();
    await expect(page.getByTestId('ai-confirm-on')).toContainText(text('az', 'ai.confirmOnWarning'));
    await page.getByTestId('ai-confirm-on').getByRole('button', { name: text('az', 'common.cancel') }).click();
    await expect(page.getByTestId('ai-confirm-on')).toHaveCount(0);
    await expect(page.getByTestId('ai-mode-OFF')).toHaveAttribute('aria-pressed', 'true');

    await page.getByTestId('ai-mode-ON').click();
    await page.getByTestId('ai-confirm-on-yes').click();
    await expect(page.getByTestId('ai-mode-ON')).toHaveAttribute('aria-pressed', 'true');
    await expect(page.getByTestId('ai-mode-hint')).toHaveText(text('az', 'ai.modeOnHint'));

    // Saved on the server: still "For everyone" after a reload
    await page.reload();
    await expect(page.getByTestId('ai-mode-ON')).toHaveAttribute('aria-pressed', 'true');

    await page.getByTestId('ai-mode-TEST').click();
    await expect(page.getByTestId('ai-mode-TEST')).toHaveAttribute('aria-pressed', 'true');
    await expect(page.getByTestId('ai-mode-hint')).toHaveText(text('az', 'ai.modeTestHint'));
  });

  test('a wrong test number is refused, a right one is added', async ({ merchantPage: page }) => {
    await createShopAndOpenDashboard(page);
    await expect(page.getByTestId('ai-mode-TEST')).toHaveAttribute('aria-pressed', 'true');

    // The number from the pilot: "50" was missing and the AI kept silent
    await page.getByTestId('test-phone-input').fill('+9944679933');
    await page.getByTestId('test-phone-add').click();
    await expect(page.getByTestId('test-phone-error')).toHaveText(text('az', 'ai.invalidPhone'));
    await expect(page.getByTestId('test-phone')).toHaveCount(0);

    await page.getByTestId('test-phone-input').fill('050 467 99 33');
    await page.getByTestId('test-phone-add').click();
    await expect(page.getByTestId('test-phone')).toHaveText('+994 50 467 99 33');
    await expect(page.getByTestId('test-phone-error')).toHaveCount(0);
  });
});

test('the orders page opens', async ({ merchantPage: page }) => {
  const shopId = await createShopAndOpenDashboard(page);
  await page.getByRole('button', { name: text('az', 'dashboard.orders') }).click();

  await expect(page).toHaveURL(new RegExp(`/shops/${shopId}/orders$`));
  await expect(page.getByText(text('az', 'orders.title'), { exact: true })).toBeVisible();
  await expect(page.getByText(text('az', 'orders.noActive'))).toBeVisible();
});
