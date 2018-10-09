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
                                        <el-autocomplete
                                                popper-class="my-autocomplete"
                                                class="inline-input"
                                                v-model="searchParams.branchName"
                                                placeholder="Branch"
                                                :trigger-on-focus="false"
                                                @select="selectBranch"
                                                :fetch-suggestions="searchBranch">
                                            <i
                                                    class="el-icon-edit el-input__icon"
                                                    slot="suffix">
                                            </i>
                                            <template slot-scope="{ item }" style="width: 50px">
                                                <div class="value"><strong></strong> {{ item.value }}
                                                </div>
                                            </template>
                                        </el-autocomplete>
                                    </li>

                                    <li style="margin-top: 10px;margin-left: 5px;">
                                        <el-input placeholder="CHV Name"  v-model="searchParams.chvName"> <i
                                                class="el-icon-edit el-input__icon"
                                                slot="suffix">
                                        </i></el-input>
                                    </li>
                                    <li style="margin-left: 5px; margin-top: 10px;">
                                        <el-input label="With data bundle'" placeholder="Amount" v-model="searchParams.amount" >
                                            <el-select style="width: 250px;" slot="prepend" v-model="searchParams.operator" placeholder="Operator" >
                                                <el-option
                                                        label="With data bundle Less Than"
                                                        value="less_than">
                                                </el-option>
                                                <el-option
                                                        label="With data bundle Equal To"
                                                        value="equal_to">
                                                </el-option>
                                            </el-select>
                                        </el-input>
                                    </li>

                                    <li>
                                        <a href="javascript:" @click="getDataStats">
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
                <data-tables style="margin-top: 70px;" :data="balances" :pagination-props="{ pageSizes: [20] }">
                    <el-table-column v-for="title in titles" :prop="title.prop" :label="title.label" :key="title.label">
                    </el-table-column>
                </data-tables>
            </div>
        </div>
        <loading :active.sync="isLoading" :can-cancel="false"></loading>
    </div>
</template>

<script>
import 'bootstrap'
import {generalConfig, api} from '../../config/config'
import Sidebar from '@/components/Sidebar'
import Loading from 'vue-loading-overlay'
import toastr from 'toastr'

export default {
    name: 'BatteryStatistics',
    data () {
        return {
            'authToken': window.localStorage.getItem('auth-token'),
            'isLoading': false,
            'sidebarHeader': generalConfig.sidebarHeader,
            titles: [{
                prop: 'username',
                label: 'Username'
            }, {
                prop: 'name',
                label: 'Name'
            }, {
                prop: 'branch',
                label: 'Branch'
            },
            {
                prop: 'version_code',
                label: 'App Version'
            },
            {
                prop: 'balance',
                label: 'Balance'
            },
            {
                prop: 'balance_message',
                label: 'Raw Message'
            },
            {
                prop: 'date',
                label: 'Date'
            }],
            'mapOptions': {
                style: 'mapbox://styles/mapbox/streets-v10',
                center: [37, 0],
                zoom: 5
            },
            'searchParams': {
                'branchName': '',
                'chvName': '',
                'operator': 'equal_to',
                'value': '',
                'page': ''
            },
            balances: []

        }
    },
    props: {},
    components: {
        Sidebar,
        Loading
    },
    methods: {
        searchBranch: function (branchName, callback) {
            api.post('databalance/branches/find', {
                'branchName': branchName
            }).then(function (response) {
                var errorResult = [{'value': 'Branch not found'}]
                if (response.data.status === true) {
                    var branches = []
                    if (response.data.data.branches.length > 0) {
                        response.data.data.branches.forEach(function (branch) {
                            branches.push({'value': branch.branch})
                        })
                        callback(branches)
                    } else {
                        callback(errorResult)
                    }
                } else {
                    callback(errorResult)
                }
            }).catch(function (error) {
                var errorResult = [{'value': 'Branch not found'}]
                callback(errorResult)
                console.log(error)
            })
        },
        selectBranch: function (branch) {
            this.searchParams.branchName = branch.value
        },
        settingsHandler: function (command) {
            if (command === 'logout') {
                localStorage.setItem('auth-token', 'null')
                api.defaults.headers.common['Authorization'] = null
                this.$router.push({'path': '/login'})
            }
        },
        getDataStats: function () {
            var self = this
            api.post('/databalance/stats/find',
                self.searchParams
            ).then(function (response) {
                if (response.data.status === true) {
                    self.balances = response.data.data.balances
                }
                console.log(self.balances)
            }).catch(function (error) {
                console.log(error)
            })
        }

    },
    mounted: function () {
    },
    created: function () {
        // this.getDataStats()
    },
    watch: {
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

