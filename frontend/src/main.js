import Vue from 'vue'
import router from './router'
import './assets/scss/element-variables.scss'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import {generalConfig, api} from '../config/config'
import './registerServiceWorker'
import App from './App'
import 'vue-loading-overlay/dist/vue-loading.min.css'

Vue.config.productionTip = false

//  Router guard to check whether route is allowed
router.beforeEach((to, from, next) => {
    // Check Auth
    if (to.meta.requiresAuth) {
        if (localStorage.getItem('auth-token') !== 'null') {
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

var app = new Vue({
    router,
    render: h => h(App),
    methods: {
        observeAuthChange: function () {
            // listen for changes in localstorage
            var originalSetItemFn = localStorage.setItem

            localStorage.setItem = function () {
                var event = new CustomEvent('localStorageChange')
                document.dispatchEvent(event)

                originalSetItemFn.apply(this, arguments)
            }
            document.addEventListener('localStorageChange', this.storageHandler, false)
        },
        storageHandler: function (e) {
            if (localStorage.getItem('auth-token') === 'null') {
                this.$router.push({'path': '/login'})
            }
        }
    },
    created: function () {
        this.observeAuthChange()
    }
}).$mount('#app')

// Intercept Errors and redirect to Login
var errorCodes = [401, 403]
api.interceptors.response.use((response) => {
    return response
}, function (error) {
    if (errorCodes.includes(error.response.status)) {
        localStorage.setItem('auth-token', 'null')
        app.$router.push({'path': '/login'})
    }
})
