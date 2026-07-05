import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router, provideRouter } from '@angular/router';
import { CreatePostComponent } from './create-post.component';
import { TopicService } from '../../services/topic/topic.service';
import { PostService } from '../../services/post/post.service';

describe('CreatePostComponent', () => {
  let component: CreatePostComponent;
  let fixture: ComponentFixture<CreatePostComponent>;
  let topicServiceMock: { getTopics: jest.Mock };
  let postServiceMock: { createPost: jest.Mock };
  let router: Router;

  beforeEach(async () => {
    topicServiceMock = { getTopics: jest.fn().mockReturnValue(of([])) };
    postServiceMock = { createPost: jest.fn().mockReturnValue(of({ id: 1 })) };

    await TestBed.configureTestingModule({
      imports: [CreatePostComponent],
      providers: [
        provideRouter([]),
        { provide: TopicService, useValue: topicServiceMock },
        { provide: PostService, useValue: postServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreatePostComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    jest.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load topics on init (only subscribed)', () => {
    topicServiceMock.getTopics.mockReturnValue(of([
      { id: 1, name: 'Java', description: '', createdAt: '', subscribed: true },
      { id: 2, name: 'Angular', description: '', createdAt: '', subscribed: false }
    ]));

    component.ngOnInit();

    expect((component as any).topics.length).toBe(1);
    expect((component as any).topics[0].name).toBe('Java');
    expect((component as any).loadingTopics).toBe(false);
  });

  it('should set error when topics loading fails', () => {
    topicServiceMock.getTopics.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect((component as any).error).toBeTruthy();
    expect((component as any).loadingTopics).toBe(false);
  });

  it('should not submit when form is invalid', () => {
    component.submit();

    expect(postServiceMock.createPost).not.toHaveBeenCalled();
  });

  it('should create post and navigate to detail on success', () => {
    (component as any).form.setValue({ topicId: 1, title: 'Title', content: 'Content' });

    component.submit();

    expect(postServiceMock.createPost).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/posts', 1]);
    expect((component as any).saving).toBe(false);
  });

  it('should expose error when post creation fails', () => {
    postServiceMock.createPost.mockReturnValue(throwError(() => new Error('boom')));
    (component as any).form.setValue({ topicId: 1, title: 'Title', content: 'Content' });

    component.submit();

    expect((component as any).error).toBeTruthy();
    expect((component as any).saving).toBe(false);
  });
});
