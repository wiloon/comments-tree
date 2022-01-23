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
          class="comment-dialog"
        >
          <v-form ref="commentForm" v-model="commentsFormValid" lazy-validation>
            <v-card>
              <!-- 留言 text -->
              <v-textarea class="comment-content"
                          :label="commentLabel"
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
          <Comment
            :content="item.content"
            :commentId="item.id"
            :userName="item.userName"
            :updateTime="item.updateTime"
            v-on:comment-reply="reply"
          ></Comment>
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
  commentLabel = '留言'
  commentRule = {
    required: (value: string) => !!value || '请输入留言',
    min: (v: string) => (v && v.length >= 3) || '留言长度至少3个字',
    max: (v: string) => (v && v.length <= 200) || '留言长度至多200个字'
  }

  newComment (): void {
    this.replyCommentId = 0
    this.dialog = true
    this.commentLabel = '留言'
  }

  reply (commentId: number) {
    console.log('reply to: ' + commentId)
    this.replyCommentId = commentId
    this.dialog = true
    this.commentLabel = '评论'
  }

  loadCommentsTree (): void {
    console.log('home mounted')
    Axios.get('/comments',
      {
        headers: {},
        params: {}
      }).then(
      response => {
        if (response.data.data === undefined) {
          console.log('/comments, invalid response')
          return
        }
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
    Axios.get('/session',
      {
        headers: {},
        params: {}
      }).then(
      response => {
        const code = response.data.code
        console.log('session check response code: ' + code)
        if (code === 200) {
          this.$store.commit('login')
          this.$store.commit('updateUserInfo', { info: response.data.data.name + ' (' + response.data.data.email + ')' })
        } else {
          this.$store.commit('logout')
        }
      }
    )
  }
}
</script>

<style scoped lang="stylus">
.comment-content
  margin: 10px

.comment-dialog
  padding-top; 10px
</style>
