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
                <div id="map"></div>
                <button id='pause'></button>
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
                'from': moment(this.searchParams.dates[0]).format('MM-DD-YYYY'),
                'to': moment(this.searchParams.dates[1]).format('MM-DD-YYYY')
            }).then(function (response) {
                if (response.data.status === true) {
                    if (response.data.data.locations.length > 0) {
                        self.processLocations(response.data.data.locations)
                    } else toastr.info('No location statistics found for the parameters specified')
                } else toastr.error('Oops! Something went wrong. Please try again')
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

            this.fitBounds(locations)
            this.addMarkersToMap(geoJsonified)
            this.animateChpMovement(locations)
            this.layers = geoJsonified
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
                    'radius': 3,
                    'recordedAt': locations[i][0].recordedAt
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
        },
        transformToGeoJson: function (locationArray) {
            var geoJsonified
            geoJsonified = {
                'id': 'points',
                'type': 'symbol',
                'source': {
                    'type': 'geojson',
                    'data': {
                        'type': 'FeatureCollection',
                        'features': []
                    }

                }

            }
            locationArray.forEach(function (location) {
                geoJsonified.source.data.features.push({

                    'type': 'Feature',
                    'properties': {
                        'class': 'marker',
                        'radius': location.radius,
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
                // geoJsonified.source.data.geometry.coordinates.push([location.coordinates.longitude, location.coordinates.latitude])
            })

            return geoJsonified
        },
        addMarkersToMap: function (geoJson) {
            // try to remove any layers of the same kind that already exist on the map
            // try {
            //     map.removeLayer('points')
            // } catch (e) {
            //     //
            // }
            //
            // map.addLayer(geoJson)
            console.log(geoJson)

            geoJson.source.data.features.forEach(function (marker) {
                // create a HTML element for each feature
                var el = document.createElement('div')

                el.className = 'marker'
                el.style.width = marker.properties.radius * 4 + 'px'
                el.style.height = marker.properties.radius * 4 + 'px'

                // add popup
                var popup = new mapboxgl.Popup({ offset: 25 })
                    .setHTML('<h4> Location Details</h4>' +
                        '<p>Latitude: ' + marker.geometry.coordinates[0] + ' Latitude: ' + marker.geometry.coordinates[1] + '</p>' +
                        '<p>Time: ' + marker.properties.recordedAt + '</p>')

                new mapboxgl.Marker(el)
                    .setLngLat(marker.geometry.coordinates)
                    .setPopup(popup)
                    .addTo(map)
            })
        },
        animateChpMovement: function (locations) {
            var geoJson = {
                'type': 'FeatureCollection',
                'features': [{
                    'type': 'Feature',
                    'geometry': {
                        'type': 'LineString',
                        'coordinates': [
                            [locations[0].longitude, locations[0].latitude]
                        ]
                    }
                }]
            }
            // try {
            //     map.removeLayer('line-animation')
            // } catch (e) {
            //     //
            // }
            var lineString = {
                'id': 'line-animation',
                'type': 'line',
                'source': {
                    'type': 'geojson',
                    'data': geoJson
                },
                'layout': {
                    'line-cap': 'round',
                    'line-join': 'round'
                },
                'paint': {
                    'line-color': '#ed6498',
                    'line-width': 5,
                    'line-opacity': 0.8
                }
            }

            map.addLayer(lineString)

            // click the button to pause or play

            var i = 0
            var timer = window.setInterval(function () {
                if (i < locations.length) {
                    var x = locations[i].longitude
                    var y = locations[i].latitude
                    geoJson.features[0].geometry.coordinates.push([x, y])
                    map.getSource('line-animation').setData(geoJson)
                    i++
                } else {
                    geoJson.features[0].geometry.coordinates = []
                    map.getSource('line-animation').setData(geoJson)
                    i = 0
                }
            }, 500)
        },
        fitBounds: function (locations) {
            var coordinates = []
            locations.forEach(function (location) {
                coordinates.push([location.longitude, location.latitude])
            })
            var bounds = coordinates.reduce(function (bounds, coord) {
                return bounds.extend(coord)
            }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]))

            map.fitBounds(bounds, {
                padding: 20
            })
        }

    },
    mounted: function () {
        mapboxgl.accessToken = this.accessToken
        map = new mapboxgl.Map({
            container: 'map',
            style: 'mapbox://styles/mapbox/streets-v10',
            center: [31, 0],
            zoom: 3
        })
    },
    watch: {

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
    .marker {
        background-image: url('../assets/img/marker.png');
        background-size: cover;

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
</style>
