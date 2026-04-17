import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface VerificationResponse {
  code: string;
  holder: string;
  issueDate: string | null;
  expiryDate: string | null;
  status: string;
  valid: boolean;
}

export interface ReactionSummary {
  targetType: string;
  targetId: number;
  likes: number;
  dislikes: number;
  myReaction: boolean | null;
}

export interface Rating {
  id: number;
  userId: number;
  targetType: string;
  targetId: number;
  score: number;
  comment: string;
  createdAt: string;
}

export interface RatingSummary {
  targetType: string;
  targetId: number;
  average: number;
  total: number;
  latestRatings: Rating[];
}

export interface PaymentResponse {
  id: number;
  reference: string;
  status: string;
  amount: number;
  currency: string;
  provider: string;
  createdAt: string;
}

export interface Appointment {
  id: number;
  userId: number;
  scheduledAt: string;
  note: string;
  status: string;
  createdAt: string;
}

export interface SocialProviders {
  facebookAuthUrl: string;
  linkedInAuthUrl: string;
  note: string;
}

@Injectable({ providedIn: 'root' })
export class FeatureApiService {
  private readonly api = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  verifyCertificate(code: string): Observable<VerificationResponse> {
    return this.http.get<VerificationResponse>(`${this.api}/public/verify/${encodeURIComponent(code)}`);
  }

  subscribeNewsletter(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.api}/public/newsletter/subscribe`, { email });
  }

  getSocialProviders(): Observable<SocialProviders> {
    return this.http.get<SocialProviders>(`${this.api}/public/social/providers`);
  }

  react(targetType: string, targetId: number, liked: boolean): Observable<ReactionSummary> {
    return this.http.post<ReactionSummary>(`${this.api}/client/features/reactions`, {
      targetType,
      targetId,
      liked
    });
  }

  reactionSummary(targetType: string, targetId: number): Observable<ReactionSummary> {
    return this.http.get<ReactionSummary>(`${this.api}/client/features/reactions`, {
      params: { targetType, targetId }
    });
  }

  rate(targetType: string, targetId: number, score: number, comment: string): Observable<Rating> {
    return this.http.post<Rating>(`${this.api}/client/features/ratings`, {
      targetType,
      targetId,
      score,
      comment
    });
  }

  ratingSummary(targetType: string, targetId: number): Observable<RatingSummary> {
    return this.http.get<RatingSummary>(`${this.api}/client/features/ratings`, {
      params: { targetType, targetId }
    });
  }

  createPayment(amount: number, currency: string, description: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.api}/client/features/payments`, {
      amount,
      currency,
      description
    });
  }

  createAppointment(scheduledAt: string, note: string): Observable<Appointment> {
    return this.http.post<Appointment>(`${this.api}/client/features/appointments`, {
      scheduledAt,
      note
    });
  }

  myAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.api}/client/features/appointments`);
  }

  adminAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.api}/admin/features/appointments`);
  }

  updateAppointmentStatus(id: number, status: string): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.api}/admin/features/appointments/${id}/status`, null, {
      params: { status }
    });
  }

  runNewsletterBatch(): Observable<string> {
    return this.http.post(`${this.api}/admin/features/batch/newsletter-digest/run`, {}, { responseType: 'text' });
  }

  sendEmail(to: string, subject: string, body: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(`${this.api}/client/features/email`, { to, subject, body });
  }

  sendSms(to: string, message: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(`${this.api}/client/features/sms`, { to, message });
  }

  certificatesRssUrl(): string {
    return `${this.api}/public/rss/certificates`;
  }

  certificatesRss(): Observable<string> {
    return this.http.get(this.certificatesRssUrl(), {
      responseType: 'text',
      headers: new HttpHeaders({ Accept: 'application/xml' })
    });
  }
}
