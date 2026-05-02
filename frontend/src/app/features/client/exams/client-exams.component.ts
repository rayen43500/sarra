import { CommonModule } from '@angular/common';
import { Component, HostListener, OnDestroy } from '@angular/core';
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
export class ClientExamsComponent implements OnDestroy {
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
  protected results: ResultResponse[] = [];
  protected attemptNumber = 1;
  protected focusLossCount = 0;
  protected copyPasteCount = 0;
  protected fullscreenExitCount = 0;

  protected answers: Record<number, number> = {};
  private timerId: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly examService: ExamClientService) {
    this.loadActiveExams();
    this.loadMyResults();
  }

  ngOnDestroy(): void {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
  }

  @HostListener('window:blur')
  protected onWindowBlur(): void {
    if (this.started) {
      this.focusLossCount += 1;
    }
  }

  @HostListener('document:copy')
  @HostListener('document:paste')
  @HostListener('document:contextmenu')
  protected onCopyPasteSignal(): void {
    if (this.started) {
      this.copyPasteCount += 1;
    }
  }

  @HostListener('document:fullscreenchange')
  protected onFullscreenChange(): void {
    if (this.started && !document.fullscreenElement) {
      this.fullscreenExitCount += 1;
    }
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
        this.questions = [...questions].sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
        this.durationSeconds = Math.max(60, (exam.durationMinutes ?? 2) * 60);
        this.remainingSeconds = this.durationSeconds;
        this.answers = {};
        this.resetFraudSignals();
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
    this.resetFraudSignals();
    const fullscreenRequest = document.documentElement.requestFullscreen?.();
    fullscreenRequest?.catch(() => undefined);

    this.timerId = setInterval(() => {
      this.remainingSeconds -= 1;
      if (this.remainingSeconds <= 0) {
        this.submitExam();
      }
    }, 1000);
  }

  protected selectAnswer(questionId: number, optionId: number): void {
    if (this.answers[questionId] === optionId) {
      delete this.answers[questionId];
      return;
    }
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

    if (answerItems.length !== this.questions.length) {
      this.message = `Merci de repondre a toutes les questions (${this.answeredCount}/${this.questions.length}).`;
      return;
    }

    this.loading = true;
    this.examService.submitAnswers({
      examId,
      attemptNumber: this.attemptNumber,
      answers: answerItems,
      fraud: {
        focusLossCount: this.focusLossCount,
        copyPasteCount: this.copyPasteCount,
        fullscreenExitCount: this.fullscreenExitCount,
        totalSeconds: this.durationSeconds,
        remainingSeconds: this.remainingSeconds
      }
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
        this.loadMyResults();
        if (document.fullscreenElement) {
          const fullscreenExit = document.exitFullscreen?.();
          fullscreenExit?.catch(() => undefined);
        }
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

  protected get selectedExam(): ActiveExam | null {
    return this.exams.find((exam) => exam.id === this.selectedExamId) ?? null;
  }

  protected revisionDocumentUrl(): string {
    return this.examService.assetUrl(this.selectedExam?.revisionDocumentUrl);
  }

  protected fraudBadge(result: ResultResponse): string {
    return result.fraudSuspicious ? 'danger' : result.fraudScore >= 25 ? 'warning' : 'success';
  }

  protected get answeredCount(): number {
    return Object.keys(this.answers).length;
  }

  protected get canSubmit(): boolean {
    return this.started && !this.loading && this.questions.length > 0 && this.answeredCount === this.questions.length;
  }

  private resetFraudSignals(): void {
    this.focusLossCount = 0;
    this.copyPasteCount = 0;
    this.fullscreenExitCount = 0;
  }

  private loadMyResults(): void {
    this.examService.getMyResults().subscribe({
      next: (results) => {
        this.results = results;
      }
    });
  }
}
