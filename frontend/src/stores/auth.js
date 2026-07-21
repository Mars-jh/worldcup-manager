import { defineStore } from 'pinia'
import request from '@/utils/request'

/**
 * 认证 Store - 管理用户登录状态
 * Token 和用户信息持久化到 localStorage
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isOperator: (state) => state.user?.role === 'OPERATOR' || state.user?.role === 'ADMIN',
    username: (state) => state.user?.username || '',
    role: (state) => state.user?.role || ''
  },
  actions: {
    /** 登录 */
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token
      this.user = { username: res.data.username, role: res.data.role, userId: res.data.userId }
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return res
    },

    /** 注册 */
    async register(username, password, email) {
      const res = await request.post('/auth/register', { username, password, email })
      this.token = res.data.token
      this.user = { username: res.data.username, role: res.data.role, userId: res.data.userId }
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return res
    },

    /** 退出登录 */
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})