import { defineStore } from 'pinia'

export const useSessionStore = defineStore('session', {
  state: () => ({
    isAuthenticated: localStorage.getItem('naive-admin-token') === 'mock-token',
    user: {
      name: 'Sakura Admin',
      email: 'admin@example.com',
      role: '超级管理员',
    },
  }),
  actions: {
    // mock 登录只负责进入后台，不请求真实接口。
    login() {
      localStorage.setItem('naive-admin-token', 'mock-token')
      this.isAuthenticated = true
    },
    logout() {
      localStorage.removeItem('naive-admin-token')
      this.isAuthenticated = false
    },
  },
})
