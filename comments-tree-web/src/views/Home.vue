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
        <!-- 留言 dialog -->
        <v-dialog
          v-model="dialog"
          persistent
          max-width="290"
        >
          <v-form ref="commentForm" v-model="commentsFormValid" lazy-validation>
            <v-card>
              <v-card-title class="text-h5">
                留言
              </v-card-title>
              <!-- 留言 text -->
              <v-textarea
                label="留言"
                auto-grow
                outlined
                rows="10"
                row-height="10"
                v-model="newMsg"
                :counter="200"
                :rules="[commentRule.required, commentRule.min, commentRule.max]"
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
                  :disabled="!commentsFormValid"
                >
                  保存
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-form>
        </v-dialog>
      </v-col>
    </v-row>
    <v-row>

    </v-row>
    <v-row>
      <!-- 树形留言 -->
      <v-treeview
        open-all
        :open="nodeOpen"
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
import Component from 'vue-class-component'
import Axios from 'axios'
import Comment from '@/components/Comment.vue'

@Component({
  components: { Comment }
})
export default class Home extends Vue {
  dialog = false
  newMsg = ''
  items = []
  replyCommentId = 0
  nodeOpen = []
  commentsFormValid = true
  commentRule = {
    required: (value: string) => !!value || '请输入留言',
    min: (v: string) => (v && v.length >= 3) || '留言长度至少3个字',
    max: (v: string) => (v && v.length <= 200) || '留言长度至多200个字'
  }

  newComment (): void {
    this.dialog = true
  }

  reply (commentId: number) {
    console.log('reply to: ' + commentId)
    this.replyCommentId = commentId
    this.dialog = true
  }

  loadCommentsTree (): void {
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

  saveMsg (): void {
    console.log('saveMsg')
    if ((this.$refs.commentForm as Vue & { validate: () => boolean }).validate()) {
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
        } else if (response.data.code === 401) {
          this.$store.commit('logout')
          this.$router.push({ name: 'Login' })
        } else {
          console.log('msg save failed')
        }
        this.newMsg = ''
      })
    } else {
      console.log('comments validate failed')
    }
  }

  mounted (): void {
    this.loadCommentsTree()
  }
}
</script>
