import { HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { authInterceptor } from './auth.interceptor';
import { environment } from '../../environments/environment';

describe('authInterceptor', () => {
  it('enables credentials on API requests', done => {
    const request = new HttpRequest('GET', `${environment.apiUrl}/test`);

    TestBed.runInInjectionContext(() => {
      authInterceptor(request, req => {
        expect(req.withCredentials).toBe(true);
        return of({} as any);
      }).subscribe(() => {
        done();
      });
    });
  });

  it('leaves non-api requests untouched', done => {
    const request = new HttpRequest('GET', '/assets/file.json');

    TestBed.runInInjectionContext(() => {
      authInterceptor(request, req => {
        expect(req.withCredentials).toBe(false);
        return of({} as any);
      }).subscribe(() => {
        done();
      });
    });
  });
});
