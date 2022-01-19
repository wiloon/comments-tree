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
      <v-btn v-on:click="login" class="navBarItem">登录</v-btn>
    </v-app-bar>

    <v-main>
      <router-view/>
    </v-main>
  </v-app>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'App',

  data: () => ({
    //
  }),
  methods: {
    login: function () {
      console.log('login')
      this.$router.push({ path: '/login' })
    }
  }
})
</script>
