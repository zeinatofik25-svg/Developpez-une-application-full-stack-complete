import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { SIMULATION_ERROR_KEYS } from '../../core/constants/simulation.constants';
import { environment } from '../../../environments/environment';

export interface PostSummary {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  topicId: number;
  topicName: string;
  authorId: number;
  authorUsername: string;
}

export interface FeedPageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export interface PostDetail {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  topic: { id: number; name: string; description: string };
  author: { id: number; username: string };
  comments: Array<{
    id: number;
    content: string;
    createdAt: string;
    author: { id: number; username: string };
  }>;
}

export interface CreatePostPayload {
  title: string;
  content: string;
  topicId: number;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private readonly apiUrl = `${environment.apiUrl}/${API_ENDPOINTS.POSTS}`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Charge une page du feed des articles avec tri par date.
   *
   * @param sort ordre chronologique newest/oldest
   * @param page index de page (base 0)
   * @param size taille de page
   * @returns page de résultats pour le scroll infini
   */
  getFeed(sort: 'newest' | 'oldest' = 'newest', page = 0, size = 10): Observable<FeedPageResponse<PostSummary>> {
    if (environment.simulateApiErrors.feedFetch) {
      return throwError(() => new Error(`Simulation API: ${SIMULATION_ERROR_KEYS.FEED_FETCH}`));
    }
    return this.http.get<FeedPageResponse<PostSummary>>(`${this.apiUrl}/feed`, {
      params: { sort, page, size }
    });
  }

  /**
   * Récupère le détail d'un article avec ses commentaires.
   *
   * @param postId identifiant du post
   * @returns détail complet du post
   */
  getPostDetail(postId: number): Observable<PostDetail> {
    if (environment.simulateApiErrors.postDetailFetch) {
      return throwError(() => new Error(`Simulation API: ${SIMULATION_ERROR_KEYS.POST_DETAIL_FETCH}`));
    }
    return this.http.get<PostDetail>(`${this.apiUrl}/${postId}`);
  }

  /**
   * Crée un nouvel article pour l'utilisateur connecté.
   *
   * @param payload données de création
   * @returns détail de l'article créé
   */
  createPost(payload: CreatePostPayload): Observable<PostDetail> {
    return this.http.post<PostDetail>(this.apiUrl, payload);
  }
}
