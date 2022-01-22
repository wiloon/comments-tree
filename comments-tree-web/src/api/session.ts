import Axios from 'axios'
import store from '@/store'

export function sessionCheck (): void {
  Axios.get('/session',
    {
      headers: {},
      params: {}
    }).then(
    response => {
      if (response.data.code === 200) {
        store.commit('login')
        store.commit('updateUserInfo', { info: response.data.data.name + ' (' + response.data.data.email + ')' })
      } else {
        store.commit('logout')
      }
    }
  )
}
