import axios from 'axios'
const generalConfig = {
    'colors': {'primary': '#005084'},
    'mapboxApiKey': 'pk.eyJ1IjoiY2F5dm93Y29yaXIiLCJhIjoiY2poOHNiMzI2MDY4YTNhcWh4YnB1cHhzNCJ9.vtlXL9l0BgCri4iCJG0YqA',
    'title': 'Living Goods - DeviceHealth Dashboard',
    'sideBarHeader': 'DeviceHealth Dashboard',
    'siteModules': [
        // 'locationStatistics',
        'batteryStatistics',
        'dataUsageStatistics'
    ]
}

console.log(window.localStorage.getItem('auth-token'))
const api = axios.create({
    baseURL: process.env.NODE_ENV === 'production' ? 'https://device-health.lg-apps.com/api' : 'http://localhost:8085/api'
})

export {api, generalConfig}
