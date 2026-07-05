import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SubscriptionService } from './subscription.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';

describe('SubscriptionService', () => {
  let service: SubscriptionService;
  let httpMock: HttpTestingController;
  const baseApiUrl = `${environment.apiUrl}/${API_ENDPOINTS.TOPICS}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(SubscriptionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should subscribe to topic', () => {
    service.subscribe(5).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/5/subscribe`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ id: 5 });
  });

  it('should unsubscribe from topic', () => {
    service.unsubscribe(5).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/5/unsubscribe`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
