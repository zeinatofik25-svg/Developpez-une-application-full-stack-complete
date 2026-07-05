describe('App routing', () => {
  it('redirects unknown route to not-found page', () => {
    cy.visit('/some-unknown-route', { failOnStatusCode: false });
    cy.contains(/404|introuvable|not found/i).should('exist');
  });

  it('loads home page', () => {
    cy.visit('/');
    cy.url().should('include', '/');
  });

  it('redirects to /login when accessing protected route unauthenticated', () => {
    cy.visit('/topics');
    cy.url().should('include', '/login');
  });

  it('loads /login page', () => {
    cy.visit('/login');
    cy.get('input[formControlName="identifier"]').should('exist');
    cy.get('input[formControlName="password"]').should('exist');
  });

  it('loads /register page', () => {
    cy.visit('/register');
    cy.get('input[formControlName="email"]').should('exist');
    cy.get('input[formControlName="username"]').should('exist');
    cy.get('input[formControlName="password"]').should('exist');
  });
});
