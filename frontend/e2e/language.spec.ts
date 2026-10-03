import { test, expect } from './fixtures';
import { chooseLanguage, login, logout } from './support/merchant';
import { text } from './support/texts';

test.describe('Interface language AZ / RU', () => {
  test('Azerbaijani by default; the choice survives a reload', async ({ page }) => {
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: text('az', 'login.title') })).toBeVisible();

    await chooseLanguage(page, 'ru');
    await expect(page.getByRole('heading', { name: text('ru', 'login.title') })).toBeVisible();
    await expect(page.getByTestId('login-submit')).toHaveText(text('ru', 'common.login'));

    await page.reload();
    await expect(page.getByRole('heading', { name: text('ru', 'login.title') })).toBeVisible();
    await expect(page.getByTestId('lang-ru')).toHaveAttribute('aria-pressed', 'true');
  });

  test('the switch in the account bar changes the page and is kept in the profile', async ({ merchantPage: page, merchant, browser }) => {
    await expect(page.getByText(text('az', 'createShop.title'))).toBeVisible();

    await page.getByTestId('lang-ru').click();
    await expect(page.getByText(text('ru', 'createShop.title'))).toBeVisible();
    await expect(page.getByTestId('logout')).toContainText(text('ru', 'common.logout'));

    await page.reload();
    await expect(page.getByText(text('ru', 'createShop.title'))).toBeVisible();

    // Another browser with no saved choice: the language comes from the merchant's profile
    await logout(page);
    const other = await browser.newContext();
    const otherPage = await other.newPage();
    await login(otherPage, merchant, null);
    await expect(otherPage.getByText(text('ru', 'createShop.title'))).toBeVisible();
    await other.close();
  });
});
