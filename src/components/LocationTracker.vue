<template>
    <div>
        <div class="wrapper">
            <div class="sidebar" data-color="blue" data-image="assets/img/sidebar-1.jpg">

                <!--   you can change the color of the sidebar using: data-color="blue | azure | green | orange | red | purple" -->


                <div class="sidebar-wrapper">
                    <div class="logo">
                        <img src="../assets/img/logo.png">
                        <a class="simple-text">
                        </a>
                    </div>

                    <ul class="nav">
                        <li class="active">
                            <a href="/">
                                <i class="pe-7s-map-marker"></i>
                                <p>Location Statistics</p>
                            </a>
                        </li>

                        <li>
                            <a href="table.html">
                                <i class="pe-7s-battery"></i>
                                <p>Battery Statistics</p>
                            </a>
                        </li>
                        <li>
                            <a href="typography.html">
                                <i class="pe-7s-graph1"></i>
                                <p>Data Usage Statistics</p>
                            </a>
                        </li>
                        <li>
                            <a href="user.html">
                                <i class="pe-7s-user"></i>
                                <p>User Profile</p>
                            </a>
                        </li>


                    </ul>
                </div>
            </div>

            <div class="main-panel">
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
                            <a class="navbar-brand" href="#">DeviceHealth Dashboard</a>
                        </div>
                        <div class="collapse navbar-collapse">
                            <ul class="nav navbar-nav navbar-left">
                                <li>
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                        <i class="fa fa-dashboard"></i>
                                        <p class="hidden-lg hidden-md">Dashboard</p>
                                    </a>
                                </li>
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

                                <li style="margin-top: 10px;">
                                    <input type="text" class="form-control" placeholder="CHV Name">
                                </li>
                                <li style="margin-top: 10px; margin-left: 5px">
                                    <select class="form-control">
                                        <!--<option> Start Date           </option>-->
                                        <option>16/05/2018</option>
                                    </select>
                                </li>
                                <li style="margin-top: 10px; margin-left: 5px">
                                    <select class="form-control">
                                        <!--<option>   End Date         </option>-->
                                        <option>16/05/2018</option>
                                    </select>
                                </li>
                                <li>
                                    <a href="">
                                        <i class="fa fa-search"></i>
                                        <p class="hidden-lg hidden-md">Search</p>
                                    </a>

                                </li>

                            </ul>

                            <ul class="nav navbar-nav navbar-right">

                                <li class="dropdown">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                        <p>
                                            Options
                                            <b class="caret"></b>
                                        </p>

                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="#">Action</a></li>
                                        <li><a href="#">Another action</a></li>
                                        <li><a href="#">Something</a></li>
                                        <li><a href="#">Another action</a></li>
                                        <li><a href="#">Something</a></li>
                                        <li class="divider"></li>
                                        <li><a href="#">Separated link</a></li>
                                    </ul>
                                </li>
                                <li>
                                    <a href="#">
                                        <p>Log out</p>
                                    </a>
                                </li>
                                <li class="separator hidden-lg hidden-md"></li>
                            </ul>
                        </div>
                    </div>
                </nav>

                <div id='map'></div>

                <lg-map-viz :access-token="accessToken" :layers="layers" :map-options="mapOptions"></lg-map-viz>

            </div>
        </div>

    </div>
</template>

<script>
    import "bootstrap"
    import LgMapViz from 'lg-map-viz'

    export default {
        name: 'LocationTracker',
        data() {
            return {
                'accessToken': 'pk.eyJ1IjoiY2F5dm93Y29yaXIiLCJhIjoiY2poOHNiMzI2MDY4YTNhcWh4YnB1cHhzNCJ9.vtlXL9l0BgCri4iCJG0YqA',
                'mapOptions': {
                    style: 'mapbox://styles/mapbox/streets-v10',
                    center: [37, 0],
                    zoom: 5
                },
                'layers': [{
                    "id": "route",
                    "type": "line",
                    "source": {
                        "type": "geojson",
                        "data": {
                            "type": "Feature",
                            "properties": {},
                            "geometry": {
                                "type": "LineString",
                                "coordinates": [
                                    [37.48369693756104, 0.83381888486939],
                                    [37.48348236083984, 0.83317489144141],
                                    [37.48339653015138, 0.83270036637107],
                                    [37.48356819152832, 0.832056363179625],
                                    [37.48404026031496, 0.83114119107971],
                                    [37.48404026031496, 0.83049717427869],
                                    [37.48348236083984, 0.829920943955045],
                                    [37.48356819152832, 0.82954808664175],
                                    [37.48507022857666, 0.82944639795659],
                                    [37.48610019683838, 0.82880236636284],
                                    [37.48695850372314, 0.82931081282506],
                                    [37.48700141906738, 0.83080223556934],
                                    [37.48751640319824, 0.83168351665737],
                                    [37.48803138732912, 0.832158048267786],
                                    [37.48888969421387, 0.83297152392784],
                                    [37.48987674713133, 0.83263257682617],
                                    [37.49043464660643, 0.832937629287755],
                                    [37.49125003814696, 0.832429207817725],
                                    [37.49163627624512, 0.832564787218985],
                                    [37.49223709106445, 0.83337825839438],
                                    [37.49378204345702, 0.83368330777276]
                                ]
                            }
                        }
                    },
                    "layout": {
                        "line-join": "round",
                        "line-cap": "round"
                    },
                    "paint": {
                        "line-color": "#888",
                        "line-width": 8
                    }
                }]

            }
        },
        props: {},
        components: {
            // Mapbox,
            LgMapViz
        },
        methods: {
            /**
             * @param {number} a - value.
             * @param {number} b - value.
             * @return {number} result.
             */
        }

    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    @import "../assets/css/animate.min.css";
    @import "../assets/css/light-bootstrap-dashboard.css";
    @import "../assets/css/bootstrap.min.css";
    @import "../assets/css/pe-icon-7-stroke.css";

    #map {
        width: 100%;
        height: 90vh;
    }

    .sidebar {
        background-image: url("../assets/img/sidebar-5.jpg");
    }
</style>
