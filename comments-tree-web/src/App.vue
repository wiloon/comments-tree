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
      <v-btn data-cy="login-dialog" v-on:click="login" class="navBarItem" v-if="!this.$store.state.login">登录</v-btn>
      <v-btn data-cy="register-dialog" v-on:click="register" class="navBarItem" v-if="!this.$store.state.login">注册
      </v-btn>
      <v-btn data-cy="logout-dialog" v-on:click="logout" class="navBarItem" v-if="this.$store.state.login">退出</v-btn>
    </v-app-bar>

    <v-main>
      <router-view/>
    </v-main>
    <v-row justify="center">
      <v-dialog
        v-model="dialog"
        persistent
        max-width="290"
      >
        <v-card>
          <v-card-title class="text-h5">
            用户未登录
          </v-card-title>
          <v-card-text>用户未登录或会话过期，请重新登录。
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              color="green darken-1"
              text
              @click="dialog = false"
            >
              取消
            </v-btn>
            <v-btn
              color="green darken-1"
              text
              @click="agree"
            >
              确认
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-row>
  </v-app>
</template>

<script lang="ts">
import Vue from 'vue'
import Axios from 'axios'
import { sessionCheck } from '@/api/session'
import router from '@/router'

export default Vue.extend({
  name: 'App',

  data: () => ({
    dialog: false
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
        if (response.status === 200) {
          this.$store.commit('logout')
          console.log('login status: ' + this.$store.state.login)
          window.location.href = response.request.responseURL
        }
      })
    },
    agree: function () {
      this.dialog = false
      router.push('/login')
    }
  },
  mounted: function () {
    // sessionCheck()
  },
  // watch: {
  //   '$store.state.count': function (newVal) {
  //     console.log('count: ' + newVal)
  //     this.dialog = true
  //   }
  // },
  computed: {
    userInfo () {
      return this.$store.state.userInfo
    }
  }
})
</script>
