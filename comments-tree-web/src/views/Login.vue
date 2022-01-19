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
                    <v-text-field data-cy="user-name" v-model="userName" :rules="userNameRule" label="用户名"
                                  required></v-text-field>
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      data-cy="password"
                      v-model="password"
                      :rules="[passwordRule.required, passwordRule.min]"
                      label="密码" hint="密码长度至少8位"
                      type="password"
                      counter>
                    </v-text-field>
                  </v-col>
                  <v-col class="d-flex" cols="12" sm="6" xsm="12">
                    <v-btn v-if="showCorpWechatLogin" text @click="register">
                      注册
                    </v-btn>
                  </v-col>
                  <v-spacer></v-spacer>
                  <v-col class="d-flex" cols="12" sm="3" xsm="12" align-end>
                    <v-btn
                      x-large
                      block
                      :disabled="!valid"
                      color="primary"
                      @click="login"
                      data-cy="login"
                    >登录
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
export default class About extends Vue {
  showCorpWechatLogin = true
  snackbar = false
  snackbarColor = 'success'
  snackbarText = ''
  show1 = true
  dialog = true
  valid = true
  tab = 0
  multiLine = true
  text = 'I\'m a multi-line snackbar.'
  options = {
    isLoggingIn: true,
    shouldStayLoggedIn: true
  }

  userName = ''
  password = ''
  userNameRule = [
    // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
    (v: string) => !!v || '请输入用户名',
    (v: string) => /^[a-zA-Z0-9._-]{4,16}$/.test(v) || '无效的用户名'
  ]

  passwordRule = {
    required: (value: string) => !!value || '请输入密码',
    min: (v: string) => (v && v.length >= 8) || '密码长度至少8位'
  }

  login (): void {
    if ((this.$refs.loginForm as Vue & { validate: () => boolean }).validate()) {
      Axios.post('/user/login',
        {
          name: this.userName,
          password: this.password
        }).then((response: any) => {
        console.log('login response: ' + response)
        console.log('login response data: ' + response.data)
        console.log('login response data token: ' + response.data.token)
        console.log('login response data code: ' + response.data.code)
        if (response.data.code === 200) {
          console.log('login success')
          // to third party activate
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

  register () {
    console.log('register')
  }

  mounted () {
    console.log('vue app domain: ' + process.env.VUE_APP_DOMAIN)
    if (process.env.NODE_ENV === 'development') {
      this.showCorpWechatLogin = true
    }
  }
}
</script>

<style scoped lang="stylus">
.login-form
  max-width: 500px

</style>
