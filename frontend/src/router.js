import Vue from 'vue'
import Router from 'vue-router'
import LocationStatistics from '@/views/LocationStatistics.vue'
import DashboardConfig from '@/components/DashboardConfig.vue'
import Login from '@/views/Login.vue'
import PasswordReset from '@/views/PasswordReset.vue'
import PasswordResetVerification from '@/views/PasswordResetVerification.vue'
import NewAcc from '@/views/CreateAcc.vue'
import Error from '@/views/Error.vue'

Vue.use(Router)

export default new Router({
    mode: 'history',
    routes: [
        {
            path: '/',
            name: 'locationTracker',
            component: LocationStatistics,
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
            component: NewAcc
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
