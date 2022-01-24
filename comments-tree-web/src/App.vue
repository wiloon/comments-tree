<template>
  <v-app>
    <v-app-bar
      app
      color="primary"
      elevation="4"
    >
      <div class="d-flex align-center">

      </div>

      <v-spacer></v-spacer>

      <v-chip
        v-if="this.$store.state.login"
        class="ma-2"
      >
        {{ userInfo }}
      </v-chip>
      <v-btn
        data-cy="login-dialog"
        v-on:click="login"
        class="navBtn"
        v-if="!this.$store.state.login">
        登录
      </v-btn>
      <v-btn
        data-cy="register-dialog"
        v-on:click="register"
        class="navBtn"
        depressed
        v-if="!this.$store.state.login">
        注册
      </v-btn>
      <v-btn
        data-cy="logout-dialog"
        v-on:click="logout"
        class="navBtn"
        depressed
        v-if="this.$store.state.login">
        退出
      </v-btn>
    </v-app-bar>

    <v-main>
      <router-view/>
    </v-main>
    <v-row justify="center">
      <v-snackbar
        v-model="snackbar"
      >
        {{ snackbarText }}
        <template v-slot:action="{ attrs }">
          <v-btn
            color="pink"
            text
            v-bind="attrs"
            @click="agree"
            v-if="toLogin"
          >
            确认
          </v-btn>
          <v-btn
            text
            v-bind="attrs"
            @click="snackbar = false"
          >
            关闭
          </v-btn>
        </template>
      </v-snackbar>
    </v-row>
  </v-app>
</template>

<script lang="ts">
import Vue from 'vue'
import Axios from 'axios'
import router from '@/router'

export default Vue.extend({
  name: 'App',

  data: () => ({
    snackbar: false,
    snackbarText: '',
    toLogin: false
  }),
  methods: {
    // 登录
    login: function () {
      console.log('login')
      this.$router.push({ path: '/login' })
    },

    // 注册
    register: function () {
      this.$router.push({ path: '/register' })
    },

    // 退出
    logout: function () {
      Axios.post('/logout',
        {}).then((response: any) => {
        if (response.status === 200 && response.data.code === 200) {
          this.$store.commit('logout')
          console.log('login status: ' + this.$store.state.login)
          // window.location.href = response.request.responseURL
        }
      })
    },
    agree: function () {
      this.snackbar = false
      router.push('/login')
    }
  },
  watch: {
    '$store.state.count': function (newVal) {
      console.log('count: ' + newVal)
      console.log('show snap, this path: ' + this.$router.currentRoute.path)
      if (this.$router.currentRoute.path === '/login') {
        this.snackbarText = '登录失败，用户名或密码错错误。'
        this.toLogin = false
      } else {
        this.snackbarText = '用户未登录或会话过期，请重新登录。'
        this.toLogin = true
      }
      this.snackbar = true
    }
  },
  computed: {
    userInfo () {
      return this.$store.state.userInfo
    }
  }
})
</script>
<style scoped lang="stylus">
.navBtn
  margin-left 5px

</style>
