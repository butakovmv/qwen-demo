import { defineConfig } from '@playwright/test'

const BASE_URL = process.env.BASE_URL || 'http://localhost:3000'
const CI = process.env.CI === '1'

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!CI,
  retries: CI ? 1 : 0,
  workers: 1,
  reporter: [['html', { outputFolder: 'build/reports/ui-test' }], ['list']],
  use: {
    baseURL: BASE_URL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: CI
    ? [
        {
          name: 'chromium',
          use: {
            launchOptions: {
              executablePath: '/usr/lib/chromium/chromium',
              args: ['--no-sandbox', '--disable-setuid-sandbox'],
            },
          },
        },
      ]
    : [
        {
          name: 'chromium',
          use: { channel: 'chromium' },
        },
      ],
})
