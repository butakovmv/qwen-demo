import { test, expect } from '@playwright/test'

const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080'
const managementUrl = process.env.MANAGEMENT_URL || 'http://localhost:8081'
const adminUser = process.env.ADMIN_USER || 'test'
const adminPassword = process.env.ADMIN_PASSWORD || 'test'

const authHeaders = {
  Authorization: `Basic ${Buffer.from(`${adminUser}:${adminPassword}`).toString('base64')}`,
}

test.describe('API Smoke Tests', () => {
  test('health endpoint returns 200', async ({ request }) => {
    const response = await request.get(`${managementUrl}/actuator/health`)
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
  })

  test('info endpoint returns 200', async ({ request }) => {
    const response = await request.get(`${managementUrl}/actuator/info`, { headers: authHeaders })
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
  })

  test('metrics endpoint returns 200', async ({ request }) => {
    const response = await request.get(`${managementUrl}/actuator/metrics`, { headers: authHeaders })
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
  })

  test('prometheus endpoint returns 200', async ({ request }) => {
    const response = await request.get(`${managementUrl}/actuator/prometheus`, { headers: authHeaders })
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
  })

  test('swagger-ui.html returns 200', async ({ request }) => {
    const response = await request.get(`${backendUrl}/swagger-ui.html`, { headers: authHeaders })
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
  })

  test('openapi docs endpoint returns 200', async ({ request }) => {
    const response = await request.get(`${backendUrl}/v3/api-docs`)
    expect(response.ok()).toBeTruthy()
    expect(response.status()).toBe(200)
    const body = await response.json()
    expect(body.info).toBeDefined()
    expect(body.info.title).toBeDefined()
  })
})
