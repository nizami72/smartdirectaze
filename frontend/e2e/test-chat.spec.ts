import { test, expect } from './fixtures';
import { addProduct, createShopAndOpenDashboard } from './support/merchant';
import { text } from './support/texts';

// Talks to the real AI model (DeepSeek): needs DEEPSEEK_API_KEY on the backend, costs a few cents,
// answers in 5-60 s. Tagged @ai:
//   npm run e2e:no-ai      # everything except these tests
//   npm run e2e:ai         # only these
//   npm run e2e:ai:watch   # only these, visible, 0.5 s pause
// E2E_SKIP_AI=1 skips them in any run.

const AI_ANSWER_TIMEOUT = 90_000;

test.describe('Test chat "Check your seller"', { tag: '@ai' }, () => {
  test.skip(process.env.E2E_SKIP_AI === '1', 'AI tests are switched off (E2E_SKIP_AI=1)');
  test.setTimeout(240_000);

  test('the AI answers about the price of a product and about delivery', async ({ merchantPage: page }) => {
    // A shop with one product; delivery costs 3 AZN (set by createShopAndOpenDashboard)
    await createShopAndOpenDashboard(page);
    await addProduct(page, 'Qara dəri çanta', '45');

    const chat = page.getByTestId('test-chat');
    await chat.scrollIntoViewIfNeeded();
    await expect(chat).toContainText(text('az', 'testChat.title'));

    // 1. Price of the product
    const priceAnswer = await ask(page, 'Salam! Qara dəri çanta neçəyədir?');
    expect(priceAnswer, `AI answer: ${priceAnswer}`).toMatch(/45|çanta/i);

    // 2. Delivery, in the same conversation
    const deliveryAnswer = await ask(page, 'Çatdırılma neçəyədir?');
    expect(deliveryAnswer, `AI answer: ${deliveryAnswer}`).toMatch(/3|çatdırılma|pulsuz/i);
  });
});

/** Sends a customer message in the test chat and returns the AI answer that follows it */
async function ask(page: import('@playwright/test').Page, question: string) {
  const chat = page.getByTestId('test-chat');
  const answersBefore = await chat.locator('[data-testid="chat-message"][data-from="ai"]').count();

  await chat.getByTestId('chat-input').fill(question);
  await chat.getByTestId('chat-send').click();
  await expect(chat.locator('[data-testid="chat-message"][data-from="customer"]').last()).toHaveText(question);

  // The AI is thinking while the typing indicator is shown; an error bubble would mean no answer
  await expect(chat.getByTestId('chat-typing')).toHaveCount(0, { timeout: AI_ANSWER_TIMEOUT });
  await expect(chat.locator('[data-testid="chat-message"][data-from="error"]')).toHaveCount(0);
  const answers = chat.locator('[data-testid="chat-message"][data-from="ai"]');
  await expect(answers).toHaveCount(answersBefore + 1);

  const answer = (await answers.last().innerText()).trim();
  expect(answer.length, 'the AI answer is empty').toBeGreaterThan(0);
  // Shown in the run output, so a person sees what the AI actually said
  console.log(`\n  customer: ${question}\n  AI:       ${answer.replace(/\n/g, '\n            ')}\n`);
  return answer;
}
