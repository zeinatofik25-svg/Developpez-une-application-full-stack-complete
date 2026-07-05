import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Observable } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { UserPageComponent } from './user-page.component';
import { AuthService } from '../../services/auth/auth.service';
import { TopicService } from '../../services/topic/topic.service';
import { SubscriptionService } from '../../services/subscription/subscription.service';

describe('UserPageComponent', () => {
  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;

  let authServiceMock: { currentUser$: Observable<{ userId: number; username: string; email: string }>; updateProfile: jest.Mock };
  let topicServiceMock: { getTopics: jest.Mock };
  let subscriptionServiceMock: { unsubscribe: jest.Mock };

  beforeEach(async () => {
    authServiceMock = {
      currentUser$: of({ userId: 1, username: 'zeina', email: 'zeina@mail.test' }),
      updateProfile: jest.fn().mockReturnValue(of({
        token: 'jwt',
        userId: 1,
        username: 'zeina',
        email: 'zeina@mail.test'
      }))
    };

    topicServiceMock = {
      getTopics: jest.fn().mockReturnValue(of([
        { id: 1, name: 'Java', description: 'desc', createdAt: '2026-01-01', subscribed: true },
        { id: 2, name: 'Angular', description: 'desc', createdAt: '2026-01-01', subscribed: false }
      ]))
    };

    subscriptionServiceMock = {
      unsubscribe: jest.fn().mockReturnValue(of(undefined))
    };

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, UserPageComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: TopicService, useValue: topicServiceMock },
        { provide: SubscriptionService, useValue: subscriptionServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load only subscribed topics', () => {
    expect((component as any).subscribedTopics).toHaveLength(1);
    expect((component as any).subscribedTopics[0].name).toBe('Java');
  });

  it('should submit profile update without empty password', () => {
    (component as any).form.setValue({
      username: 'new-name',
      email: 'new@mail.test',
      password: ''
    });

    component.submit();

    expect(authServiceMock.updateProfile).toHaveBeenCalledWith({
      username: 'new-name',
      email: 'new@mail.test'
    });
  });

  it('should set error when profile update fails', () => {
    authServiceMock.updateProfile.mockReturnValue(throwError(() => new Error('boom')));
    (component as any).form.setValue({
      username: 'new-name',
      email: 'new@mail.test',
      password: ''
    });

    component.submit();

    expect((component as any).error).toBeTruthy();
  });

  it('should unsubscribe from topic', () => {
    component.unsubscribe((component as any).subscribedTopics[0]);

    expect(subscriptionServiceMock.unsubscribe).toHaveBeenCalledWith(1);
    expect((component as any).subscribedTopics).toHaveLength(0);
  });
});
