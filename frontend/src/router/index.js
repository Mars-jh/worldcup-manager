import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * 路由配置
 * - /login, /register 公开访问
 * - 其余页面需要认证
 * - 根据用户角色动态显示菜单（在 Layout 组件中处理）
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表板', icon: 'DataAnalysis' }
      },
      {
        path: 'teams',
        name: 'Teams',
        component: () => import('@/views/TeamManager.vue'),
        meta: { title: '球队管理', icon: 'Flag' }
      },
      {
        path: 'players',
        name: 'Players',
        component: () => import('@/views/PlayerManager.vue'),
        meta: { title: '球员管理', icon: 'User' }
      },
      {
        path: 'groups',
        name: 'Groups',
        component: () => import('@/views/GroupStage.vue'),
        meta: { title: '小组赛', icon: 'Grid' }
      },
      {
        path: 'knockout',
        name: 'Knockout',
        component: () => import('@/views/Knockout.vue'),
        meta: { title: '淘汰赛', icon: 'Trophy' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/Schedule.vue'),
        meta: { title: '赛程日历', icon: 'Calendar' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserManager.vue'),
        meta: { title: '用户管理', icon: 'UserFilled', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 未登录跳转登录页
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    next() // 公开页面
  } else if (!authStore.token) {
    next('/login') // 未登录
  } else {
    next() // 已登录
  }
})

export default router