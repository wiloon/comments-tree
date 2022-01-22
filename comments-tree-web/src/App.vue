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

      <v-btn
        href=""
        target="_blank"
        text
      >
        <span class="mr-2">Name</span>
        <v-icon>mdi-open-in-new</v-icon>
      </v-btn>
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

export default Vue.extend({
  name: 'App',

  data: () => ({
    //
  }),
  methods: {
    login: function () {
      console.log('login')
      this.$router.push({ path: '/login' })
    },
    register: function () {
      this.$router.push({ path: '/register' })
    },
    logout: function () {
      Axios.post('/logout',
        {}).then((response: any) => {
        console.log('logout response.status: ' + response.status)
        console.log('logout response.request.responseURL: ' + response.request.responseURL)
        console.log('logout response data: ' + response.data)
        console.log('logout response data token: ' + response.data.token)
        console.log('logout response data code: ' + response.data.code)
        if (response.status === 200) {
          this.$store.commit('logout')
          console.log('login status: ' + this.$store.state.login)
          window.location.href = response.request.responseURL
        }
      })
    }
  },
  mounted: function () {
    console.log('app mounted')
    Axios.get('/session',
      {
        headers: {},
        params: {}
      }).then(
      response => {
        console.log('session response: ' + response)
        console.log('session response data: ' + response.data)
        console.log('session response data code: ' + response.data.code)
        if (response.data.code === 200) {
          this.$store.commit('login')
        } else {
          this.$store.commit('logout')
        }
      }
    )
  }
})
</script>
