<template>
  <div>
    <v-row>
      <v-col>
        #{{ commentId }}: {{ content }}
      </v-col>
      <v-col>
        <v-btn color="primary" @click="reply" v-if="this.$store.state.login">
          评论
        </v-btn>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Comment',
  props: ['content', 'commentId'],
  data: () => ({
    dialog: false,
    newMsg: ''
  }),
  methods: {
    reply: function () {
      console.log('reply, emit, comment id: ' + this.commentId)
      this.$emit('comment-reply', this.commentId)
    }
  }
})
</script>
