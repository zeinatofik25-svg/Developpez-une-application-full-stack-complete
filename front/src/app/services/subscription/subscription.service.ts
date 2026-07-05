import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { environment } from '../../../environments/environment';
import { Topic } from '../topic/topic.service';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly apiUrl = `${environment.apiUrl}/${API_ENDPOINTS.TOPICS}`;

  constructor(private readonly http: HttpClient) {}

  // Abonne l'utilisateur courant à un thème.
  subscribe(topicId: number): Observable<Topic> {
    return this.http.post<Topic>(`${this.apiUrl}/${topicId}/subscribe`, {});
  }

  // Désabonne l'utilisateur courant d'un thème.
  unsubscribe(topicId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${topicId}/unsubscribe`);
  }
}
