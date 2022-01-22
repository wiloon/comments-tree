describe('Register Test', () => {
  it('Visits the app root url', () => {
    cy.visit('/')
    cy.get('[data-cy=register-dialog]').click()

    cy.get('[data-cy=user-name]').type('user0')
    cy.get('[data-cy=email]').type('user0@comments-tree.com')
    cy.get('[data-cy=password]').type('1qaz!QAZ')
    cy.get('[data-cy=register-btn]').click()
  })
})
