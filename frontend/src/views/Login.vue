<template>
    <div class="login">
        <div style="padding-top: 250px">
            <el-card class="box-card center-block" style="width: 550px" >
                <p>Login</p>
            </el-card>
        </div>


    </div>
</template>

<script>
import 'bootstrap'

export default {
    name: 'Login',
    data () {
        return {
            'user': {
                'email': '',
                'password': ''
            }

        }
    },
    props: {},
    components: {
    },
    methods: {
    /**
             * @param {number} a - value.
             * @param {number} b - value.
             * @return {number} result.
             */
        login: function () {

        }
    }

}
</script>

<style scoped>
    @import "../assets/css/animate.min.css";
    @import "../assets/css/light-bootstrap-dashboard.css";
    @import "../assets/css/bootstrap.min.css";
    @import "../assets/css/pe-icon-7-stroke.css";
    .login{
        background-image: url("../assets/img/login-bg.jpg");
        height: 100vh;
        width: 100vw;
    }

</style>
