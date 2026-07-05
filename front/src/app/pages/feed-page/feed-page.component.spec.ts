import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { FeedPageComponent } from './feed-page.component';
import { PostService } from '../../services/post/post.service';

describe('FeedPageComponent', () => {
  let component: FeedPageComponent;
  let fixture: ComponentFixture<FeedPageComponent>;
  let postServiceMock: { getFeed: jest.Mock };

  beforeEach(async () => {
    postServiceMock = { getFeed: jest.fn().mockReturnValue(of([])) };

    await TestBed.configureTestingModule({
      imports: [FeedPageComponent],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(FeedPageComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load feed on init', () => {
    const posts = [{ id: 1, title: 'T' }];
    postServiceMock.getFeed.mockReturnValue(of(posts));

    component.ngOnInit();

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('newest');
    expect((component as any).posts).toEqual(posts);
    expect((component as any).loading).toBe(false);
  });

  it('should set error when load feed fails', () => {
    postServiceMock.getFeed.mockReturnValue(throwError(() => new Error('boom')));

    component.ngOnInit();

    expect((component as any).error).toBeTruthy();
    expect((component as any).loading).toBe(false);
  });

  it('should reload feed with oldest when toggling sort once', () => {
    component.toggleSort();

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('oldest');
  });

  it('should reload feed with newest when toggling sort twice', () => {
    component.toggleSort();
    component.toggleSort();

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('newest');
  });
});
