import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios 请求封装
 * - 自动携带 JWT Token
 * - 统一错误处理
 * - 401 自动跳转登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 注入 Token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一错误处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      // Token 过期或无效，同步清除所有登录状态
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // 动态导入 auth store 以避免循环依赖
      import('@/stores/auth').then(({ useAuthStore }) => {
        useAuthStore().logout()
      })
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足，无法执行此操作')
    } else {
      ElMessage.error(error.response?.data?.message || '网络请求失败')
    }
    return Promise.reject(error)
  }
)

export default request