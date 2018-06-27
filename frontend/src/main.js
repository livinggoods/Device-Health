import Vue from 'vue'
import router from './router'
import './assets/scss/element-variables.scss'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import {generalConfig} from '../config/config'
import './registerServiceWorker'
import App from './App'
import 'vue-loading-overlay/dist/vue-loading.min.css'

Vue.config.productionTip = false

//  Router guard to check whether route is allowed
router.beforeEach((to, from, next) => {
    // Check Auth
    if (to.meta.requiresAuth) {
        if (localStorage.getItem('auth-token')) {
            // if (generalConfig.siteModules.includes(from.name) === false) {
            //     next()
            // } else if (!to.matched[0].length) {
            //     next('/error')
            // } else {
            //     next()
            // }
            next()
        } else next('/login')
    } else {
        next()
    }
})

Vue.use(ElementUI, {locale})

new Vue({
    router,
    render: h => h(App)
}).$mount('#app')
