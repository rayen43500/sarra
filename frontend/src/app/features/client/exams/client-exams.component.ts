import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ActiveExam,
  ExamClientService,
  ExamQuestion,
  ResultResponse,
  SubmitExamAnswerItem
} from '../../../core/services/exam-client.service';

@Component({
  selector: 'app-client-exams',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-exams.component.html',
  styleUrl: './client-exams.component.css'
})
export class ClientExamsComponent {
  protected exams: ActiveExam[] = [];
  protected selectedExamId: number | null = null;
  protected questions: ExamQuestion[] = [];

  protected durationSeconds = 120;
  protected remainingSeconds = 120;
  protected started = false;
  protected submitted = false;
  protected loading = false;
  protected loadingExamData = false;
  protected message = '';
  protected score = 0;
  protected maxScore = 0;
  protected currentResult: ResultResponse | null = null;
  protected attemptNumber = 1;

  protected answers: Record<number, number> = {};
  private timerId: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly examService: ExamClientService) {
    this.loadActiveExams();
  }

  protected loadActiveExams(): void {
    this.loadingExamData = true;
    this.examService.getActiveExams().subscribe({
      next: (exams) => {
        this.exams = exams;
        if (exams.length > 0 && this.selectedExamId == null) {
          this.selectedExamId = exams[0].id;
          this.loadExamQuestions();
        }
      },
      error: () => {
        this.message = 'Chargement des examens impossible.';
      },
      complete: () => {
        this.loadingExamData = false;
      }
    });
  }

  protected loadExamQuestions(): void {
    if (!this.selectedExamId) {
      this.message = 'Selectionnez un examen.';
      return;
    }

    const exam = this.exams.find((e) => e.id === this.selectedExamId);
    if (!exam) {
      this.message = 'Examen introuvable.';
      return;
    }

    this.loadingExamData = true;
    this.examService.getExamQuestions(this.selectedExamId).subscribe({
      next: (questions) => {
        this.questions = questions;
        this.durationSeconds = Math.max(60, (exam.durationMinutes ?? 2) * 60);
        this.remainingSeconds = this.durationSeconds;
        this.answers = {};
        this.submitted = false;
        this.currentResult = null;
        this.maxScore = this.questions.reduce((acc, q) => acc + (q.points ?? 1), 0);
        this.message = questions.length === 0
          ? 'Aucune question configuree pour cet examen. Contactez un administrateur.'
          : '';
      },
      error: () => {
        this.message = 'Chargement des questions impossible.';
      },
      complete: () => {
        this.loadingExamData = false;
      }
    });
  }

  protected startExam(): void {
    if (this.started || this.questions.length === 0) {
      return;
    }

    this.started = true;
    this.submitted = false;
    this.message = '';
    this.remainingSeconds = this.durationSeconds;
    this.answers = {};
    this.currentResult = null;

    this.timerId = setInterval(() => {
      this.remainingSeconds -= 1;
      if (this.remainingSeconds <= 0) {
        this.submitExam();
      }
    }, 1000);
  }

  protected selectAnswer(questionId: number, optionId: number): void {
    this.answers[questionId] = optionId;
  }

  protected submitExam(): void {
    if (!this.started || this.submitted) {
      return;
    }

    if (this.timerId) {
      clearInterval(this.timerId);
      this.timerId = null;
    }

    const examId = this.selectedExamId;
    if (!examId) {
      this.message = 'Aucun examen selectionne.';
      this.started = false;
      return;
    }

    const answerItems: SubmitExamAnswerItem[] = this.questions
      .filter((q) => this.answers[q.id] != null)
      .map((q) => ({ questionId: q.id, optionId: this.answers[q.id] }));

    if (answerItems.length === 0) {
      this.message = 'Aucune reponse selectionnee.';
      this.started = false;
      return;
    }

    this.loading = true;
    this.examService.submitAnswers({
      examId,
      attemptNumber: this.attemptNumber,
      answers: answerItems
    }).subscribe({
      next: (result) => {
        this.submitted = true;
        this.started = false;
        this.currentResult = result;
        this.score = result.score;
        this.maxScore = result.maxScore;
        this.attemptNumber += 1;
        this.message = result.passed
          ? 'Examen reussi. Un certificat a ete genere automatiquement.'
          : 'Examen termine. Continuez vos revisions puis repassez le test.';
      },
      error: () => {
        this.message = 'Soumission impossible. Verifiez la connexion backend.';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected formatTime(seconds: number): string {
    const mm = Math.floor(seconds / 60).toString().padStart(2, '0');
    const ss = (seconds % 60).toString().padStart(2, '0');
    return `${mm}:${ss}`;
  }
}
