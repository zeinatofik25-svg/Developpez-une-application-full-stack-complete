import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, finalize, map, of, shareReplay, tap } from 'rxjs';
import { API_ENDPOINTS } from '../../core/constants/api.constants';
import { environment } from '../../../environments/environment';

export interface AuthUser {
  userId: number;
  username: string;
  email: string;
}

interface AuthResponse extends AuthUser {
}

interface LoginPayload {
  identifier: string;
  password: string;
}

interface RegisterPayload {
  email: string;
  username: string;
  password: string;
}

export interface UpdateProfilePayload {
  email: string;
  username: string;
  password?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/${API_ENDPOINTS.AUTH}`;
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  private restoreSessionRequest?: Observable<boolean>;

  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  /**
   * Authentifie l'utilisateur et synchronise le profil en mémoire.
   *
   * @param payload identifiants de connexion
   * @returns réponse d'authentification
   */
  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload, { withCredentials: true }).pipe(
      tap(response => this.syncCurrentUser(response))
    );
  }

  /**
   * Crée un compte puis synchronise la session comme un login direct.
   *
   * @param payload données d'inscription
   * @returns réponse d'authentification
   */
  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload, { withCredentials: true }).pipe(
      tap(response => this.syncCurrentUser(response))
    );
  }

  /**
   * Met à jour le profil et resynchronise les infos utilisateur en mémoire.
   *
   * @param payload données de mise à jour du profil
   * @returns réponse d'authentification mise à jour
   */
  updateProfile(payload: UpdateProfilePayload): Observable<AuthResponse> {
    return this.http.put<AuthResponse>(`${this.apiUrl}/me`, payload, { withCredentials: true }).pipe(
      tap(response => this.syncCurrentUser(response))
    );
  }

  /**
   * Ferme la session côté backend puis réinitialise l'état local.
   */
  logout(): void {
    this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      error: () => undefined
    });
    this.currentUserSubject.next(null);
  }

  /**
   * Recharge l'utilisateur connecté depuis le cookie HttpOnly si nécessaire.
   *
   * @returns true si la session est restaurée, sinon false
   */
  restoreSession(): Observable<boolean> {
    if (this.currentUserSubject.value) {
      return of(true);
    }

    this.restoreSessionRequest ??= this.http.get<AuthUser>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
        tap(user => this.currentUserSubject.next(user)),
        map(() => true),
        catchError(() => {
          this.currentUserSubject.next(null);
          return of(false);
        }),
        finalize(() => {
          this.restoreSessionRequest = undefined;
        }),
        shareReplay(1)
      );

    return this.restoreSessionRequest;
  }

  /**
   * Met à jour l'utilisateur courant en mémoire.
   *
   * @param response données utilisateur renvoyées par l'API
   */
  private syncCurrentUser(response: AuthUser): void {
    const user: AuthUser = {
      userId: response.userId,
      username: response.username,
      email: response.email
    };

    this.currentUserSubject.next(user);
  }
}
