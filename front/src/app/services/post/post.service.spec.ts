import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PostService } from './post.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { SIMULATION_ERROR_KEYS } from '../../core/constants/simulation.constants';

describe('PostService', () => {
  let service: PostService;
  let httpMock: HttpTestingController;
  const baseApiUrl = `${environment.apiUrl}/${API_ENDPOINTS.POSTS}`;

  beforeEach(() => {
    environment.simulateApiErrors.feedFetch = false;
    environment.simulateApiErrors.postDetailFetch = false;

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    environment.simulateApiErrors.feedFetch = false;
    environment.simulateApiErrors.postDetailFetch = false;
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch feed with newest sort by default', () => {
    let result: unknown;
    service.getFeed().subscribe(value => {
      result = value;
    });

    const req = httpMock.expectOne(`${baseApiUrl}/feed?sort=newest&page=0&size=10`);
    expect(req.request.method).toBe('GET');
    req.flush({ items: [{ id: 1 }], page: 0, size: 10, totalElements: 1, totalPages: 1, hasNext: false });

    expect(result).toEqual({ items: [{ id: 1 }], page: 0, size: 10, totalElements: 1, totalPages: 1, hasNext: false });
  });

  it('should fetch feed with explicit sort', () => {
    service.getFeed('oldest', 2, 5).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/feed?sort=oldest&page=2&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush({ items: [], page: 2, size: 5, totalElements: 0, totalPages: 0, hasNext: false });
  });

  it('should return simulated error for feed', () => {
    environment.simulateApiErrors.feedFetch = true;

    let errorMessage = '';
    service.getFeed().subscribe({
      error: (error: Error) => {
        errorMessage = error.message;
      }
    });

    expect(errorMessage).toContain(SIMULATION_ERROR_KEYS.FEED_FETCH);
  });

  it('should fetch post detail', () => {
    let result: unknown;
    service.getPostDetail(12).subscribe(value => {
      result = value;
    });

    const req = httpMock.expectOne(`${baseApiUrl}/12`);
    expect(req.request.method).toBe('GET');
    req.flush({ id: 12, comments: [] });

    expect(result).toEqual({ id: 12, comments: [] });
  });

  it('should return simulated error for post detail', () => {
    environment.simulateApiErrors.postDetailFetch = true;

    let errorMessage = '';
    service.getPostDetail(1).subscribe({
      error: (error: Error) => {
        errorMessage = error.message;
      }
    });

    expect(errorMessage).toContain(SIMULATION_ERROR_KEYS.POST_DETAIL_FETCH);
  });

  it('should create post', () => {
    service.createPost({ title: 'T', content: 'C', topicId: 2 }).subscribe();

    const req = httpMock.expectOne(baseApiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'T', content: 'C', topicId: 2 });
    req.flush({ id: 99 });
  });
});
