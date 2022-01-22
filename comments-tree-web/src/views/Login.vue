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
                    <v-text-field data-cy="user-name" v-model="nameOrEmail" :rules="userNameRule" label="用户名/邮箱"
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
                    <v-checkbox
                      v-model="rememberMe"
                      label="记住我"
                    ></v-checkbox>
                  </v-col>

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
                    <v-btn x-large block @click="cancel">
                      取消
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
export default class Login extends Vue {
  rememberMe = true
  snackbar = false
  snackbarColor = 'success'
  snackbarText = ''
  dialog = true
  valid = true
  text = ''

  nameOrEmail = ''
  password = ''
  userNameRule = [
    (v: string) => !!v || '请输入用户名',
    (v: string) => /^[a-zA-Z0-9@._-]{4,16}$/.test(v) || '无效的用户名'
  ]

  passwordRule = {
    required: (value: string) => !!value || '请输入密码',
    min: (v: string) => (v && v.length >= 8) || '密码长度至少8位'
  }

  login (): void {
    if ((this.$refs.loginForm as Vue & { validate: () => boolean }).validate()) {
      Axios.post('/login',
        {
          nameOrEmail: this.nameOrEmail,
          password: this.password,
          rememberMe: this.rememberMe
        }).then((response: any) => {
        console.log('response.status: ' + response.status)
        if (response.status === 200 && response.data.code === 200) {
          console.log('login success')
          this.$store.commit('login')
          console.log('login status: ' + this.$store.state.login)
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
