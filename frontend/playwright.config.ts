import { defineConfig, devices } from '@playwright/test';

// End-to-end tests of the merchant site. Runs against an already started site:
//   npx playwright test                                         # http://localhost:5173 (npm run dev)
//   E2E_URL=https://smartdirect.qrfood.az npx playwright test   # another address
export default defineConfig({
  testDir: 'e2e',
  use: {
    baseURL: process.env.E2E_URL || 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
