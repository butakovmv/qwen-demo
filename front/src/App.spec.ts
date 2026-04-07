import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import App from '../src/App.vue'

describe('App.vue', () => {
  it('renders heading and button', () => {
    const wrapper = mount(App)

    expect(wrapper.find('h1').text()).toBe('OTUS Application')
    expect(wrapper.find('button').text()).toBe('Получить приветствие')
    expect(wrapper.find('button').attributes('disabled')).toBeUndefined()
  })

  it('shows loading state when button is clicked', async () => {
    let resolveFetch: () => void = () => {}
    const promise = new Promise<unknown>((resolve) => {
      resolveFetch = () =>
        resolve({
          ok: true,
          json: () => Promise.resolve({ message: '' }),
        })
    })
    globalThis.fetch = vi.fn().mockReturnValue(promise)

    const wrapper = mount(App)
    await wrapper.find('button').trigger('click')

    expect(wrapper.find('button').text()).toBe('Загрузка...')
    expect(wrapper.find('button').attributes('disabled')).toBe('')

    // Cleanup
    resolveFetch()
    await flushPromises()
  })

  it('displays message after successful fetch', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ message: 'Hello, World!' }),
    })

    const wrapper = mount(App)
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.find('.message p').text()).toBe('Hello, World!')
    expect(wrapper.find('.error').exists()).toBe(false)
  })

  it('displays error on failed fetch', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
    })

    const wrapper = mount(App)
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error p').text()).toBe('HTTP ошибка: 500')
    expect(wrapper.find('.message').exists()).toBe(false)
  })
})
