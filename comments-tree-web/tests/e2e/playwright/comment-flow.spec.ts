import { test, expect, request } from '@playwright/test';

const BACKEND_URL = 'http://localhost:8081';

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

/**
 * Helper: log in via the UI.
 * Waits for the logout button to appear as a positive confirmation of success.
 */
async function loginUser(page: any, nameOrEmail: string, password: string) {
  await page.click('[data-cy=login-dialog]');
  // Wait for the login form to be visible before interacting
  await page.waitForSelector('[data-cy=user-name]', { state: 'visible' });
  await page.fill('[data-cy=user-name]', nameOrEmail);
  await page.fill('[data-cy=password]', password);
  await page.click('[data-cy=login]');
  // Positive assertion: wait for the logout button to appear
  await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible({ timeout: 8000 });
}

test.describe('Comment flow', () => {
  // Generate a unique user per test run so the suite is self-contained
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

  test('unauthenticated user sees the comment list', async ({ page }) => {
    await page.goto('/');
    // The login and register buttons should be visible
    await expect(page.locator('[data-cy=login-dialog]')).toBeVisible();
    await expect(page.locator('[data-cy=register-dialog]')).toBeVisible();
    // The "new comment" button should NOT be visible when logged out
    await expect(page.locator('[data-cy=comment-new]')).not.toBeVisible();
  });

  test('user can log in and the logout button appears', async ({ page }) => {
    await page.goto('/');
    await loginUser(page, username, password);
    await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible();
  });

  test('logged-in user can post a root comment', async ({ page }) => {
    await page.goto('/');
    await loginUser(page, username, password);

    await page.click('[data-cy=comment-new]');
    const commentText = `Playwright test comment ${timestamp}`;
    await page.fill('[data-cy=comment-text]', commentText);
    await page.click('[data-cy=comment-save]');

    // After saving, the dialog should close and the comment should appear in the list
    await expect(page.locator('[data-cy=comment-new]')).toBeVisible({ timeout: 5000 });
    await expect(page.getByText(commentText)).toBeVisible({ timeout: 5000 });
  });

  test('logged-in user can reply to a comment', async ({ page }) => {
    await page.goto('/');
    await loginUser(page, username, password);

    // Post root comment first
    await page.click('[data-cy=comment-new]');
    const rootText = `Root comment for reply test ${timestamp}`;
    await page.fill('[data-cy=comment-text]', rootText);
    await page.click('[data-cy=comment-save]');
    await expect(page.getByText(rootText)).toBeVisible({ timeout: 5000 });

    // Reply to the comment we just posted (newest = first in the list)
    await page.locator('[data-cy=reply-btn]').first().click();
    const replyText = `Reply to root comment ${timestamp}`;
    await page.fill('[data-cy=comment-text]', replyText);
    await page.click('[data-cy=comment-save]');

    // Wait for dialog to close (save completed)
    await expect(page.locator('[data-cy=comment-save]')).not.toBeVisible({ timeout: 5000 });

    // Reload so v-treeview open-all re-expands all nodes (including the new reply)
    await page.reload();
    await expect(page.getByText(rootText)).toBeVisible({ timeout: 8000 });
    await expect(page.getByText(replyText)).toBeVisible({ timeout: 5000 });
  });

  test('user can log out', async ({ page }) => {
    await page.goto('/');
    await loginUser(page, username, password);
    await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible();

    await page.click('[data-cy=logout-dialog]');
    await expect(page.locator('[data-cy=login-dialog]')).toBeVisible({ timeout: 5000 });
  });
});
