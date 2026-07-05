import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CommentService } from './comment.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';

describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;
  const baseApiUrl = `${environment.apiUrl}/${API_ENDPOINTS.POSTS}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a comment for a post', () => {
    service.createComment(10, 'bonjour').subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/10/comments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'bonjour' });
    req.flush({ id: 1, content: 'bonjour' });
  });
});
