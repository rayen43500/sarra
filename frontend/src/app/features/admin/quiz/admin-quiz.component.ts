import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AdminQuizService,
  GenerateQuizPayload,
  GeneratedQuizResponse
} from '../../../core/services/admin-quiz.service';
import { ActiveExam, ResultResponse } from '../../../core/services/exam-client.service';

@Component({
  selector: 'app-admin-quiz',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-quiz.component.html',
  styleUrl: './admin-quiz.component.css'
})
export class AdminQuizComponent {
  protected exams: ActiveExam[] = [];
  protected results: ResultResponse[] = [];
  protected generatedQuiz: GeneratedQuizResponse | null = null;

  protected loading = false;
  protected resultsLoading = false;
  protected uploadLoading = false;
  protected message = '';

  protected generateModel: GenerateQuizPayload = {
    topic: 'Certification numerique',
    questionCount: 5,
    difficulty: 'intermediaire',
    durationMinutes: 15,
    passScore: 70,
    isActive: true
  };

  protected selectedExamId: number | null = null;
  protected revisionTitle = 'Document de revision';
  protected revisionFile: File | null = null;

  constructor(private readonly adminQuizService: AdminQuizService) {
    this.loadExams();
  }

  protected loadExams(): void {
    this.adminQuizService.listExams().subscribe({
      next: (exams) => {
        this.exams = exams;
        if (this.selectedExamId == null && exams.length > 0) {
          this.selectedExamId = exams[0].id;
          this.loadResults();
        }
      },
      error: () => {
        this.message = 'Chargement des quiz impossible.';
      }
    });
  }

  protected generateQuiz(): void {
    if (!this.generateModel.topic.trim()) {
      this.message = 'Le sujet du quiz est obligatoire.';
      return;
    }

    this.loading = true;
    this.message = '';
    this.adminQuizService.generateQuiz(this.generateModel).subscribe({
      next: (response) => {
        this.generatedQuiz = response;
        this.selectedExamId = response.exam.id;
        this.message = `Quiz cree avec ${response.questions.length} questions (${response.source}).`;
        this.loadExams();
        this.loadResults();
      },
      error: () => {
        this.message = 'Generation du quiz impossible. Verifiez les donnees et le backend.';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected onRevisionFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.revisionFile = input.files?.[0] ?? null;
  }

  protected uploadRevisionDocument(): void {
    if (!this.selectedExamId || !this.revisionFile) {
      this.message = 'Selectionnez un quiz et un document.';
      return;
    }

    this.uploadLoading = true;
    this.adminQuizService.uploadRevisionDocument(this.selectedExamId, this.revisionTitle, this.revisionFile).subscribe({
      next: () => {
        this.message = 'Document de revision ajoute au quiz.';
        this.revisionFile = null;
        this.loadExams();
      },
      error: () => {
        this.message = 'Upload document impossible.';
      },
      complete: () => {
        this.uploadLoading = false;
      }
    });
  }

  protected loadResults(): void {
    if (!this.selectedExamId) {
      this.results = [];
      return;
    }

    this.resultsLoading = true;
    this.adminQuizService.getResults(this.selectedExamId).subscribe({
      next: (results) => {
        this.results = results;
      },
      error: () => {
        this.message = 'Chargement des resultats impossible.';
      },
      complete: () => {
        this.resultsLoading = false;
      }
    });
  }

  protected documentUrl(url: string | null | undefined): string {
    return this.adminQuizService.assetUrl(url);
  }

  protected fraudBadge(result: ResultResponse): string {
    return result.fraudSuspicious ? 'danger' : result.fraudScore >= 25 ? 'warning' : 'success';
  }
}
