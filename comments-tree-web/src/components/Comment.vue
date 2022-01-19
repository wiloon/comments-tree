<template>
  <v-container>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import Axios from 'axios'

export default Vue.extend({
  name: 'Comment',

  data: () => ({
    dialog: false,
    newMsg: ''
  }),
  methods: {
    placeholder: function () {
      console.log('placeholder')
    }
  }
})
</script>
