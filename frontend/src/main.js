import Vue from 'vue'
import router from './router'
import './assets/scss/element-variables.scss'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import Config from '../config/config'
import './registerServiceWorker'
import App from './App'

Vue.config.productionTip = false


//  Router guard to check whether route is allowed
router.beforeEach((to, from, next) => {
    if (Config.siteModules.includes(from.name) == false) {
        next()
    }
    else next()
})
Vue.use(ElementUI, {locale})


new Vue({
    router,
    render: h => h(App)
}).$mount('#app')


