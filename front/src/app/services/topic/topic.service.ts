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

  /**
   * Retourne tous les thèmes avec le statut d'abonnement utilisateur.
   *
   * @returns liste des thèmes
   */
  getTopics(): Observable<Topic[]> {
    if (environment.simulateApiErrors.topicsFetch) {
      return throwError(() => new Error(`Simulation API: ${SIMULATION_ERROR_KEYS.TOPICS_FETCH}`));
    }
    return this.http.get<Topic[]>(this.apiUrl);
  }
}
