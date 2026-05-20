import { expect, Page } from '@playwright/test';

export async function gotoHome(page: Page) {
  await page.goto('/', { waitUntil: 'domcontentloaded' });
  // Wait for Vue app shell (Vite dev optimizer failures leave an empty #app).
  await expect(page.locator('[data-cy=login-dialog]')).toBeVisible({ timeout: 30_000 });
}

export async function loginUser(page: Page, nameOrEmail: string, password: string) {
  await page.click('[data-cy=login-dialog]');
  await page.waitForURL('**/login', { timeout: 10000 });
  await page.waitForSelector('[data-cy=user-name]', { state: 'visible', timeout: 10000 });
  await page.fill('[data-cy=user-name]', nameOrEmail);
  await page.fill('[data-cy=password]', password);
  await page.click('[data-cy=login]');
  await expect(page.locator('[data-cy=logout-dialog]')).toBeVisible({ timeout: 10000 });
}
