import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then(m => m.HomeComponent),
  },

  {
    path: 'home',
    redirectTo: '',
    pathMatch: 'full',
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then(m => m.LoginComponent),
  },

  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register.component').then(m => m.RegisterComponent),
  },

  {
    path: 'topics',
    loadComponent: () =>
      import('./pages/topics-page/topics-page.component').then(
        m => m.TopicsPageComponent
      ),
    canActivate: [authGuard],
  },

  {
    path: 'feed',
    loadComponent: () =>
      import('./pages/feed-page/feed-page.component').then(
        m => m.FeedPageComponent
      ),
    canActivate: [authGuard],
  },

  {
    path: 'user',
    loadComponent: () =>
      import('./pages/user-page/user-page.component').then(
        m => m.UserPageComponent
      ),
    canActivate: [authGuard],
  },

  {
    path: 'posts/new',
    loadComponent: () =>
      import('./pages/create-post/create-post.component').then(
        m => m.CreatePostComponent
      ),
    canActivate: [authGuard],
  },

  {
    path: 'posts/:postId',
    loadComponent: () =>
      import('./pages/post-detail/post-detail.component').then(
        m => m.PostDetailComponent
      ),
  },

  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found.component').then(
        m => m.NotFoundComponent
      ),
  },
];
