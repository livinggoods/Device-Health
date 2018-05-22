import './assets/scss/element-variables.scss'
Vue.config.productionTip = false
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import LocationTracker from './components/LocationTracker.vue'
import DashboardConfig from './components/DashboardConfig.vue'
import Vue from 'vue/dist/vue'
import VueRouter from 'vue-router'
import Config from '../config/config'

const routes=[
    {
        path:'/',
        name:'locationStatistics',
        component: LocationTracker


    },
    {
        path:'/config',
        component: DashboardConfig
    },




]
const router= new VueRouter({
        routes
})
//Router guard to check whether route is allowed
router.beforeEach((to, from, next)=>{
    if(Config.siteModules.includes(from.name)==false){
        next()
    }
    else next()
})
Vue.use(ElementUI, {locale})
Vue.use(VueRouter)



new Vue({
    router
}).$mount('#app')


