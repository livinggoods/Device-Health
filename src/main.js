import './assets/css/theme/index.css'
Vue.config.productionTip = false
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import App from './App.vue'
import Vue from 'vue'

Vue.use(ElementUI, {locale})

new Vue({
  render: h => h(App)
}).$mount('#app')
