import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: { login: jest.Mock };
  let router: Router;

  beforeEach(async () => {
    authServiceMock = { login: jest.fn().mockReturnValue(of({})) };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.submit();

    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('should login and navigate to topics', () => {
    (component as any).form.setValue({ identifier: 'zeina', password: '123456' });

    component.submit();

    expect(authServiceMock.login).toHaveBeenCalledWith({ identifier: 'zeina', password: '123456' });
    expect(router.navigate).toHaveBeenCalledWith(['/topics']);
    expect((component as any).loading).toBe(false);
  });

  it('should set error when login fails', () => {
    authServiceMock.login.mockReturnValue(throwError(() => new Error('bad')));
    (component as any).form.setValue({ identifier: 'zeina', password: 'bad' });

    component.submit();

    expect((component as any).error).toBeTruthy();
    expect((component as any).loading).toBe(false);
  });
});
