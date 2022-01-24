<template>
  <div class="comment">
    <v-card
      outlined
    >
      <v-card-text>
        <div> {{ content }}</div>
        <div class="grey--text ms-4">
          ID: {{ commentId }}, 用户: {{ userName }}, 日期: {{ updateTime }}
        </div>
      </v-card-text>
      <v-card-actions>
        <v-btn
          color="primary accent-4"
          text
          @click="reply"
          v-if="this.$store.state.login"
        >
          评论
        </v-btn>
      </v-card-actions>
    </v-card>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Comment',
  props: ['content', 'commentId', 'userName', 'updateTime'],
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

<style scoped lang="stylus">
.comment
  margin-bottom 3px

</style>
