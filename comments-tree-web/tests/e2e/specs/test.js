// https://docs.cypress.io/api/introduction/api.html

describe('My First Test', () => {
  it('Visits the app root url', () => {
    cy.visit('/')
    cy.get('[data-cy=login-dialog]').click()

    cy.get('[data-cy=user-name]').type('admin')
    cy.get('[data-cy=password]').type('0oVHFEqB')
    cy.get('.v-input--selection-controls__ripple').click()
    cy.get('[data-cy=login]').click()
  })
})
