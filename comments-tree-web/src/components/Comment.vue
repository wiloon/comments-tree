<template>
  <div>
    {{ content }}
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Comment',
  props: ['content'],
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
