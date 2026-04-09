import { test, expect } from '@playwright/test'

test.describe('Smoke test', () => {
  test('страница загружается и есть форма с вопросами', async ({ page }) => {
    await page.goto('/')
    await expect(page).toHaveTitle(/OTUS/)
    await expect(page.getByRole('heading', { name: 'OTUS Application' })).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Вопросы' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Загрузить вопросы' })).toBeVisible()
  })
})
