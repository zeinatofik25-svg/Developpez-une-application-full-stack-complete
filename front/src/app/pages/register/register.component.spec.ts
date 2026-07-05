import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceMock: { register: jest.Mock };
  let router: Router;

  beforeEach(async () => {
    authServiceMock = { register: jest.fn().mockReturnValue(of({})) };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not submit when form is invalid', () => {
    component.submit();

    expect(authServiceMock.register).not.toHaveBeenCalled();
  });

  it('should register and navigate to feed', () => {
    (component as any).form.setValue({ email: 'z@a.com', username: 'zeina', password: 'Zeina1!' });

    component.submit();

    expect(authServiceMock.register).toHaveBeenCalledWith({
      email: 'z@a.com',
      username: 'zeina',
      password: 'Zeina1!'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/feed']);
    expect((component as any).loading).toBe(false);
  });

  it('should set error when register fails', () => {
    authServiceMock.register.mockReturnValue(throwError(() => new Error('fail')));
    (component as any).form.setValue({ email: 'z@a.com', username: 'zeina', password: 'Zeina1!' });

    component.submit();

    expect((component as any).error).toBeTruthy();
    expect((component as any).loading).toBe(false);
  });
});
