import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ActiveExam, ExamQuestion, ResultResponse } from './exam-client.service';

export interface GenerateQuizPayload {
  topic: string;
  questionCount: number;
  difficulty: string;
  durationMinutes: number;
  passScore: number;
  isActive: boolean;
}

export interface GeneratedQuizResponse {
  exam: ActiveExam;
  questions: ExamQuestion[];
  source: string;
}

export interface RevisionDocumentResponse {
  examId: number;
  title: string;
  url: string;
}

@Injectable({ providedIn: 'root' })
export class AdminQuizService {
  private readonly apiUrl = 'http://localhost:8080/api/admin';

  constructor(private readonly http: HttpClient) {}

  listExams(): Observable<ActiveExam[]> {
    return this.http.get<ActiveExam[]>(`${this.apiUrl}/exams`);
  }

  generateQuiz(payload: GenerateQuizPayload): Observable<GeneratedQuizResponse> {
    return this.http.post<GeneratedQuizResponse>(`${this.apiUrl}/exams/generate-quiz`, payload);
  }

  uploadRevisionDocument(examId: number, title: string, file: File): Observable<RevisionDocumentResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    return this.http.post<RevisionDocumentResponse>(`${this.apiUrl}/exams/${examId}/revision-document`, formData);
  }

  getResults(examId: number): Observable<ResultResponse[]> {
    return this.http.get<ResultResponse[]>(`${this.apiUrl}/results/${examId}`);
  }

  assetUrl(url: string | null | undefined): string {
    if (!url) {
      return '';
    }
    return url.startsWith('http') ? url : `http://localhost:8080${url}`;
  }
}
