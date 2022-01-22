import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    count: 0,
    login: false,
    userInfo: ''
  },
  mutations: {
    increment (state) {
      state.count++
    },
    login (state) {
      state.login = true
    },
    logout (state) {
      state.login = false
    },
    updateUserInfo (state, payload) {
      state.userInfo = payload.info
    }
  },
  actions: {},
  modules: {}
})
