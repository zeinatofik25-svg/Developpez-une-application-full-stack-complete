import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { environment } from '../../../environments/environment';

export interface CommentResponse {
  id: number;
  content: string;
  createdAt: string;
  author: { id: number; username: string };
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private readonly apiUrl = `${environment.apiUrl}/${API_ENDPOINTS.POSTS}`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Crée un commentaire sur l'article ciblé.
   *
   * @param postId identifiant de l'article
   * @param content contenu du commentaire
   * @returns commentaire créé
   */
  createComment(postId: number, content: string): Observable<CommentResponse> {
    return this.http.post<CommentResponse>(`${this.apiUrl}/${postId}/comments`, { content });
  }
}
