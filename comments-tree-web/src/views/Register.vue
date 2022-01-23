<template>
  <div id="app">
    <v-app>
      <v-dialog v-model="dialog" persistent max-width="600px" min-width="360px">
        <div>
          <v-card class="px-4">
            <v-card-text>
              <v-form ref="loginForm" v-model="valid" lazy-validation>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      data-cy="user-name"
                      v-model="userName"
                      :rules="userNameRule"
                      label="用户名"
                      required>
                    </v-text-field>
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      data-cy="email"
                      v-model="email"
                      :rules="emailRule"
                      label="邮箱"
                      required>
                    </v-text-field>
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      data-cy="password"
                      v-model="password"
                      :rules="[passwordRule.required, passwordRule.min, passwordRule.max,passwordRule.complexity]"
                      label="密码" hint="密码长度至少8位"
                      type="password"
                      counter>
                    </v-text-field>
                  </v-col>
                  <v-col class="d-flex" cols="12" sm="6" xsm="12">
                    <v-btn text @click="cancel">
                      取消
                    </v-btn>
                  </v-col>
                  <v-spacer></v-spacer>
                  <v-col class="d-flex" cols="12" sm="3" xsm="12" align-end>
                    <v-btn
                      data-cy="register-btn"
                      x-large
                      block
                      :disabled="!valid"
                      color="primary"
                      @click="register"> 注册
                    </v-btn>
                  </v-col>
                </v-row>
              </v-form>
            </v-card-text>
          </v-card>
        </div>
      </v-dialog>
      <v-snackbar
        v-model="snackbar"
        :color="snackbarColor"
        :timeout=3000
      >
        {{ snackbarText }}

        <template v-slot:action="{ attrs }">
          <v-btn
            text
            v-bind="attrs"
            @click="snackbar = true"
          >
            Close
          </v-btn>
        </template>
      </v-snackbar>
    </v-app>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import Component from 'vue-class-component'
import Axios from 'axios'

@Component({
  components: {}
})
export default class Register extends Vue {
  snackbar = false
  snackbarColor = 'success'
  snackbarText = ''
  dialog = true
  valid = true
  text = ''

  userName = ''
  password = ''
  email = ''
  isUserLogin = false
  userNameRule = [
    (v: string) => !!v || '请输入用户名',
    (v: string) => /^[a-zA-Z0-9._-]{5,20}$/.test(v) || '用户名长度5~20字符'
  ]

  emailRule = [
    (v: string) => !!v || '请输入邮箱',
    (v: string) => /^[0-9a-zA-Z_.-]+[@][0-9a-zA-Z_.-]+([.][a-zA-Z]+){1,2}$/.test(v) || '无效的邮箱地址'
  ]

  passwordRule = {
    required: (value: string) => !!value || '请输入密码',
    min: (v: string) => (v && v.length >= 8) || '密码长度至少8位',
    max: (v: string) => (v && v.length <= 20) || '密码长度少于20位',
    complexity: (v: string) => /^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\d)(?=.*?[!@#$%^&*()\-_=+;])[a-zA-Z\d!@#$%^&*()\-_=+;]*$/.test(v) || '密码复杂度太低，至少包含一个大写，一个小写，一个数字，一个特殊符号 (!@#$%^&*()-_=+;)'
  }

  // user register
  register (): void {
    if ((this.$refs.loginForm as Vue & { validate: () => boolean }).validate()) {
      Axios.post('/user',
        {
          name: this.userName,
          email: this.email,
          password: this.password
        }).then((response: any) => {
        if (response.data.code === 200) {
          console.log('register success')
          this.$store.commit('login')
          this.$router.push({ name: 'Home' })
        } else {
          this.snackbarColor = 'error'
          this.snackbarText = response.data.message
          this.snackbar = true
        }
      })
    } else {
      console.log('validate failed')
    }
  }

  cancel (): void {
    this.$router.push({ name: 'Home' })
  }
}
</script>

<style scoped lang="stylus">
.login-form
  max-width: 500px

</style>
