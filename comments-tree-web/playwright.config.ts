import { defineConfig, devices } from '@playwright/test';

const E2E_UI_URL = process.env.COMMENTS_TREE_UI_URL ?? 'http://127.0.0.1:4173';

export default defineConfig({
  testDir: './tests/e2e/playwright',
  globalSetup: './tests/e2e/playwright/global-setup.ts',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: 'html',
  timeout: 60_000,
  expect: { timeout: 15_000 },

  // Production preview on 4173 — avoids Vite dev dep-optimizer races on 5173.
  webServer: {
    command: 'npm run serve:e2e',
    url: E2E_UI_URL,
    reuseExistingServer: false,
    timeout: 120_000,
  },

  use: {
    baseURL: E2E_UI_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
