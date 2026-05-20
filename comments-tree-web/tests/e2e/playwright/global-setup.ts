import { request } from '@playwright/test';

const BACKEND_URL = process.env.COMMENTS_TREE_API_URL ?? 'http://127.0.0.1:8081';
const FRONTEND_URL = process.env.COMMENTS_TREE_UI_URL ?? 'http://127.0.0.1:4173';

async function globalSetup() {
  await assertBackendReady();
  // Frontend is started by Playwright webServer (serve:e2e); optional early check is skipped here
  // because webServer has not run yet during globalSetup.
}

async function assertBackendReady() {
  const ctx = await request.newContext({ baseURL: BACKEND_URL });
  try {
    const res = await ctx.get('/actuator/health', { timeout: 5000 });
    if (!res.ok()) {
      throw new Error(`GET /actuator/health returned HTTP ${res.status()}`);
    }
    const body = await res.text();
    if (!body.includes('UP')) {
      throw new Error(`unexpected health response: ${body}`);
    }
  } catch (err) {
    const detail = err instanceof Error ? err.message : String(err);
    throw new Error(
      `Backend is not ready at ${BACKEND_URL} (${detail}). ` +
        'Run: task api'
    );
  } finally {
    await ctx.dispose();
  }
}

export default globalSetup;
