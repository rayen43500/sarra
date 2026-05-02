import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SubmitResultPayload {
  examId: number;
  score: number;
  maxScore: number;
  attemptNumber: number;
}

export interface ActiveExam {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  passScore: number;
  isActive: boolean;
  revisionDocumentTitle: string | null;
  revisionDocumentUrl: string | null;
}

export interface ExamQuestionOption {
  id: number;
  text: string;
}

export interface ExamQuestion {
  id: number;
  text: string;
  points: number;
  orderIndex: number;
  options: ExamQuestionOption[];
}

export interface SubmitExamAnswerItem {
  questionId: number;
  optionId: number;
}

export interface SubmitExamAnswersPayload {
  examId: number;
  attemptNumber: number;
  answers: SubmitExamAnswerItem[];
  fraud?: FraudSignals;
}

export interface FraudSignals {
  focusLossCount: number;
  copyPasteCount: number;
  fullscreenExitCount: number;
  totalSeconds: number;
  remainingSeconds: number;
}

export interface ResultResponse {
  id: number;
  examId: number;
  examTitle: string;
  score: number;
  maxScore: number;
  percentage: number;
  passed: boolean;
  submittedAt: string;
  fraudScore: number;
  fraudSuspicious: boolean;
  fraudReason: string;
}

@Injectable({ providedIn: 'root' })
export class ExamClientService {
  private readonly apiUrl = 'http://localhost:8080/api/client';

  constructor(private readonly http: HttpClient) {}

  submitResult(payload: SubmitResultPayload): Observable<ResultResponse> {
    return this.http.post<ResultResponse>(`${this.apiUrl}/results`, payload);
  }

  getActiveExams(): Observable<ActiveExam[]> {
    return this.http.get<ActiveExam[]>(`${this.apiUrl}/exams`);
  }

  getExamQuestions(examId: number): Observable<ExamQuestion[]> {
    return this.http.get<ExamQuestion[]>(`${this.apiUrl}/exams/${examId}/questions`);
  }

  submitAnswers(payload: SubmitExamAnswersPayload): Observable<ResultResponse> {
    return this.http.post<ResultResponse>(`${this.apiUrl}/results/answers`, payload);
  }

  getMyResults(): Observable<ResultResponse[]> {
    return this.http.get<ResultResponse[]>(`${this.apiUrl}/results`);
  }

  assetUrl(url: string | null | undefined): string {
    if (!url) {
      return '';
    }
    return url.startsWith('http') ? url : `http://localhost:8080${url}`;
  }
}
