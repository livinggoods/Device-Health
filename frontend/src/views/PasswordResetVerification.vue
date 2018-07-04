<template>
    <div class="login">
        <div style="padding-top: 200px; ">
            <el-card class="box-card center-block login-logo" style="border: none" >
                <div class="logo">
                    <img class="center-block" src="../assets/img/logo.png">
                </div>
            </el-card>
            <el-card class="box-card center-block" style="width: 550px; padding-top: 50px" >
                <h3 class="text-center" style="margin-bottom: 30px">Password Reset</h3>
                <el-form v-if="showForm==true" style="padding: 0px 30px 0px 30px" label-position="top" :rules="rules" ref="resetCompletionForm" :model="user" label-width="120px">
                    <p class="well">Password Reset Successful. Please enter your new password</p>
                    <el-form-item  prop="password">
                        <el-input placeholder="Password" type="password" v-model="user.password"></el-input>
                    </el-form-item>
                    <el-form-item  prop="repeatPassword">
                        <el-input placeholder="Repeat Password" type="password" v-model="user.repeatPassword"></el-input>
                    </el-form-item>
                    <el-form-item style="margin-top: 50px">
                        <el-button class="btn-block" @click="resetPassword" type="primary" >Submit</el-button>
                    </el-form-item>
                </el-form>

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
    name: 'PasswordResetVerification',
    data () {
        return {
            user: {
                'email': '',
                'password': '',
                'repeatPassword': ''
            },
            rules: {
                password: [
                    { required: true, message: 'Please input your password', trigger: 'blur' }
                ],
                repeatPassword: [
                    { required: true, message: 'Please input a verification of your password', trigger: 'blur' }
                ]
            },
            isLoading: false,
            showForm: false
        }
    },
    props: {},
    components: {
        Loading
    },
    methods: {
        validateToken: function (token) {
            this.isLoading = true
            var self = this
            api.get('/admin/password-reset/verify?token=' + token).then(function (response) {
                if (response.data.status === '200') {
                    self.user.email = response.data.email
                    self.showForm = true
                    self.linkSent = true
                } else {
                    toastr.error('Token Verification failed. Please try again')
                }
                self.isLoading = false
            }).catch(function (error) {
                console.log(error)
                toastr.error('Token Verification failed. Please try again')
                self.isLoading = false
            })
        },
        resetPassword: function () {
            this.isLoading = true
            var self = this
            this.$refs['resetCompletionForm'].validate((valid) => {
                if (valid && self.user.password === self.user.repeatPassword) {
                    api.post('/admin/password-reset/complete', this.user).then(function (response) {
                        if (response.data.status === '200') {
                            toastr.info('Password successfully reset. You can now proceed to login with the new credentials')
                            self.$router.push({path: '/login'})
                        } else {
                            toastr.error('An error has occurred. Please try again')
                        }
                        self.isLoading = false
                    }).catch(function (error) {
                        console.log(error)
                        toastr.error('Something went wrong. Please try again')
                        self.isLoading = false
                    })
                } else {
                    toastr.error('Please fill all mandatory fields')
                    self.isLoading = false
                    return false
                }
            })
        }
    },
    created: function () {
        if (this.$route.query.token) {
            this.validateToken(this.$route.query.token)
        } else {
            toastr.error('Token non-existent')
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
