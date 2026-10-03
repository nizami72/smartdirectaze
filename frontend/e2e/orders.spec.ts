import { test, expect, type Page } from '@playwright/test';
import { text } from './support/texts';

// The orders page with an order in it. Orders are created only by the AI in WhatsApp, so the server
// answers are stubbed here: the test needs no backend, no AI and no real order.

const ORDER = {
  id: 1, shopId: 1, status: 'NEW', customerName: 'Leyla', phoneNumber: '+994 50 467 99 33',
  deliveryAddress: 'Nizami küçəsi 5, mənzil 12', itemsSummary: 'Кроссовки женские белые x1, 38',
  paymentMethod: 'CASH', createdAt: '2026-10-03T11:53:42',
};

async function openOrdersWithOneOrder(page: Page) {
  const patches: string[] = [];
  await page.route('**/api/v1/shops/1/orders', route => route.fulfill({ json: [ORDER] }));
  await page.route('**/api/v1/shops/1/orders/1/status', async route => {
    const { status } = route.request().postDataJSON();
    patches.push(status);
    await route.fulfill({ json: { ...ORDER, status } });
  });
  await page.goto('/shops/1/orders');
  return patches;
}

test.describe('Orders page with an order', () => {
  test('the order card is shown', async ({ page }) => {
    await openOrdersWithOneOrder(page);

    await expect(page.getByText(`${text('az', 'orders.request')} #1`)).toBeVisible();
    await expect(page.getByText('Leyla')).toBeVisible();
    await expect(page.getByTestId('order-status')).toContainText(text('az', 'orders.statusNew'));
  });

  test('one click only opens the status list, nothing is changed', async ({ page }) => {
    const patches = await openOrdersWithOneOrder(page);

    await page.getByTestId('order-status').click();
    await expect(page.getByRole('listbox')).toBeVisible();
    // Click outside: the list closes, the status stays
    await page.getByText(text('az', 'orders.title'), { exact: true }).click();
    await expect(page.getByRole('listbox')).toHaveCount(0);
    await expect(page.getByTestId('order-status')).toContainText(text('az', 'orders.statusNew'));

    // Esc closes it too
    await page.getByTestId('order-status').click();
    await page.keyboard.press('Escape');
    await expect(page.getByRole('listbox')).toHaveCount(0);

    expect(patches).toEqual([]);
  });

  test('the status changes only on choosing an option', async ({ page }) => {
    const patches = await openOrdersWithOneOrder(page);

    await page.getByTestId('order-status').click();
    await page.getByTestId('order-status-CONFIRMED').click();

    await expect(page.getByRole('listbox')).toHaveCount(0);
    await expect(page.getByTestId('order-status')).toContainText(text('az', 'orders.statusConfirmed'));
    expect(patches).toEqual(['CONFIRMED']);
  });
});

test.describe('Final statuses ask to confirm', () => {
  for (const [status, label] of [['COMPLETED', 'orders.statusCompleted'], ['CANCELLED', 'orders.statusCancelled']] as const) {
    test(`${status}: Cancel keeps the status, Yes sets it`, async ({ page }) => {
      const patches = await openOrdersWithOneOrder(page);

      await page.getByTestId('order-status').click();
      await page.getByTestId(`order-status-${status}`).click();
      const confirm = page.getByTestId('order-status-confirm');
      await expect(confirm).toContainText(text('az', 'orders.confirmFinal', { id: 1, status: text('az', label) }));

      await confirm.getByRole('button', { name: text('az', 'common.cancel') }).click();
      await expect(confirm).toHaveCount(0);
      await expect(page.getByTestId('order-status')).toContainText(text('az', 'orders.statusNew'));
      expect(patches).toEqual([]);

      await page.getByTestId('order-status').click();
      await page.getByTestId(`order-status-${status}`).click();
      await page.getByTestId('order-status-confirm-yes').click();
      // A finished or cancelled order leaves the "Active" tab and is listed under "All"
      await expect(page.getByText(text('az', 'orders.noActive'))).toBeVisible();
      await page.getByRole('button', { name: text('az', 'orders.all', { count: 1 }) }).click();
      await expect(page.getByTestId('order-status')).toContainText(text('az', label));
      expect(patches).toEqual([status]);
    });
  }
});
