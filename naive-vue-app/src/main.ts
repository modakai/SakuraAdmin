import './style.css'
import { createApp } from 'vue'
import naive from 'naive-ui'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './app/router'
import { permissionDirective } from './shared/directives/permission'

createApp(App)
  .use(createPinia())
  .use(router)
  .use(naive)
  .directive('permission', permissionDirective)
  .mount('#app')
