import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    count: 0,
    login: false
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
    }
  },
  actions: {
  },
  modules: {
  }
})
