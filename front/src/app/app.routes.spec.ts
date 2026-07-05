import { routes } from './app.routes';

describe('app routes', () => {
  it('should expose expected routes', () => {
    const paths = routes.map(route => route.path);

    expect(paths).toContain('');
    expect(paths).toContain('home');
    expect(paths).toContain('login');
    expect(paths).toContain('register');
    expect(paths).toContain('topics');
    expect(paths).toContain('feed');
    expect(paths).toContain('user');
    expect(paths).toContain('posts/new');
    expect(paths).toContain('posts/:postId');
    expect(paths).toContain('**');
  });

  it('should protect topics, feed, user and create-post routes', () => {
    const topicsRoute = routes.find(route => route.path === 'topics');
    const feedRoute = routes.find(route => route.path === 'feed');
    const userRoute = routes.find(route => route.path === 'user');
    const createPostRoute = routes.find(route => route.path === 'posts/new');

    expect(topicsRoute?.canActivate?.length).toBeGreaterThan(0);
    expect(feedRoute?.canActivate?.length).toBeGreaterThan(0);
    expect(userRoute?.canActivate?.length).toBeGreaterThan(0);
    expect(createPostRoute?.canActivate?.length).toBeGreaterThan(0);
  });
});
