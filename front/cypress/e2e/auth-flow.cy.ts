describe('Authentication flow', () => {
  it('allows a user to register with mocked backend', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 201,
      body: {
        token: 'fake-token',
        userId: 1,
        username: 'cypress-user',
        email: 'cypress@mail.test'
      }
    }).as('register');

    cy.visit('/register');
    cy.get('input[formControlName="email"]').type('cypress@mail.test');
    cy.get('input[formControlName="username"]').type('cypress-user');
    cy.get('input[formControlName="password"]').type('Cypress1!');
    cy.get('button[type="submit"]').click();

    cy.wait('@register');
    cy.url().should('include', '/feed');
  });

  it('allows a user to login with mocked backend', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        token: 'fake-token',
        userId: 1,
        username: 'cypress-user',
        email: 'cypress@mail.test'
      }
    }).as('login');

    cy.visit('/login');
    cy.get('input[formControlName="identifier"]').type('cypress-user');
    cy.get('input[formControlName="password"]').type('Cypress1!');
    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.url().should('include', '/topics');
  });

  it('shows error on invalid login', () => {
    cy.intercept('POST', '**/api/auth/login', { statusCode: 401, body: { message: 'Unauthorized' } }).as('loginFail');

    cy.visit('/login');
    cy.get('input[formControlName="identifier"]').type('bad-user');
    cy.get('input[formControlName="password"]').type('badPass');
    cy.get('button[type="submit"]').click();

    cy.wait('@loginFail');
    cy.get('.error, [class*="error"]').should('exist');
  });
});
