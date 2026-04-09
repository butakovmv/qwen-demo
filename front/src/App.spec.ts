import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import App from '../src/App.vue'

describe('App.vue', () => {
  it('renders heading and questions button', () => {
    const wrapper = mount(App)

    expect(wrapper.find('h1').text()).toBe('OTUS Application')
    expect(wrapper.find('h2').text()).toBe('Вопросы')
    expect(wrapper.find('button').text()).toBe('Загрузить вопросы')
  })

  it('shows questions after successful fetch', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          questions: [
            { id: 'q1', text: 'What is your name?' },
            { id: 'q2', text: 'What is your quest?' },
          ],
        }),
    })

    const wrapper = mount(App)
    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.find('.questions-list li').exists()).toBe(true)
    expect(wrapper.findAll('.questions-list li').length).toBe(2)
    expect(wrapper.findAll('.questions-list input').length).toBe(2)
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
    expect(wrapper.find('.questions-list').exists()).toBe(false)
  })
})
