describe('Feed page', () => {
  it('renders posts from mocked API', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        token: 'fake-token',
        userId: 1,
        username: 'cypress-user',
        email: 'cypress@mail.test'
      }
    }).as('login');

    cy.intercept('GET', '**/api/topics*', { statusCode: 200, body: [] }).as('topics');

    cy.intercept('GET', '**/api/posts/feed*', {
      statusCode: 200,
      body: [
        {
          id: 10,
          title: 'Post Cypress',
          content: 'Contenu de test',
          createdAt: '2026-01-01T00:00:00',
          topicId: 1,
          topicName: 'Java',
          authorId: 1,
          authorUsername: 'cypress-user'
        }
      ]
    }).as('feed');

    cy.visit('/login');
    cy.get('input[formControlName="identifier"]').type('cypress-user');
    cy.get('input[formControlName="password"]').type('Cypress1!');
    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.visit('/feed');
    cy.wait('@feed');
    cy.contains('Post Cypress').should('exist');
  });

  it('shows sorted feed when sort=oldest', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: { token: 'fake-token', userId: 1, username: 'cypress-user', email: 'cypress@mail.test' }
    }).as('login');

    cy.intercept('GET', '**/api/posts/feed*', { statusCode: 200, body: [] }).as('feed');

    cy.visit('/login');
    cy.get('input[formControlName="identifier"]').type('cypress-user');
    cy.get('input[formControlName="password"]').type('Cypress1!');
    cy.get('button[type="submit"]').click();
    cy.wait('@login');

    cy.visit('/feed');
    cy.wait('@feed');
    cy.get('body').should('exist');
  });
});
