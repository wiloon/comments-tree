import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import 'roboto-fontface/css/roboto/roboto-fontface.css'
import '@mdi/font/css/materialdesignicons.css'
import axios from 'axios'

Vue.config.productionTip = false
Vue.prototype.$axios = axios
console.log('env: ' + process.env.NODE_ENV)
if (process.env.NODE_ENV !== 'production') {
  axios.defaults.baseURL = '/api'
}
new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
