import './assets/css/theme/index.css'
Vue.config.productionTip = false
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import LocationTracker from './components/LocationTracker.vue'
import Vue from 'vue/dist/vue'
import VueRouter from 'vue-router'

const routes=[
    {
        path:'/',
        component: LocationTracker
    }
]
const router= new VueRouter({
        routes
})
Vue.use(ElementUI, {locale})
Vue.use(VueRouter)



new Vue({
    router
}).$mount('#app')


