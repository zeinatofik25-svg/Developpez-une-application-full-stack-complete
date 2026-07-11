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
    postServiceMock = {
      getFeed: jest.fn().mockReturnValue(of({ items: [], page: 0, size: 4, totalElements: 0, totalPages: 0, hasNext: false }))
    };

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
    const response = { items: [{ id: 1, title: 'T' }], page: 0, size: 4, totalElements: 1, totalPages: 1, hasNext: false };
    postServiceMock.getFeed.mockReturnValue(of(response));

    component.ngOnInit();

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('newest', 0, 4);
    expect((component as any).posts).toEqual(response.items);
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

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('oldest', 0, 4);
  });

  it('should reload feed with newest when toggling sort twice', () => {
    component.toggleSort();
    component.toggleSort();

    expect(postServiceMock.getFeed).toHaveBeenCalledWith('newest', 0, 4);
  });

  it('should load next page when clicking next', () => {
    postServiceMock.getFeed
      .mockReturnValueOnce(of({ items: [{ id: 1, title: 'T1' }], page: 0, size: 4, totalElements: 8, totalPages: 2, hasNext: true }))
      .mockReturnValueOnce(of({ items: [{ id: 2, title: 'T2' }], page: 1, size: 4, totalElements: 8, totalPages: 2, hasNext: false }));

    component.ngOnInit();
    component.loadNextPage();

    expect(postServiceMock.getFeed).toHaveBeenLastCalledWith('newest', 1, 4);
    expect((component as any).posts).toEqual([{ id: 2, title: 'T2' }]);
  });

  it('should load previous page when possible', () => {
    postServiceMock.getFeed
      .mockReturnValueOnce(of({ items: [{ id: 1, title: 'T1' }], page: 0, size: 4, totalElements: 8, totalPages: 2, hasNext: true }))
      .mockReturnValueOnce(of({ items: [{ id: 2, title: 'T2' }], page: 1, size: 4, totalElements: 8, totalPages: 2, hasNext: false }))
      .mockReturnValueOnce(of({ items: [{ id: 1, title: 'T1' }], page: 0, size: 4, totalElements: 8, totalPages: 2, hasNext: true }));

    component.ngOnInit();
    component.loadNextPage();
    component.loadPreviousPage();

    expect(postServiceMock.getFeed).toHaveBeenLastCalledWith('newest', 0, 4);
  });
});
