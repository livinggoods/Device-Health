<template>
    <div class="login">
        <div style="padding-top: 200px; ">
            <el-card class="box-card center-block login-logo" style="border: none" >
                <div class="logo">
                    <img class="center-block" src="../assets/img/logo.png">
                </div>
            </el-card>
            <el-card class="box-card center-block" style="width: 550px; padding-top: 50px" >
                <h3 class="text-center" style="margin-bottom: 30px">Login</h3>
                <el-form style="padding: 0px 30px 0px 30px" label-position="top" :rules="rules" ref="loginForm" :model="user" label-width="120px">
                    <el-form-item  prop="name">
                        <el-input placeholder="Name" v-model="user.name"></el-input>
                    </el-form-item>
                    <el-form-item  prop="email">
                        <el-input placeholder="Email" v-model="user.email"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input placeholder="Password" type="password" v-model="user.password"></el-input>
                    </el-form-item>
                    <el-form-item style="margin-top: 50px">
                        <el-button class="btn-block" @click="submit" type="primary" >Submit</el-button>
                    </el-form-item>
                </el-form>
                <p class="center-block text-center"><a href="password/reset">Forgot your password?</a></p>

            </el-card>
        </div>
        <loading :active.sync="isLoading" :can-cancel="false"></loading>
    </div>
</template>

<script>
import 'bootstrap'
import {api} from '../../config/config'
import toastr from 'toastr'
import Loading from 'vue-loading-overlay'

export default {
    name: 'Login',
    data () {
        return {
            'user': {
                'email': '',
                'password': '',
                'name': ''
            },
            rules: {
                email: [
                    { required: true, message: 'Please input your email', trigger: 'blur' }
                ],
                password: [
                    { required: true, message: 'Please input your password', trigger: 'blur' }
                ]
            },
            isLoading: false

        }
    },
    props: {},
    components: {
        Loading
    },
    methods: {
        submit: function () {
            this.isLoading = true
            var self = this
            this.$refs['loginForm'].validate((valid) => {
                if (valid) {
                    api.post('/admin/make/new/account', this.user).then(function (response) {
                        if (response.data.status === '200') {
                            self.$router.push({ path: '/login' })
                        } else {
                            toastr.error('Email/Password mismatch. Please try again')
                        }
                        self.isLoading = false
                    }).catch(function (error) {
                        console.log(error)
                        toastr.error('Something went wrong. Please try again')
                        self.isLoading = false
                    })
                } else {
                    toastr.error('Please enter your credentials')
                    self.isLoading = false
                    return false
                }
            })
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
    .login-logo{
        width: 300px;
        background-color: #005084;
        position: absolute;
        right: 0;
        left: 0;
        margin-top: -48px;
    }

</style>
