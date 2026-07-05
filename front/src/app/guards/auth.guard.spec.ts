import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { firstValueFrom, of } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth/auth.service';

describe('authGuard', () => {
  it('returns true when user is authenticated', async () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { restoreSession: () => of(true) } },
        { provide: Router, useValue: { createUrlTree: jest.fn() } }
      ]
    });

    const result = await firstValueFrom(TestBed.runInInjectionContext(() => authGuard({} as any, {} as any)) as any);

    expect(result).toBe(true);
  });

  it('returns login UrlTree when user is not authenticated', async () => {
    const createUrlTree = jest.fn().mockReturnValue('redirect-tree');

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { restoreSession: () => of(false) } },
        { provide: Router, useValue: { createUrlTree } }
      ]
    });

    const result = await firstValueFrom(TestBed.runInInjectionContext(() => authGuard({} as any, {} as any)) as any);

    expect(createUrlTree).toHaveBeenCalledWith(['/login']);
    expect(result).toBe('redirect-tree');
  });
});
