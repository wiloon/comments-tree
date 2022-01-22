<template>
  <v-container>
    <v-row>
      <v-col>
        <v-btn color="primary" @click="newComment" v-if="this.$store.state.login">
          留言
        </v-btn>
      </v-col>
    </v-row>
    <v-row class="text-center">
      <v-col cols="12">
        <v-dialog
          v-model="dialog"
          persistent
          max-width="290"
        >
          <v-card>
            <v-card-title class="text-h5">
              留言
            </v-card-title>
            <v-textarea
              label="留言"
              auto-grow
              outlined
              rows="10"
              row-height="10"
              v-model="newMsg"
            ></v-textarea>
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
                v-on:click="saveMsg"
              >
                保存
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-col>
    </v-row>
    <v-row>

    </v-row>
    <v-row>
      <v-treeview
        open-all
        :items="items"
        item-text="content"
        item-children="reply"
      >
        <template v-slot:label="{ item }">
          <Comment :content="item.content" :commentId="item.id" v-on:comment-reply="reply"></Comment>
        </template>
      </v-treeview>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import Axios from 'axios'
import Comment from '@/components/Comment.vue'

export default Vue.extend({
  name: 'Home',
  components: {
    Comment
  },
  data: () => ({
    dialog: false,
    newMsg: '',
    items: [],
    replyCommentId: 0
  }),
  methods: {
    newComment: function () {
      this.dialog = true
    },
    reply: function (commentId: number) {
      console.log('reply to: ' + commentId)
      this.replyCommentId = commentId
      this.dialog = true
    },
    saveMsg: function () {
      console.log('saveMsg')
      this.dialog = false
      console.log(this.newMsg)

      Axios.post('/comment',
        {
          content: this.newMsg,
          parentId: this.replyCommentId
        }).then((response: any) => {
        console.log('login response: ' + response)
        console.log('login response data: ' + response.data)
        console.log('login response data token: ' + response.data.token)
        console.log('login response data code: ' + response.data.code)
        if (response.data.code === 200) {
          console.log('msg save success')
          // to third party activate
          this.loadCommentsTree()
        } else {
          console.log('msg save failed')
        }
        this.newMsg = ''
      })
    },
    loadCommentsTree: function () {
      console.log('home mounted')
      Axios.get('/comments',
        {
          headers: {},
          params: {}
        }).then(
        response => {
          this.items = response.data.data.reply
        }
      )
    }
  },
  mounted: function () {
    this.loadCommentsTree()
  }
})
</script>
