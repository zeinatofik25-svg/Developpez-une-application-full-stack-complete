import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { PostDetailComponent } from './post-detail.component';
import { PostService } from '../../services/post/post.service';
import { CommentService } from '../../services/comment/comment.service';
import { AuthService } from '../../services/auth/auth.service';

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  let postServiceMock: { getPostDetail: jest.Mock };
  let commentServiceMock: { createComment: jest.Mock };

  const validPost = {
    id: 1,
    title: 'Post',
    content: 'Content',
    createdAt: '2026-01-01T00:00:00',
    topic: { id: 1, name: 'Java', description: 'Desc' },
    author: { id: 1, username: 'zeina' },
    comments: []
  };

  beforeEach(async () => {
    postServiceMock = {
      getPostDetail: jest.fn().mockReturnValue(of(validPost))
    };
    commentServiceMock = {
      createComment: jest.fn().mockReturnValue(of({
        id: 4,
        content: 'new comment',
        createdAt: '2026-01-01T00:00:00',
        author: { id: 1, username: 'zeina' }
      }))
    };

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => '1'
              }
            }
          }
        },
        {
          provide: PostService,
          useValue: postServiceMock
        },
        { provide: CommentService, useValue: commentServiceMock },
        { provide: AuthService, useValue: { currentUser$: new BehaviorSubject(null) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PostDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load post on init', () => {
    component.ngOnInit();

    expect(postServiceMock.getPostDetail).toHaveBeenCalledWith(1);
    expect((component as any).post?.id).toBe(1);
    expect((component as any).loading).toBe(false);
  });

  it('should set not found error when route id is invalid', () => {
    const route = TestBed.inject(ActivatedRoute) as any;
    route.snapshot.paramMap.get = () => null;

    component.ngOnInit();

    expect((component as any).loading).toBe(false);
    expect((component as any).error).toBeTruthy();
    expect(postServiceMock.getPostDetail).not.toHaveBeenCalled();
  });

  it('should set loading error when post fetch fails', () => {
    postServiceMock.getPostDetail.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect((component as any).loading).toBe(false);
    expect((component as any).error).toBeTruthy();
  });

  it('should not submit comment when invalid', () => {
    component.submitComment();

    expect(commentServiceMock.createComment).not.toHaveBeenCalled();
  });

  it('should create comment and append it', () => {
    (component as any).post = { ...validPost };
    (component as any).commentForm.setValue({ content: 'new comment' });

    component.submitComment();

    expect(commentServiceMock.createComment).toHaveBeenCalledWith(1, 'new comment');
    expect((component as any).post.comments.length).toBe(1);
    expect((component as any).commentLoading).toBe(false);
  });

  it('should set error when comment creation fails', () => {
    commentServiceMock.createComment.mockReturnValue(throwError(() => new Error('boom')));
    (component as any).post = { ...validPost };
    (component as any).commentForm.setValue({ content: 'new comment' });

    component.submitComment();

    expect((component as any).error).toBeTruthy();
    expect((component as any).commentLoading).toBe(false);
  });
});
