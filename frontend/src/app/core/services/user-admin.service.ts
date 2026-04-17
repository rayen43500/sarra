import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface AdminUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  status: 'ACTIVE' | 'BLOCKED';
  roles: string[];
}

export interface CreateAdminUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  role: 'ROLE_ADMIN' | 'ROLE_CLIENT';
  status: 'ACTIVE' | 'BLOCKED';
}

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private readonly apiUrl = 'http://localhost:8080/api/admin/users';

  constructor(private readonly http: HttpClient) {}

  list(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.apiUrl);
  }

  create(payload: CreateAdminUserPayload): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.apiUrl, payload);
  }

  assignRole(id: number, role: 'ROLE_ADMIN' | 'ROLE_CLIENT'): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/role?role=${role}`, {});
  }

  updateStatus(id: number, status: 'ACTIVE' | 'BLOCKED'): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
