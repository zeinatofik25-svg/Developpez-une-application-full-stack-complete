import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { SIMULATION_ERROR_KEYS } from '../../core/constants/simulation.constants';
import { environment } from '../../../environments/environment';

export interface Topic {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  subscribed: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TopicService {

  private readonly apiUrl = `${environment.apiUrl}/${API_ENDPOINTS.TOPICS}`;

  constructor(private readonly http: HttpClient) {}

  // Retourne tous les thèmes avec le statut d'abonnement utilisateur.
  getTopics(): Observable<Topic[]> {
    if (environment.simulateApiErrors.topicsFetch) {
      return throwError(() => new Error(`Simulation API: ${SIMULATION_ERROR_KEYS.TOPICS_FETCH}`));
    }
    return this.http.get<Topic[]>(this.apiUrl);
  }

  // Crée un abonnement au thème donné.
  subscribe(topicId: number): Observable<Topic> {
    return this.http.post<Topic>(`${this.apiUrl}/${topicId}/subscribe`, {});
  }

  // Supprime l'abonnement au thème donné.
  unsubscribe(topicId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${topicId}/unsubscribe`);
  }
}
