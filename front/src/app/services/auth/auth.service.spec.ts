import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { take } from 'rxjs';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const baseApiUrl = `${environment.apiUrl}/${API_ENDPOINTS.AUTH}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should restore session from backend me endpoint', () => {
    let restored = false;
    service.restoreSession().pipe(take(1)).subscribe(value => {
      restored = value;
    });

    const req = httpMock.expectOne(`${baseApiUrl}/me`);
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush({ userId: 1, username: 'john', email: 'john@mail.test' });

    expect(restored).toBe(true);
  });

  it('should login and update current user', () => {
    let currentUser: any = null;
    service.currentUser$.subscribe(user => {
      currentUser = user;
    });

    service.login({ identifier: 'john', password: 'secret' }).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush({
      token: 'jwt-token',
      userId: 1,
      username: 'john',
      email: 'john@mail.test'
    });

    expect(currentUser).toEqual({ userId: 1, username: 'john', email: 'john@mail.test' });
  });

  it('should register and update current user', () => {
    let currentUser: any = null;
    service.currentUser$.subscribe(user => {
      currentUser = user;
    });

    service.register({ email: 'a@b.c', username: 'zeina', password: '123456' }).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush({
      token: 'jwt-2',
      userId: 2,
      username: 'zeina',
      email: 'a@b.c'
    });

    expect(currentUser).toEqual({ userId: 2, username: 'zeina', email: 'a@b.c' });
  });

  it('should update profile and refresh current user', () => {
    let currentUser: any = null;
    service.currentUser$.subscribe(user => {
      currentUser = user;
    });

    service.updateProfile({ email: 'new@b.c', username: 'new-name' }).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.withCredentials).toBe(true);
    req.flush({
      token: 'jwt-3',
      userId: 2,
      username: 'new-name',
      email: 'new@b.c'
    });

    expect(currentUser).toEqual({ userId: 2, username: 'new-name', email: 'new@b.c' });
  });

  it('should logout and clear current user', () => {
    let currentUser: any = { userId: 1, username: 'u', email: 'e' };
    service.currentUser$.subscribe(user => {
      currentUser = user;
    });

    service.logout();

    const req = httpMock.expectOne(`${baseApiUrl}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.withCredentials).toBe(true);
    req.flush(null);

    expect(currentUser).toBeNull();
  });

  it('should not restore session when backend rejects it', () => {
    let restored = true;
    service.restoreSession().pipe(take(1)).subscribe(value => {
      restored = value;
    });

    const req = httpMock.expectOne(`${baseApiUrl}/me`);
    req.flush({ message: 'unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(restored).toBe(false);
  });
});
