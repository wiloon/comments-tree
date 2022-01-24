// https://docs.cypress.io/api/introduction/api.html

describe('Comment Reply Test', () => {
  it('Visits the app root url', () => {
    cy.visit('/')
    cy.get('[data-cy=login-dialog]').click()

    cy.get('[data-cy=user-name]').type('user0')
    cy.get('[data-cy=password]').type('1qaz!QAZ')
    cy.get('[data-cy=login]').click()
    cy.get('[data-cy=comment-new]').click() // 新留言
    cy.get('[data-cy=comment-text]').type('comment test, 留言/评论测试，comment test, 留言/评论测试，comment test, 留言/评论测试，') // 输入留言
    cy.get('[data-cy=comment-save]').click() // 保存留言
    var genArr = Array.from({ length: 50 }, (v, k) => k + 1)
    cy.wrap(genArr).each((index) => {
      cy.get('[data-cy=reply-btn]').last().click() // 点击最后一个评论
      cy.get('[data-cy=comment-text]').type('comment test, 留言/评论测试，comment test, 留言/评论测试，comment test, 留言/评论测试，comment test, 留言/评论测试，comment test, 留言/评论测试，comment test, 留言/评论测试，') // 输入评论
      cy.get('[data-cy=comment-save]').click() // 保存评论
      cy.wait(200)
      cy.get('.v-icon').last().click() // 展开评论
    })
  })
})
