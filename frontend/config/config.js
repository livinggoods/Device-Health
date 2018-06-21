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

const api = axios.create({
    baseURL: 'https://device-health.lg-apps.com/api'
    // baseURL: 'http://localhost:8085/api'
})

export {api, generalConfig}
