import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface VerificationResponse {
  code: string;
  holder: string;
  issueDate: string;
  expiryDate: string;
  status: string;
  valid: boolean;
}

@Injectable({ providedIn: 'root' })
export class VerificationService {
  private readonly apiUrl = 'http://localhost:8080/api/public/verify';

  constructor(private readonly http: HttpClient) {}

  verifyByCode(code: string): Observable<VerificationResponse> {
    return this.http.get<VerificationResponse>(`${this.apiUrl}/${code}`);
  }
}
