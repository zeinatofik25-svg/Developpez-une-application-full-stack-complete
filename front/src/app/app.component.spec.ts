import { TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth/auth.service';

describe('AppComponent', () => {
  const authServiceMock = {
    currentUser$: new BehaviorSubject(null),
    logout: jest.fn(),
    restoreSession: jest.fn().mockReturnValue({ subscribe: jest.fn() })
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        AppComponent,
      ],
      providers: [
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    authServiceMock.logout.mockClear();
    authServiceMock.restoreSession.mockClear();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the app shell', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.app-shell')).toBeTruthy();
  });

  it('should render router outlet shell', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('should call logout on auth service', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    const router = TestBed.inject(Router);
    const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    app.logout();

    expect(authServiceMock.logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should detect auth pages from router url', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance as any;
    const router = TestBed.inject(Router);

    Object.defineProperty(router, 'url', {
      configurable: true,
      get: () => '/login?next=/feed#top'
    });
    expect(app.isAuthPage()).toBe(true);

    Object.defineProperty(router, 'url', {
      configurable: true,
      get: () => '/feed'
    });
    expect(app.isAuthPage()).toBe(false);
  });

  it('should close mobile menu on resize when width is above 640', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance as any;

    app.mobileMenuOpen = true;
    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      value: 1024
    });

    app.onResize();

    expect(app.mobileMenuOpen).toBe(false);
  });

  it('should keep mobile menu state on resize when width is 640 or below', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance as any;

    app.mobileMenuOpen = true;
    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      value: 640
    });

    app.onResize();

    expect(app.mobileMenuOpen).toBe(true);
  });
});
