import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import locale from 'element-plus/dist/locale/zh-cn'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import '@/theme/index.scss'

const app = createApp(App)
app.use(router)
app.use(createPinia())
app.use(ElementPlus, { locale })
for (const [key, component] of Object.entries(ElementPlusIcons)) {
  app.component(key, component)
}
app.mount('#app')
