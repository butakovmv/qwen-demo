import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
    },
    {
      path: '/naturalLang',
      name: 'NaturalLang',
      component: () => import('../views/NaturalLang.vue'),
    },
  ],
})

export default router
