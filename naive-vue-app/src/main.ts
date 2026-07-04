import './style.css'
import { createApp } from 'vue'
import naive from 'naive-ui'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './app/router'

createApp(App)
  .use(createPinia())
  .use(router)
  .use(naive)
  .mount('#app')
