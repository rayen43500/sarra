import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: 'ROLE_ADMIN' | 'ROLE_CLIENT';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';
  private readonly tokenKey = 'cert_token';
  private readonly roleKey = 'cert_role';
  private readonly emailKey = 'cert_email';

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response) => this.storeSession(response))
    );
  }

  register(payload: RegisterPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, payload).pipe(
      tap((response) => this.storeSession(response))
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.roleKey);
    sessionStorage.removeItem(this.emailKey);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    const payload = this.decodePayload(token);
    const exp = payload?.['exp'];
    if (typeof exp !== 'number') {
      return false;
    }

    return exp * 1000 > Date.now();
  }

  getRole(): string {
    return sessionStorage.getItem(this.roleKey) ?? '';
  }

  getEmail(): string {
    return sessionStorage.getItem(this.emailKey) ?? '';
  }

  getToken(): string {
    return sessionStorage.getItem(this.tokenKey) ?? '';
  }

  setSession(response: AuthResponse): void {
    this.storeSession(response);
  }

  private storeSession(response: AuthResponse): void {
    sessionStorage.setItem(this.tokenKey, response.token);
    sessionStorage.setItem(this.roleKey, response.role);
    sessionStorage.setItem(this.emailKey, response.email);
  }

  private decodePayload(token: string): Record<string, unknown> | null {
    try {
      const base64 = token.split('.')[1];
      if (!base64) {
        return null;
      }
      const normalized = base64.replace(/-/g, '+').replace(/_/g, '/');
      const json = atob(normalized);
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
