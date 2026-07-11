import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth/auth.service';

/**
 * Bloque l'accès aux routes privées et redirige vers /login si non authentifié.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.restoreSession().pipe(
    map(isAuthenticated => isAuthenticated ? true : router.createUrlTree(['/login']) as UrlTree),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};
