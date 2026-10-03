import { defineConfig, devices } from '@playwright/test';

// End-to-end tests of the merchant site. Runs against an already started site:
//   npx playwright test                                         # http://localhost:5173 (npm run dev)
//   E2E_URL=https://smartdirect.qrfood.az npx playwright test   # another address
//   npm run e2e:watch                                           # visible browser, 0.5 s pause between actions
const slowMo = Number(process.env.E2E_SLOWMO || 0);

export default defineConfig({
  testDir: 'e2e',
  use: {
    baseURL: process.env.E2E_URL || 'http://localhost:5173',
    trace: 'on-first-retry',
    // Failed tests leave a screenshot of the moment they failed (test-results/, HTML report)
    screenshot: 'only-on-failure',
    // A pause before every action, so a person can follow a visible run
    launchOptions: { slowMo },
  },
  // Slowed down runs take longer than the default 30 s per test
  timeout: slowMo ? 30_000 + slowMo * 120 : 30_000,
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
