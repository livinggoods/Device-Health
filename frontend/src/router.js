import Vue from 'vue'
import Router from 'vue-router'
import LocationStatistics from '@/views/LocationStatistics.vue'
import DashboardConfig from '@/components/DashboardConfig.vue'

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
        }
    ]
})
