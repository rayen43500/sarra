import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Certificate {
  id: number;
  code: string;
  title: string;
  holder: string;
  issueDate: string;
  expiryDate: string;
  status: 'ACTIVE' | 'EXPIRED' | 'REVOKED';
  pdfPath: string;
}

export interface CreateCertificatePayload {
  title: string;
  description: string;
  issuedToUserId: number;
  issueDate?: string | null;
  expiryDate?: string | null;
}

@Injectable({ providedIn: 'root' })
export class CertificateService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AuthService
  ) {}

  getClientCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(`${this.apiUrl}/client/certificates`);
  }

  getAdminCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(`${this.apiUrl}/admin/certificates`);
  }

  getCertificatesForCurrentRole(): Observable<Certificate[]> {
    return this.authService.getRole() === 'ROLE_ADMIN'
      ? this.getAdminCertificates()
      : this.getClientCertificates();
  }

  downloadClientPdf(certificateId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/client/certificates/${certificateId}/pdf`, { responseType: 'blob' });
  }

  downloadAdminPdf(certificateId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/admin/certificates/${certificateId}/pdf`, { responseType: 'blob' });
  }

  createAdminCertificate(payload: CreateCertificatePayload): Observable<Certificate> {
    return this.http.post<Certificate>(`${this.apiUrl}/admin/certificates`, payload);
  }

  downloadPdfForCurrentRole(certificateId: number): Observable<Blob> {
    return this.authService.getRole() === 'ROLE_ADMIN'
      ? this.downloadAdminPdf(certificateId)
      : this.downloadClientPdf(certificateId);
  }
}
