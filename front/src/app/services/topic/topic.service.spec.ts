import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TopicService } from './topic.service';
import { environment } from '../../../environments/environment';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { SIMULATION_ERROR_KEYS } from '../../core/constants/simulation.constants';

describe('TopicService', () => {
  let service: TopicService;
  let httpMock: HttpTestingController;
  const baseApiUrl = `${environment.apiUrl}/${API_ENDPOINTS.TOPICS}`;

  beforeEach(() => {
    environment.simulateApiErrors.topicsFetch = false;

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });

    service = TestBed.inject(TopicService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    environment.simulateApiErrors.topicsFetch = false;
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch topics', () => {
    let result: unknown;
    service.getTopics().subscribe(value => {
      result = value;
    });

    const req = httpMock.expectOne(baseApiUrl);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, name: 'Java' }]);

    expect(result).toEqual([{ id: 1, name: 'Java' }]);
  });

  it('should return simulated error when topics fetch simulation is enabled', () => {
    environment.simulateApiErrors.topicsFetch = true;

    let errorMessage = '';
    service.getTopics().subscribe({
      error: (error: Error) => {
        errorMessage = error.message;
      }
    });

    expect(errorMessage).toContain(SIMULATION_ERROR_KEYS.TOPICS_FETCH);
  });

  it('should subscribe to a topic', () => {
    service.subscribe(7).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/7/subscribe`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ id: 7 });
  });

  it('should unsubscribe from a topic', () => {
    service.unsubscribe(7).subscribe();

    const req = httpMock.expectOne(`${baseApiUrl}/7/unsubscribe`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
