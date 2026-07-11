import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { TopicsPageComponent } from './topics-page.component';
import { TopicService } from '../../services/topic/topic.service';
import { SubscriptionService } from '../../services/subscription/subscription.service';
import { AuthService } from '../../services/auth/auth.service';

describe('TopicsPageComponent', () => {
  let component: TopicsPageComponent;
  let fixture: ComponentFixture<TopicsPageComponent>;
  let topicServiceMock: { getTopics: jest.Mock };
  let subscriptionServiceMock: { subscribe: jest.Mock; unsubscribe: jest.Mock };
  const topic = {
    id: 1,
    name: 'Java',
    description: 'Desc',
    createdAt: '2026-01-01T00:00:00',
    subscribed: false
  };

  beforeEach(async () => {
    topicServiceMock = { getTopics: jest.fn().mockReturnValue(of([])) };
    subscriptionServiceMock = {
      subscribe: jest.fn().mockReturnValue(of({ ...topic, subscribed: true })),
      unsubscribe: jest.fn().mockReturnValue(of(undefined))
    };

    await TestBed.configureTestingModule({
      imports: [TopicsPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: TopicService,
          useValue: topicServiceMock
        },
        {
          provide: SubscriptionService,
          useValue: subscriptionServiceMock
        },
        {
          provide: AuthService,
          useValue: { currentUser$: new BehaviorSubject(null) }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TopicsPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load topics on init', () => {
    topicServiceMock.getTopics.mockReturnValue(of([topic]));

    component.ngOnInit();

    expect((component as any).topics.length).toBe(1);
    expect((component as any).pagedTopics.length).toBe(1);
    expect((component as any).loading).toBe(false);
  });

  it('should paginate topics by 4 items per page', () => {
    const topics = Array.from({ length: 9 }).map((_, index) => ({
      id: index + 1,
      name: `Topic ${index + 1}`,
      description: 'Desc',
      createdAt: '2026-01-01T00:00:00',
      subscribed: false
    }));

    topicServiceMock.getTopics.mockReturnValue(of(topics));

    component.ngOnInit();
    expect((component as any).pagedTopics).toHaveLength(4);

    component.loadNextPage();
    expect((component as any).pagedTopics).toHaveLength(4);

    component.loadNextPage();
    expect((component as any).pagedTopics).toHaveLength(1);
  });

  it('should set error when topics loading fails', () => {
    topicServiceMock.getTopics.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect((component as any).loading).toBe(false);
    expect((component as any).error).toBeTruthy();
  });

  it('should subscribe when topic is not subscribed', () => {
    (component as any).topics = [{ ...topic, subscribed: false }];

    component.toggleSubscription((component as any).topics[0]);

    expect(subscriptionServiceMock.subscribe).toHaveBeenCalledWith(1);
    expect((component as any).topics[0].subscribed).toBe(true);
  });

  it('should set error when subscribe fails', () => {
    subscriptionServiceMock.subscribe.mockReturnValue(throwError(() => new Error('boom')));
    (component as any).topics = [{ ...topic, subscribed: false }];

    component.toggleSubscription((component as any).topics[0]);

    expect((component as any).error).toBeTruthy();
  });

  it('should unsubscribe when topic is subscribed', () => {
    (component as any).topics = [{ ...topic, subscribed: true }];

    component.toggleSubscription((component as any).topics[0]);

    expect(subscriptionServiceMock.unsubscribe).toHaveBeenCalledWith(1);
    expect((component as any).topics[0].subscribed).toBe(false);
  });

  it('should set error when unsubscribe fails', () => {
    subscriptionServiceMock.unsubscribe.mockReturnValue(throwError(() => new Error('boom')));
    (component as any).topics = [{ ...topic, subscribed: true }];

    component.toggleSubscription((component as any).topics[0]);

    expect((component as any).error).toBeTruthy();
  });
});
