<template>
    <!--Navbar-->

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

                            ></el-autocomplete>
                        </li>
                        <li style="margin-left: 5px; margin-top: 10px;">
                            <el-date-picker
                                    v-model="searchParams.date"
                                    type="daterange"
                                    range-separator="To"
                                    start-placeholder="Start date"
                                    end-placeholder="End date">
                            </el-date-picker>
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

</template>

<script>

export default {
    name: 'Navbar',
    props: [
        'searchParams'
    ]
}
</script>
