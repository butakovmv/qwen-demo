import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', name: 'Home', component: Home }],
})

describe('App.vue', () => {
  it('renders navigation with two links', async () => {
    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] },
    })

    const links = wrapper.findAll('.nav a')
    expect(links.length).toBe(2)
    expect(links[0].text()).toBe('Вопросы')
    expect(links[1].text()).toBe('Естественный язык')
  })

  it('renders Home view by default', async () => {
    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] },
    })

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

    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] },
    })

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

    router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [router] },
    })

    await wrapper.find('button').trigger('click')
    await flushPromises()

    expect(wrapper.find('.error p').text()).toBe('HTTP ошибка: 500')
    expect(wrapper.find('.questions-list').exists()).toBe(false)
  })
})
