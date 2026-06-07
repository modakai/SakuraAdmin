import { defineStore } from 'pinia'

type ColorMode = 'light' | 'dark'
type Density = 'comfortable' | 'compact'

export const useAppearanceStore = defineStore('appearance', {
  state: () => ({
    colorMode: (localStorage.getItem('naive-admin-color-mode') as ColorMode) || 'light',
    density: (localStorage.getItem('naive-admin-density') as Density) || 'comfortable',
  }),
  actions: {
    // 外观偏好只保存在本地，和真实后端配置解耦。
    toggleColorMode() {
      this.colorMode = this.colorMode === 'light' ? 'dark' : 'light'
      localStorage.setItem('naive-admin-color-mode', this.colorMode)
    },
    setDensity(density: Density) {
      this.density = density
      localStorage.setItem('naive-admin-density', density)
    },
  },
})
