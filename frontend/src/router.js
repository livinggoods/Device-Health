import Vue from 'vue'
import Router from 'vue-router'
import LocationStatistics from '@/views/LocationStatistics.vue'
import DashboardConfig from '@/components/DashboardConfig.vue'
import Error from '@/views/Error.vue'

Vue.use(Router)

export default new Router({

    routes: [
        {
            path: '/',
            name: 'locationTracker',
            component: LocationStatistics

        },
        {
            path: '/config',
            component: DashboardConfig
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
