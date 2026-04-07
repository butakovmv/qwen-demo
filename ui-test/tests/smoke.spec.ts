import { test, expect } from '@playwright/test'

test.describe('Smoke test', () => {
  test('страница загружается, при клике на кнопку появляется сообщение', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/OTUS/)
    await expect(page.getByRole('heading', { name: 'OTUS Application' })).toBeVisible()

    await page.getByRole('button', { name: 'Получить приветствие' }).click()

    await expect(page.locator('.message p')).toHaveText(/Hello, World!/, { timeout: 15000 })
  })
})
