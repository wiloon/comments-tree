import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import Home from '../views/Home.vue'
import axios from 'axios'
import store from '@/store'

Vue.use(VueRouter)

const routes: Array<RouteConfig> = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import(/* webpackChunkName: "about" */ '../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import(/* webpackChunkName: "about" */ '../views/Register.vue')
  }
]

const router = new VueRouter({
  routes
})
router.beforeEach((to, from, next) => {
  console.log('router, before each, from: ' + from.name + ', to: ' + to.name)
  next()
})

axios.interceptors.response.use(function (response) {
  return response
}, function (error) {
  console.log('error.response.status: ' + error.response.status)
  if (error.response.status === 401) {
    const url = error.response.config.url
    const method = error.response.config.method
    console.log('response method: ' + method)
    console.log('response url: ' + url)
    if (url === '/session' && method === 'get') {
      console.log('session get 401')
    } else {
      store.commit('increment')
    }

    return Promise.reject(error)
  }
  return Promise.reject(error)
})

export default router
