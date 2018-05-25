<template>
    <div>
        <div class="wrapper">
            <div class="main-panel">
                <div class="topbar">
                    <nav class="navbar navbar-default navbar-fixed">
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
                                        <el-autocomplete
                                                class="inline-input"
                                                v-model="searchParams.chvName"
                                                placeholder="CHV Name"
                                                :trigger-on-focus="false"
                                                @select="selectChv"
                                                :fetch-suggestions="searchChv"
                                        ></el-autocomplete>
                                    </li>
                                    <li style="margin-left: 5px; margin-top: 10px;">
                                        <el-date-picker
                                                v-model="searchParams.dates"
                                                type="daterange"
                                                range-separator="To"
                                                start-placeholder="Start date"
                                                end-placeholder="End date"
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
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                            <i class="fa fa-cog"></i>
                                            <b class="caret hidden-sm hidden-xs"></b>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li><a href="#">Notification 1</a></li>
                                            <li><a href="#">Notification 2</a></li>
                                            <li><a href="#">Notification 3</a></li>
                                            <li><a href="#">Notification 4</a></li>
                                            <li><a href="#">Another notification</a></li>
                                        </ul>
                                    </li>
                                    <li class="separator hidden-lg hidden-md"></li>
                                </ul>
                            </div>
                        </div>
                    </nav>
                </div>
                <Sidebar :sidebar-header="sidebarHeader"></Sidebar>
                <lg-map-viz :access-token="accessToken" :layers="layers" :map-options="mapOptions"></lg-map-viz>
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

export default {
    name: 'LocationTracker',
    data () {
        return {
            'isLoading': false,
            'accessToken': generalConfig.mapboxApiKey,
            'mapOptions': {
                style: 'mapbox://styles/mapbox/streets-v10',
                center: [37, 0],
                zoom: 5
            },
            'sidebarHeader': generalConfig.sidebarHeader,
            'searchParams': {
                'chvName': '',
                'chvId': '',
                'dates': []
            },
            'layers': ''

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
            api.post('/user/find', {'username': username}).then(function (response) {
                if (response.data.status === true) {
                    var users = []
                    response.data.data.users.forEach(function (user) {
                        users.push({'value': user})
                        callback(users)
                    })
                }
            }).catch(function (error) {
                console.log(error)
            })
        },
        selectChv: function (chvName) {
            this.searchParams.chvName = chvName.value
        },

        getLocationStats: function () {
            var self = this
            this.isLoading = true
            api.post('/stats/find', {
                'username': this.searchParams.chvName,
                'from': moment(this.searchParams.dates[0]).format('MM-DD-YYYY')
            }).then(function (response) {
                self.processLocations(response.data.data.locations)
                self.isLoading = false
            }).catch(function (error) {
                self.isLoading = false
                console.log(error)
            })
        },
        processLocations: function (locations) {
            var reducedArray = this.filterDuplicates(locations)
            var radiusedLocations = this.computeRadius(reducedArray)
            var geoJsonified = this.transformToGeoJson(radiusedLocations)
            console.log(geoJsonified)
            this.layers = geoJsonified
        },
        transformToGeoJson: function (array) {
            var geoJsonified = {
                'id': 'route',
                'type': 'line',
                'source': {
                    'type': 'geojson',
                    'data': {
                        'type': 'Feature',
                        'properties': {
                        },
                        'geometry': {
                            'type': 'LineString',
                            'coordinates': []
                        }
                    }

                },
                'layout': {
                    'line-join': 'round',
                    'line-cap': 'round'
                },
                'paint': {
                    'line-color': '#888',
                    'line-width': 20
                }
            }
            array.forEach(function (location) {
                geoJsonified.source.data.geometry.coordinates.push([location.coordinates.longitude, location.coordinates.latitude])
            })
            return geoJsonified
        },
        filterDuplicates: function (array) {
            var consolidatedArray = []
            var filteredArray = []
            array.forEach(function (entry) {
                consolidatedArray.push({
                    'coordinates': JSON.stringify({
                        'latitude': entry.latitude,
                        'longitude': entry.longitude
                    }),
                    'recordedAt': entry.recordedAt
                })
            })
            var reducedObject = consolidatedArray.reduce(function (hash, entry) {
                if (!hash.hasOwnProperty(entry.coordinates)) hash[entry.coordinates] = []
                hash[entry.coordinates].push(entry)
                return hash
            }, {})
            filteredArray = Object.keys(reducedObject).map(i => reducedObject[i])
            return filteredArray
        },
        computeRadius: function (locations) {
            var radiusedLocations = []
            for (var i = 0; i < locations.length; i++) {
                var coordinates = JSON.parse(locations[i][0].coordinates)
                if (locations[i].length <= 1) {
                    radiusedLocations.push({'coordinates': {
                        'latitude': coordinates.latitude,
                        'longitude': coordinates.longitude},
                    'radius': 3
                    })
                } else {
                    var sortedLocation = locations[i].sort(function (a, b) {
                        return a.recordedAt - b.recordedAt
                    })
                    var timeSpent = moment.duration((moment.unix(sortedLocation[sortedLocation.length - 1].recordedAt)
                        .diff(moment.unix(sortedLocation[0].recordedAt)) / 1000)).asHours()
                    var radius
                    switch (Math.floor(timeSpent)) {
                    case 0:

                        radius = 5
                        break
                    case 1:
                        radius = 6
                        break
                    case 3:
                        radius = 7
                        break
                    case 4:
                        radius = 8
                        break
                    case 5:
                        radius = 9
                        break
                    default:
                        radius = 10
                        break
                    }
                    radiusedLocations.push({'coordinates': {
                        'latitude': coordinates.latitude,
                        'longitude': coordinates.longitude},
                    'radius': radius
                    })
                }
            }
            return radiusedLocations
        }

    },
    mounted: function () {

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
</style>
