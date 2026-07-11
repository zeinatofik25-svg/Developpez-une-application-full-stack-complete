import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

/**
 * Active l'envoi des cookies HTTPOnly sur les requêtes API du backend.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }

  return next(req.clone({
    withCredentials: true
  }));
};
