<template>
    <div>
        <div class="wrapper">
            <div class="main-panel">
                <div class="topbar" style="position: static">
                    <nav class="navbar navbar-default navbar-fixed" style="position: fixed; z-index: 100; width:83%; ">
                        <div class="container-fluid">
                            <div class="navbar-header">
                                <button type="button" class="navbar-toggle" data-toggle="collapse"
                                        data-target="#navigation-example-2">
                                    <span class="sr-only">Toggle navigation</span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                    <span class="icon-bar"></span>
                                </button>

                            </div>
                            <div class="collapse navbar-collapse">
                                <ul class="nav navbar-nav navbar-left">
                                    <li style="margin-top: 10px;">
                                        <el-select v-model="searchParams.country" filterable default-first-option
                                                   placeholder="Country">
                                            <el-option
                                                    v-for="item in countryOptions"
                                                    :key="item.code"
                                                    :label="item.name"
                                                    :value="item.code">
                                            </el-option>
                                        </el-select>
                                    </li>

                                    <li style="margin-top: 10px;margin-left: 5px;">
                                        <el-autocomplete
                                                popper-class="my-autocomplete"
                                                class="inline-input"
                                                v-model="searchParams.chvName"
                                                placeholder="CHV Name"
                                                :trigger-on-focus="false"
                                                @select="selectChv"
                                                :fetch-suggestions="searchChv">
                                            <i
                                                    class="el-icon-edit el-input__icon"
                                                    slot="suffix">
                                            </i>
                                            <template slot-scope="{ item }" style="width: 50px">
                                                <div class="value"><strong></strong> {{ item.value }} <strong>
                                                    - </strong> {{ item.branch }}
                                                </div>
                                            </template>
                                        </el-autocomplete>
                                    </li>
                                    <li style="margin-left: 5px; margin-top: 10px;">
                                        <el-date-picker
                                                v-model="searchParams.dates[0]"
                                                type="date"
                                                placeholder="Start Date"
                                        >
                                        </el-date-picker>
                                    </li>
                                    <li style="margin-left: 5px; margin-top: 10px;">
                                        <el-date-picker
                                                v-model="searchParams.dates[1]"
                                                type="date"
                                                placeholder="End date"
                                        >
                                        </el-date-picker>
                                    </li>

                                    <li>
                                        <a href="javascript:" @click="getLocationStats">
                                            <i class="fa fa-search"></i>
                                            <p class="hidden-lg hidden-md">Search</p>
                                        </a>

                                    </li>

                                </ul>

                                <ul class="nav navbar-nav navbar-right">

                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                            <i class="fa fa-globe"></i>
                                            <b class="caret hidden-sm hidden-xs"></b>
                                            <span class="notification hidden-sm hidden-xs">0</span>
                                            <p class="hidden-lg hidden-md">
                                                5 Notifications
                                                <b class="caret"></b>
                                            </p>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#">Notification 1</a></li>
                                            <li><a href="#">Notification 2</a></li>
                                            <li><a href="#">Notification 3</a></li>
                                            <li><a href="#">Notification 4</a></li>
                                            <li><a href="#">Another notification</a></li>
                                        </ul>
                                    </li>

                                    <li>
                                        <el-dropdown @command="settingsHandler" style="font-size: 20px;margin-right: 30px; margin-top: 15px;">
                                            <a href="#" class="el-dropdown-link" >
                                                <i class="fa fa-cog"></i>
                                                <b class="caret hidden-sm hidden-xs"></b>
                                            </a>
                                            <el-dropdown-menu slot="dropdown">
                                                <el-dropdown-item command="logout">Logout</el-dropdown-item>
                                            </el-dropdown-menu>
                                        </el-dropdown>
                                    </li>
                                    <li class="separator hidden-lg hidden-md"></li>
                                </ul>
                            </div>
                        </div>
                    </nav>
                </div>
                <Sidebar :sidebar-header="sidebarHeader"></Sidebar>
                <el-card v-if="this.medicLayerSelected==true && this.deviceHealthLayerSelected==false" class="box-card" style="position: absolute; float: right;margin-right:
                0px;max-width: 400px; z-index: 9999;  top: 60px; right: 0;max-height: 200px;overflow-y: scroll;overflow-x: scroll;overflow: -moz-scrollbars-vertical;">
                    <h4  style="margin-bottom: 5px;">Unmapped Medic Activities :
                        {{medicActivitiesWithoutLocation.length}}</h4>
                    <div style="border-bottom: 1px gainsboro solid; margin-bottom:5px"
                         v-for="unmappedActivity in medicActivitiesWithoutLocation" class="text item">
                        <strong>Activity:</strong> {{unmappedActivity.activity}} <br/> <strong>Client :</strong>
                        {{unmappedActivity.client}}
                        <br/> <strong>Reported At:</strong> {{unmappedActivity.recordedAt}}
                        <br/> <strong>Activity Id:</strong> {{unmappedActivity.activityId}}
                    </div>
                </el-card>
                <el-card v-else-if="this.medicLayerSelected==false && this.deviceHealthLayerSelected==true" class="box-card" style="position: absolute; float: right;margin-right:
                0px;max-width: 400px; z-index: 9999;  top: 60px; right: 0;max-height: 200px;overflow-y: scroll;overflow-x: scroll;overflow: -moz-scrollbars-vertical;">
                    <h4 style="margin-bottom: 5px;">Unmapped DeviceHealth
                        Activities : {{unmatchedDeviceHealthActivities.length}}</h4>
                    <div style="border-bottom: 1px gainsboro solid; margin-bottom:5px"
                         v-for="unmappedActivity in unmatchedDeviceHealthActivities" class="text item">
                        <strong>Activity:</strong> {{unmappedActivity.activity}} <br/> <strong>Client :</strong>
                        {{unmappedActivity.client}}
                        <br/> <strong>Reported At:</strong> {{unmappedActivity.recordedAt}}
                        <br/> <strong>Activity Id:</strong> {{unmappedActivity.activityId}}
                    </div>
                </el-card>
                <el-card v-else-if="this.medicLayerSelected==true && this.deviceHealthLayerSelected==true" class="box-card" style="position: absolute; float: right;margin-right:
                0px;max-width: 400px; z-index: 9999;  top: 60px; right: 0;max-height: 200px;overflow-y: scroll;overflow-x: scroll;overflow: -moz-scrollbars-vertical;">
                    <h4 style="margin-bottom: 5px;">All unmapped Activities : {{unmappedActivities.length}}</h4>
                    <div style="border-bottom: 1px gainsboro solid; margin-bottom:5px"
                         v-for="unmappedActivity in unmappedActivities" class="text item">
                        <strong>Activity:</strong> {{unmappedActivity.activity}} <br/> <strong>Client :</strong>
                        {{unmappedActivity.client}}
                        <br/> <strong>Reported At:</strong> {{unmappedActivity.recordedAt}}
                        <br/> <strong>Activity Id:</strong> {{unmappedActivity.activityId}}
                    </div>
                </el-card>
                <div id="map"></div>
                <div style="position: absolute;margin-bottom: 20px;z-index: 1000; bottom:30px; right: 0px"
                     id="menu" class="btn-group" role="group" aria-label="">
                    <button @click="toggleMedicData" :class="medicLayerSelected?'':'btn-neutral'" type="button"
                            class="btn btn-fill btn-secondary">Medic Data
                    </button>
                    <button @click="toggleDeviceHealthData" :class="deviceHealthLayerSelected?'':'btn-neutral'"
                            type="button"
                            class="btn btn-fill btn-secondary">DeviceHealth Data
                    </button>
                </div>

                <!--<lg-map-viz :access-token="accessToken" :layers="layers" :map-options="mapOptions"></lg-map-viz>-->
            </div>
        </div>
        <loading :active.sync="isLoading" :can-cancel="false"></loading>

    </div>
