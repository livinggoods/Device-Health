import Vue from 'vue'
import Router from 'vue-router'
import LocationStatistics from '@/views/LocationStatistics.vue'
import DashboardConfig from '@/components/DashboardConfig.vue'
import Login from '@/views/Login.vue'
import PasswordReset from '@/views/PasswordReset.vue'
import PasswordResetVerification from '@/views/PasswordResetVerification.vue'
import DataUsageStatistics from '@/views/DataUsageStatistics.vue'
import NewAcc from '@/views/CreateAcc.vue'
import Error from '@/views/Error.vue'
import BatteryStatistics from '@/views/BatteryStatistics.vue'

Vue.use(Router)

export default new Router({
    mode: 'history',
    routes: [
        {
            path: '/',
            name: 'locationStatistics',
            component: LocationStatistics,
            meta: { requiresAuth: true }
        },
        {
            path: '/battery-statistics',
            name: 'batteryStatistics',
            component: BatteryStatistics,
            meta: { requiresAuth: true }
        },
        {
            path: '/data-usage-statistics',
            name: 'dataUsageStatistics',
            component: DataUsageStatistics,
            meta: { requiresAuth: true }
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
            path: '/make/new/admin/account/now',
            component: NewAcc,
            meta: { requiresAuth: true }
        },
        {
            path: '/password/reset',
            component: PasswordReset
        },
        {
            path: '/password/reset/verify',
            component: PasswordResetVerification
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
