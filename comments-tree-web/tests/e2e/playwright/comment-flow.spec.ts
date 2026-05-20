import { test, expect, request } from '@playwright/test';
import { gotoHome, loginUser } from './helpers';

const BACKEND_URL = process.env.COMMENTS_TREE_API_URL ?? 'http://localhost:8081';

/**
 * Helper: register a new user directly via the backend API.
 * Bypasses the UI so that beforeAll setup is fast and reliable.
 */
async function registerUserViaApi(username: string, email: string, password: string) {
  const ctx = await request.newContext({ baseURL: BACKEND_URL });
  const res = await ctx.post('/user', {
    data: { name: username, email, password },
  });
  await ctx.dispose();
  return res.ok();
}

test.describe('Comment flow', () => {
  const timestamp = Date.now();
  const username = `testuser_${timestamp}`;
  const email = `testuser_${timestamp}@example.com`;
  const password = 'Test@1234';

  test.beforeAll(async () => {
    const ok = await registerUserViaApi(username, email, password);
    if (!ok) {
      throw new Error(`Failed to register test user: ${username}`);
    }
  });

  test.beforeEach(async ({ context }) => {
    await context.clearCookies();
  });

  test('unauthenticated user sees the comment list', async ({ page }) => {
    await gotoHome(page);
    await expect(page.locator('[data-cy=register-dialog]')).toBeVisible();
    await expect(page.locator('[data-cy=comment-new]')).not.toBeVisible();
  });

  test('user can log in and the logout button appears', async ({ page }) => {
    await gotoHome(page);
    await loginUser(page, username, password);
    await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible();
  });

  test('logged-in user can post a root comment', async ({ page }) => {
    await gotoHome(page);
    await loginUser(page, username, password);

    await page.click('[data-cy=comment-new]');
    const commentText = `Playwright test comment ${timestamp}`;
    await page.fill('[data-cy=comment-text]', commentText);
    await page.click('[data-cy=comment-save]');

    await expect(page.locator('[data-cy=comment-new]')).toBeVisible({ timeout: 5000 });
    await expect(page.getByText(commentText)).toBeVisible({ timeout: 5000 });
  });

  test('logged-in user can reply to a comment', async ({ page }) => {
    await gotoHome(page);
    await loginUser(page, username, password);

    await page.click('[data-cy=comment-new]');
    const rootText = `Root comment for reply test ${timestamp}`;
    await page.fill('[data-cy=comment-text]', rootText);
    await page.click('[data-cy=comment-save]');
    await expect(page.getByText(rootText)).toBeVisible({ timeout: 5000 });

    await page.locator('[data-cy=reply-btn]').first().click();
    const replyText = `Reply to root comment ${timestamp}`;
    await page.fill('[data-cy=comment-text]', replyText);
    await page.click('[data-cy=comment-save]');

    await expect(page.locator('[data-cy=comment-save]')).not.toBeVisible({ timeout: 5000 });

    await page.reload({ waitUntil: 'networkidle' });
    await expect(page.getByText(rootText)).toBeVisible({ timeout: 8000 });
    await expect(page.getByText(replyText)).toBeVisible({ timeout: 5000 });
  });

  test('user can log out', async ({ page }) => {
    await gotoHome(page);
    await loginUser(page, username, password);
    await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible();

    await page.click('[data-cy=logout-dialog]');
    await expect(page.locator('[data-cy=login-dialog]')).toBeVisible({ timeout: 5000 });
  });
});
