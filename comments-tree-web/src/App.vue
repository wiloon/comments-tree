<template>
  <v-app>
    <v-app-bar
      app
      color="primary"
      dark
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
      <v-btn v-on:click="login" class="navBarItem" v-if="!this.$store.state.login" data-cy="login-dialog">登录</v-btn>
      <v-btn v-on:click="register" class="navBarItem" v-if="!this.$store.state.login">注册</v-btn>
      <v-btn v-on:click="logout" class="navBarItem" v-if="this.$store.state.login">退出</v-btn>
    </v-app-bar>

    <v-main>
      <router-view/>
    </v-main>
  </v-app>
</template>

<script lang="ts">
import Vue from 'vue'
import Axios from 'axios'
import { sessionCheck } from '@/api/session'

export default Vue.extend({
  name: 'App',

  data: () => ({}),
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
        if (response.status === 200) {
          this.$store.commit('logout')
          console.log('login status: ' + this.$store.state.login)
          window.location.href = response.request.responseURL
        }
      })
    }
  },
  mounted: function () {
    sessionCheck()
  },
  computed: {
    userInfo () {
      return this.$store.state.userInfo
    }
  }
})
</script>
