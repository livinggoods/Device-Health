<template>
    <div>

        <el-row>
            <el-col :span="24">
                <div class="grid-content" style="text-align: center;">
                    <h4>Device Health Dashboard Configuration</h4>
                </div>
                <hr>
            </el-col>
        </el-row>
        <div class="row">

            <div class="col-sm-6 col-lg-offset-3" style="border: 1px solid gray; border-radius: 3px;padding: 20px 10px">

                <el-form :model="configForm" ref="configForm" label-width="120px">
                    <el-form-item label="Primary Color">
                        <input v-model="configForm.themeColor" type="color">
                        <el-input style="width: 140px" v-model="configForm.themeColor" ></el-input>
                    </el-form-item>
                    <el-form-item label="Site Title">
                        <el-input v-model="configForm.title"></el-input>
                    </el-form-item>
                    <el-form-item label="Sidebar Header">
                        <el-input v-model="configForm.sideBarHeader"></el-input>
                    </el-form-item>
                    <el-form-item label="Super Admin Email">
                        <el-input v-model="configForm.adminEmail"></el-input>
                    </el-form-item>

                    <el-form-item label="Site Logo">
                        <div >
                            <img :style="{backgroundColor:configForm.themeColor}" src="../assets/img/logo.png"/>
                            <el-input></el-input>
                        </div>
                    </el-form-item>
                    <el-row>
                        <el-col :span="24">

                        </el-col>
                    </el-row>


                    <el-form-item>
                        <el-button type="primary" @click="submitForm('ruleForm')">Create</el-button>
                        <el-button @click="resetForm('ruleForm')">Reset</el-button>
                    </el-form-item>
                </el-form>

            </div>


        </div>

    </div>
</template>

<script>
    import "bootstrap"
    import {Chrome} from 'vue-color'
    import styles from '../assets/scss/element-variables.scss'
    import Config from '../../config/config'

    var themeColor=styles.primaryColor

    export default {
        name: 'DashboardConfig',
        data() {
            return {
                'configForm': {
                    themeColor: themeColor,
                    title: Config.title,
                    sideBarHeader: Config.sideBarHeader,
                }
            }
        },
        props: {},
        components: {
            'chrome-picker': Chrome
        },
        methods: {
            /**
             * @param {number} a - value.
             * @param {number} b - value.
             * @return {number} result.
             */

        },
        mounted: function () {

        }

    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style >

</style>
