<template>
    <div class="sidebar" data-color="blue" data-image="assets/img/sidebar-1.jpg">

        <div class="sidebar-wrapper">
            <div class="logo">
                <img src="../assets/img/logo.png">
                <a style="font-family: Roboto; font-size: 15px" href="#" class="simple-text"> {{sidebarHeader}}
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
</template>

<script>
export default {
    name: 'Sidebar',
    props: [
        'sidebarHeader'
    ]
}
</script>