</template>

<script>
import 'bootstrap'
import {generalConfig, api} from '../../config/config'
import Sidebar from '@/components/Sidebar'
import LgMapViz from 'lg-map-viz'
import moment from 'moment'
import Loading from 'vue-loading-overlay'
import mapboxgl from 'mapbox-gl'
import toastr from 'toastr'

var map

export default {
    name: 'LocationTracker',
    data () {
        return {
            'authToken': window.localStorage.getItem('auth-token'),
            'countryOptions': [
                {'name': 'Uganda', 'code': 'UG'},
                {'name': 'Kenya', 'code': 'KE'},
                {'name': 'USA', 'code': 'USA'},
                {'name': 'Sierra Leonne', 'code': 'SE'}
            ],
            'isLoading': false,
            'accessToken': generalConfig.mapboxApiKey,
            'mapOptions': {
                style: 'mapbox://styles/mapbox/streets-v10',
                center: [37, 0],
                zoom: 5
            },
            'sidebarHeader': generalConfig.sidebarHeader,
            'searchParams': {
                'country': 'UG',
                'uuid': '',
                'chvId': '',
                'dates': []
            },
            'layers': '',
            'medicActivitiesWithLocation': [], // Activities that have coordinates collected by Medic
            'medicActivitiesWithoutLocation': [], // Activities that don't have coordinates collected by Medic
            'matchedDeviceHealthActivities': [], // Activities that have matched Device Health coordinates
            'unmatchedDeviceHealthActivities': [], // Activities that have not matched Device Health coordinates
            'unmappedActivities': [],
            'medicLayerSelected': false,
            'deviceHealthLayerSelected': false

        }
    },
    props: {},
    components: {
        Sidebar,
        LgMapViz,
        Loading
    },
    methods: {
        searchChv: function (username, callback) {
            api.post('/user/find', {
                'username': username,
                'country': this.searchParams.country
            }).then(function (response) {
                var errorResult = [{'value': 'No user found'}]
                if (response.data.status === true) {
                    var users = []
                    if (response.data.data.users.length > 0) {
                        response.data.data.users.forEach(function (user) {
                            users.push({'value': user.name, 'branch': user.branch, 'userObject': user})
                            callback(users)
                        })
                    } else {
                        callback(errorResult)
                    }
                } else {
                    callback(errorResult)
                }
            }).catch(function (error) {
                var errorResult = [{'value': 'No user found'}]
                callback(errorResult)
                console.log(error)
            })
        },
        reinitializeMap: function () {
            if (map.getLayer('device-health-data-layer')) {
                map.removeLayer('device-health-data-layer')
            }
            if (map.getSource('device-health-data-source')) {
                map.removeSource('device-health-data-source')
            }
            if (map.getLayer('medic-data-layer')) {
                map.removeLayer('medic-data-layer')
            }
            if (map.getSource('medic-data-source')) {
                map.removeSource('medic-data-source')
            }
            if (map.getLayer('device-health-line-layer')) {
                map.removeLayer('device-health-line-layer')
            }
            if (map.getSource('device-health-line-source')) {
                map.removeSource('device-health-line-source')
            }
            if (map.getLayer('medic-line-layer')) {
                map.removeLayer('medic-line-layer')
            }
            if (map.getSource('medic-line-source')) {
                map.removeSource('medic-line-source')
            }
            this.deviceHealthLayerSelected = false
            this.medicLayerSelected = false
            this.medicActivitiesWithLocation = [] // Activities that have coordinates collected by Medic
            this.medicActivitiesWithoutLocation = [] // Activities that don't have coordinates collected by Medic
            this.matchedDeviceHealthActivities = []// Activities that have matched Device Health coordinates
            this.unmatchedDeviceHealthActivities = [] // Activities that have not matched Device Health coordinates
            this.unmappedActivities = []
            map.setZoom(3)
        },
        selectChv: function (chv) {
            this.searchParams.uuid = chv.userObject.uuid
        },
        getLocationStats: function () {
            this.reinitializeMap()
            var self = this
            this.isLoading = true
            api.post('/stats/find', {
                'uuid': this.searchParams.uuid,
                'from': moment(this.searchParams.dates[0]).format('MM-DD-YYYY'),
                'to': moment(this.searchParams.dates[1]).format('MM-DD-YYYY'),
                'country': this.searchParams.country
            }).then(function (response) {
                if (response.data.status === true) {
                    if (response.data.data.locations.length > 0) {
                        self.processLocations(response.data.data.locations)
                    } else toastr.info('No location statistics found for the parameters specified')
                } else toastr.error('Oops! Something went wrong. Please try again')
                self.isLoading = false
            }).catch(function (error) {
                toastr.error('Oops! Something went wrong. Please try again')
                self.isLoading = false
                console.log(error)
            })
        },
        processLocations: function (locations) {
            this.filterUnmappedActivities(locations)
            this.filterMedicActivities(locations)
            this.filterDeviceHealthActivities(locations)
            this.createDataSources()
            this.animateChpMovement()
            if (this.matchedDeviceHealthActivities.length > 0) {
                this.deviceHealthLayerSelected = true
                this.medicLayerSelected = false
                this.toggleDeviceHealthData()
            } else {
                this.deviceHealthLayerSelected = false
                this.medicLayerSelected = true
                this.toggleMedicData()
            }
            this.repositionMap()
        },
        filterUnmappedActivities: function (array) {
            var self = this
            array.forEach(function (entry) {
                // Use Medic Coordinates
                if (entry.latitude == null || entry.longitude == null) {
                    var coordinates = JSON.parse(entry.medicCoordinates)
                    if (coordinates.lat === '' || coordinates.long === '') {
                        // set as unmatched activities
                        self.unmappedActivities.push({
                            'coordinates': {
                                'latitude': null,
                                'longitude': null
                            },
                            'activity': entry.activity,
                            'radius': 5,
                            'medicCoordinates': entry.medicCoordinates,
                            'client': entry.client,
                            'recordedAt': entry.timestamp,
                            'activityId': entry.activityId
                        })
                    }
                }
            })
        },
        filterMedicActivities: function (array) {
            var self = this
            array.forEach(function (entry) {
                // Use Medic Coordinates
                var coordinates = JSON.parse(entry.medicCoordinates)
                if (coordinates.lat === '' || coordinates.long === '') {
                    // set as unmatched activities
                    self.medicActivitiesWithoutLocation.push({
                        'coordinates': {
                            'latitude': null,
                            'longitude': null
                        },
                        'activity': entry.activity,
                        'radius': 5,
                        'medicCoordinates': entry.medicCoordinates,
                        'client': entry.client,
                        'recordedAt': entry.timestamp,
                        'activityId': entry.activityId
                    })
                } else {
                    self.medicActivitiesWithLocation.push({
                        'coordinates': {
                            'latitude': coordinates.lat,
                            'longitude': coordinates.long
                        },
                        'activity': entry.activity,
                        'radius': 5,
                        'medicCoordinates': entry.medicCoordinates,
                        'client': entry.client,
                        'recordedAt': entry.timestamp,
                        'activityId': entry.activityId
                    })
                }
            })
        },
        filterDeviceHealthActivities: function (array) {
            var self = this
            array.forEach(function (entry) {
                if (entry.latitude == null || entry.longitude == null) {
                    // Use Medic Coordinates
                    self.unmatchedDeviceHealthActivities.push({
                        'coordinates': {
                            'latitude': null,
                            'longitude': null
                        },
                        'activity': entry.activity,
                        'radius': 5,
                        'medicCoordinates': entry.medicCoordinates,
                        'client': entry.client,
                        'recordedAt': entry.timestamp,
                        'activityId': entry.activityId
                    })
                } else {
                    // Activities matched with device health coordinates
                    self.matchedDeviceHealthActivities.push({
                        'coordinates': {
                            'latitude': entry.latitude,
                            'longitude': entry.longitude
                        },
                        'activity': entry.activity,
                        'radius': 5,
                        'client': entry.client,
                        'recordedAt': entry.timestamp
                    })
                }
            })
        },
        createDataSources: function () {
            var medicData = {
                'type': 'FeatureCollection',
                'features': []
            }
            var deviceHealthData = {
                'type': 'FeatureCollection',
                'features': []
            }
            if (this.matchedDeviceHealthActivities.length > 0) {
                this.matchedDeviceHealthActivities.forEach(function (location) {
                    deviceHealthData.features.push({
                        'type': 'Feature',
                        'properties': {
                            'class': 'marker',
                            'radius': location.radius,
                            'activity': location.activity,
                            'client': location.client,
                            'recordedAt': location.recordedAt

                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [
                                location.coordinates.longitude,
                                location.coordinates.latitude
                            ]
                        }
                    })
                })
            }

            if (this.medicActivitiesWithLocation.length > 0) {
                this.medicActivitiesWithLocation.forEach(function (location) {
                    medicData.features.push({
                        'type': 'Feature',
                        'properties': {
                            'class': 'marker',
                            'radius': location.radius,
                            'activity': location.activity,
                            'client': location.client,
                            'recordedAt': location.recordedAt

                        },
                        'geometry': {
                            'type': 'Point',
                            'coordinates': [
                                location.coordinates.longitude,
                                location.coordinates.latitude
                            ]
                        }
                    })
                })
            }
            map.addSource('device-health-data-source',
                {
                    'type': 'geojson',
                    'data': deviceHealthData
                })
            map.addSource('medic-data-source',
                {
                    'type': 'geojson',
                    'data': medicData
                })
            this.addLayersToMap('medic')
            this.addLayersToMap('device-health')
        },
        addLayersToMap: function (type) {
            if (type === 'device-health') {
                var deviceHealthLayer = {
                    'id': 'device-health-data-layer',
                    'type': 'circle',
                    'interactive': true,
                    'source': 'device-health-data-source',
                    'layout': {
                        'visibility': 'none'
                    },
                    'paint': {
                        'circle-radius': 8,
                        'circle-color': 'rgba(66, 166, 138, 1)'
                    }

                }
                map.addLayer(deviceHealthLayer)
            } else if (type === 'medic') {
                var medicLayer = {
                    'id': 'medic-data-layer',
                    'type': 'circle',
                    'interactive': true,
                    'source': 'medic-data-source',
                    'layout': {
                        'visibility': 'none'
                    },
                    'paint': {
                        'circle-radius': 8,
                        'circle-color': 'rgba(58, 89, 255, 1)'
                    }

                }
                map.addLayer(medicLayer)
            } else if (type === 'device-health-line-string') {
                var deviceHealthLineString = {
                    'id': 'device-health-line-layer',
                    'type': 'line',
                    'source': 'device-health-line-source',
                    'symbol-placement': 'line',
                    'allow-overlap': true,
                    'icon-allow-overlap': true,
                    'layout': {
                        'line-cap': 'round',
                        'line-join': 'round',
                        'visibility': 'none'
                    },
                    'paint': {
                        'line-color': '#ed6498',
                        'line-width': 5,
                        'line-opacity': 0.8
                    }
                }
                map.addLayer(deviceHealthLineString)
            } else if (type === 'medic-line-string') {
                var medicLineString = {
                    'id': 'medic-line-layer',
                    'type': 'line',
                    'source': 'medic-line-source',
                    'symbol-placement': 'line',
                    'allow-overlap': true,
                    'icon-allow-overlap': true,
                    'layout': {
                        'line-cap': 'round',
                        'line-join': 'round',
                        'visibility': 'none'
                    },
                    'paint': {
                        'line-color': '#ed6498',
                        'line-width': 5,
                        'line-opacity': 0.8
                    }
                }
                map.addLayer(medicLineString)
            }
        },

        animateChpMovement: function () {
            var i = 0
            var self = this
            var medicLineString = {
                'type': 'FeatureCollection',
                'features': []
            }
            map.addSource('medic-line-source',
                {
                    'type': 'geojson',
                    'data': medicLineString
                })
            var feature =
                {
                    'type': 'Feature',
                    'geometry': {
                        'type': 'LineString',
                        'coordinates': []
                    }
                }
            var deviceHealthLineString = {
                'type': 'FeatureCollection',
                'features': []
            }
            map.addSource('device-health-line-source',
                {
                    'type': 'geojson',
                    'data': deviceHealthLineString
                })
            var lineStringFeature =
                {
                    'type': 'Feature',
                    'geometry': {
                        'type': 'LineString',
                        'coordinates': []
                    }
                }
            if (this.medicActivitiesWithLocation.length > 0) {
                window.setInterval(function () {
                    if (i < self.medicActivitiesWithLocation.length) {
                        var x = self.medicActivitiesWithLocation[i].coordinates.longitude
                        var y = self.medicActivitiesWithLocation[i].coordinates.latitude
                        feature.geometry.coordinates.push([x, y])
                        medicLineString.features.push(feature)
                        map.getSource('medic-line-source').setData(medicLineString)
                        i++
                    } else {
                        feature.geometry.coordinates = []
                        medicLineString.features.push(feature)
                        try {
                            map.getSource('medic-line-source').setData(medicLineString)
                        } catch (e) {
                        }
                        i = 0
                    }
                }, 500)

                // add Linestring layers
                this.addLayersToMap('medic-line-string')
            }
            if (this.matchedDeviceHealthActivities.length > 0) {
                window.setInterval(function () {
                    if (i < self.matchedDeviceHealthActivities.length) {
                        var x = self.matchedDeviceHealthActivities[i].coordinates.longitude
                        var y = self.matchedDeviceHealthActivities[i].coordinates.latitude
                        lineStringFeature.geometry.coordinates.push([x, y])
                        deviceHealthLineString.features.push(lineStringFeature)
                        map.getSource('device-health-line-source').setData(deviceHealthLineString)
                        i++
                    } else {
                        lineStringFeature.geometry.coordinates = []
                        deviceHealthLineString.features.push(lineStringFeature)
                        try {
                            map.getSource('device-health-line-source').setData(deviceHealthLineString)
                        } catch (e) {
                        }
                        i = 0
                    }
                }, 500)

                // add Linestring layers
                this.addLayersToMap('device-health-line-string')
            }
        },
        settingsHandler: function (command) {
            if (command === 'logout') {
                localStorage.setItem('auth-token', 'null')
                api.defaults.headers.common['Authorization'] = null
                this.$router.push({'path': '/login'})
            }
        },
        repositionMap: function () {
            var coordinates = []
            if (this.deviceHealthLayerSelected && !this.medicLayerSelected) {
                this.matchedDeviceHealthActivities.forEach(function (location) {
                    coordinates.push([location.coordinates.longitude, location.coordinates.latitude])
                })
            } else if (this.medicLayerSelected && !this.deviceHealthLayerSelected) {
                this.medicActivitiesWithLocation.forEach(function (location) {
                    coordinates.push([location.coordinates.longitude, location.coordinates.latitude])
                })
            } else {
                this.medicActivitiesWithLocation.concat(this.matchedDeviceHealthActivities).forEach(function (location) {
                    coordinates.push([location.coordinates.longitude, location.coordinates.latitude])
                })
            }

            if (coordinates.length > 0) {
                var bounds = coordinates.reduce(function (bounds, coord) {
                    return bounds.extend(coord)
                }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[1]))

                map.fitBounds(bounds, {
                    padding: 20
                })
            }
        },
        showPopup: function (location, layer) {
            var popup = new mapboxgl.Popup({
                closeButton: true,
                closeOnClick: true
            })
            var identifiedFeatures = map.queryRenderedFeatures(location.point, layer)
            console.log(location)
            popup.remove()
            if (identifiedFeatures !== '') {
                popup.setLngLat(location.lngLat)
                    .setHTML('<h4> Location Details</h4>' +
                            '<p>Latitude: ' + location.lngLat.lat + '</p>' +
                            '<p>Longitude: ' + location.lngLat.lng + '</p>' +
                            '<p>Time: ' + identifiedFeatures[0].properties.recordedAt + '</p>' +
                            '<p>Activity: ' + identifiedFeatures[0].properties.activity + '</p>' +
                            '<p>Client: ' + identifiedFeatures[0].properties.client + '</p>'
                    )
                    .addTo(map)
            }
        },
        toggleMedicData: function () {
            try {
                var markerVisibility = map.getLayoutProperty('medic-data-layer', 'visibility')
                if (markerVisibility === 'visible') {
                    this.medicLayerSelected = false
                    map.setLayoutProperty('medic-data-layer', 'visibility', 'none')
                    if (map.getLayer('medic-line-layer')) {
                        map.setLayoutProperty('medic-line-layer', 'visibility', 'none')
                    }
                } else {
                    this.medicLayerSelected = true
                    map.setLayoutProperty('medic-data-layer', 'visibility', 'visible')
                    if (map.getLayer('medic-line-layer')) {
                        map.setLayoutProperty('medic-line-layer', 'visibility', 'visible')
                    }
                }
            } catch (e) {
                this.medicLayerSelected = false
                toastr.error('This Layer has no data')
            }
        },
        toggleDeviceHealthData: function () {
            try {
                var visibility = map.getLayoutProperty('device-health-data-layer', 'visibility')

                if (visibility === 'visible') {
                    this.deviceHealthLayerSelected = false
                    map.setLayoutProperty('device-health-data-layer', 'visibility', 'none')
                    if (map.getLayer('device-health-line-layer')) {
                        map.setLayoutProperty('device-health-line-layer', 'visibility', 'none')
                    }
                } else {
                    this.deviceHealthLayerSelected = true
                    map.setLayoutProperty('device-health-data-layer', 'visibility', 'visible')
                    if (map.getLayer('device-health-line-layer')) {
                        map.setLayoutProperty('device-health-line-layer', 'visibility', 'visible')
                    }
                }
            } catch (e) {
                this.deviceHealthLayerSelected = false
                toastr.error('This Layer has no data')
            }
        }

    },
    mounted: function () {
        var self = this
        mapboxgl.accessToken = this.accessToken
        map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/mapbox/streets-v10',
            center: [31, 0],
            zoom: 3
        })

        map.on('click', function (e) {
            self.showPopup(e, 'device-health-data-layer')
        })
    },
    watch: {
        medicLayerSelected: function () {
            this.repositionMap()
        },
        deviceHealthLayerSelected: function () {
            this.repositionMap()
        },
        authToken: function (newValue) {
            console.log(newValue)
        }
    }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style type="text/scss">
    @import "../assets/css/animate.min.css";
    @import "../assets/css/light-bootstrap-dashboard.css";
    @import "../assets/css/bootstrap.min.css";
    @import "../assets/css/pe-icon-7-stroke.css";

    #map {
        width: 100%;
        height: 90vh;
        margin-top: 60px;
    }

    .marker {
        background-image: url('../assets/img/marker.png');
        background-size: cover;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        cursor: pointer;
    }

    .el-range-separator {
        width: 10% !important;
    }

    #app {

        font-family: 'Avenir', Helvetica, Arial, sans-serif;

    }

    a, a:hover {
        text-decoration: none;
    }

    .sidebar {
        background-image: url("../assets/img/sidebar-5.jpg");
    }

    #pause {
        position: absolute;
        margin: 20px;
    }

    #pause::after {
        content: 'Pause';
    }

    #pause.pause::after {
        content: 'Play';
    }

    .my-autocomplete {
        li {
            line-height: normal;
            padding: 7px;

            .value {
                text-overflow: ellipsis;
                overflow: hidden;
            }
            .link {
                font-size: 12px;
                color: #b4b4b4;
            }
        }
    }

    .el-autocomplete-suggestion {
        width: 300px !important;
    }
</style>
