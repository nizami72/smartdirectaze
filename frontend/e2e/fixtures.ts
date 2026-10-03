import { test as base, type Page } from '@playwright/test';
import { registerMerchant, type Merchant } from './support/merchant';

// Tests import { test, expect } from './fixtures' and get a merchant who is already registered and logged in:
//   test('...', async ({ merchantPage, merchant }) => { ... });
export const test = base.extend<{ merchantPage: Page; merchant: Merchant }>({
  merchant: async ({ page }, use) => {
    const { merchant } = await registerMerchant(page);
    await use(merchant);
  },
  merchantPage: async ({ page, merchant }, use) => {
    void merchant; // registered and logged in by the merchant fixture
    await use(page);
  },
});

export { expect } from '@playwright/test';
