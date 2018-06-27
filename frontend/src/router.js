import Vue from 'vue'
import Router from 'vue-router'
import LocationStatistics from '@/views/LocationStatistics.vue'
import DashboardConfig from '@/components/DashboardConfig.vue'
import Login from '@/views/Login.vue'
import Error from '@/views/Error.vue'

Vue.use(Router)

export default new Router({

    routes: [
        {
            path: '/',
            name: 'locationTracker',
            component: LocationStatistics,
            // meta: { requiresAuth: true }
        },
        {
            path: '/config',
            component: DashboardConfig,
            meta: { requiresAuth: true }
        },
        {
            path: '/login',
            component: Login
        },
        {
            path: '/error',
            component: Error
        },
        {
            path: '',
            redirect: '/error'
        },
        {
            path: '*',
            redirect: '/error'
        }
    ]
})
