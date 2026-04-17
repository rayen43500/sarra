import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  Appointment,
  FeatureApiService,
  RatingSummary,
  ReactionSummary,
  SocialProviders
} from '../../../core/services/feature-api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-dashboard.component.html',
  styleUrl: './client-dashboard.component.css'
})
export class ClientDashboardComponent {
  protected actionMessage = '';
  protected feedbackMessage = '';
  protected paymentMessage = '';
  protected appointmentMessage = '';

  protected targetId = 1;
  protected reactionSummary: ReactionSummary | null = null;

  protected ratingScore = 5;
  protected ratingComment = '';
  protected ratingSummary: RatingSummary | null = null;

  protected paymentAmount = 20;
  protected paymentDescription = 'Pack preparation certification';

  protected appointmentDateTime = '';
  protected appointmentNote = '';
  protected appointments: Appointment[] = [];

  protected newsletterEmail = '';
  protected socialProviders: SocialProviders | null = null;

  protected readonly summary = [
    { label: 'Mes certificats', value: 7, badge: 'Actifs' },
    { label: 'Examens termines', value: 5, badge: 'Valides' },
    { label: 'Examens en attente', value: 2, badge: 'A suivre' }
  ];

  protected readonly nextActions = [
    'Telecharger votre dernier certificat PDF',
    'Lancer un examen de niveau avance',
    'Verifier un certificat via code ou QR'
  ];

  constructor(
    private readonly router: Router,
    private readonly featureApiService: FeatureApiService,
    private readonly authService: AuthService
  ) {
    this.newsletterEmail = this.authService.getEmail();
    this.refreshReactionSummary();
    this.refreshRatingSummary();
    this.loadAppointments();
    this.featureApiService.getSocialProviders().subscribe({
      next: (providers) => {
        this.socialProviders = providers;
      }
    });
  }

  protected goToCertificates(): void {
    this.router.navigateByUrl('/app/certificates');
  }

  protected startExam(): void {
    this.actionMessage = 'Examen: utilisez la section client ou endpoint /api/client/exams.';
  }

  protected setLike(liked: boolean): void {
    this.featureApiService.react('CERTIFICATE', this.targetId, liked).subscribe({
      next: (summary) => {
        this.reactionSummary = summary;
        this.feedbackMessage = liked ? 'Reaction LIKE enregistree.' : 'Reaction DISLIKE enregistree.';
      },
      error: () => {
        this.feedbackMessage = 'Echec enregistrement reaction.';
      }
    });
  }

  protected submitRating(): void {
    this.featureApiService.rate('CERTIFICATE', this.targetId, this.ratingScore, this.ratingComment).subscribe({
      next: () => {
        this.feedbackMessage = 'Rating enregistre.';
        this.ratingComment = '';
        this.refreshRatingSummary();
      },
      error: () => {
        this.feedbackMessage = 'Echec enregistrement rating.';
      }
    });
  }

  protected simulatePayment(): void {
    this.featureApiService.createPayment(this.paymentAmount, 'EUR', this.paymentDescription).subscribe({
      next: (response) => {
        this.paymentMessage = `Paiement ${response.status} - ref ${response.reference}`;
      },
      error: () => {
        this.paymentMessage = 'Paiement impossible pour le moment.';
      }
    });
  }

  protected createAppointment(): void {
    if (!this.appointmentDateTime) {
      this.appointmentMessage = 'Merci de choisir une date.';
      return;
    }

    const scheduledAt = new Date(this.appointmentDateTime).toISOString();
    this.featureApiService.createAppointment(scheduledAt, this.appointmentNote).subscribe({
      next: () => {
        this.appointmentMessage = 'RDV cree avec succes.';
        this.appointmentNote = '';
        this.loadAppointments();
      },
      error: () => {
        this.appointmentMessage = 'Echec creation RDV.';
      }
    });
  }

  protected subscribeNewsletter(): void {
    this.featureApiService.subscribeNewsletter(this.newsletterEmail).subscribe({
      next: (response) => {
        this.feedbackMessage = response.message;
      },
      error: () => {
        this.feedbackMessage = 'Echec abonnement newsletter.';
      }
    });
  }

  protected get rssUrl(): string {
    return this.featureApiService.certificatesRssUrl();
  }

  protected reactionLabel(value: boolean | null): string {
    if (value === true) {
      return 'Like';
    }
    if (value === false) {
      return 'Dislike';
    }
    return 'Aucune reaction';
  }

  protected appointmentBadgeClass(status: string): string {
    if (status === 'CONFIRMED') {
      return 'success';
    }
    if (status === 'CANCELLED' || status === 'MISSED') {
      return 'danger';
    }
    return 'warning';
  }

  private refreshReactionSummary(): void {
    this.featureApiService.reactionSummary('CERTIFICATE', this.targetId).subscribe({
      next: (summary) => {
        this.reactionSummary = summary;
      }
    });
  }

  private refreshRatingSummary(): void {
    this.featureApiService.ratingSummary('CERTIFICATE', this.targetId).subscribe({
      next: (summary) => {
        this.ratingSummary = summary;
      }
    });
  }

  private loadAppointments(): void {
    this.featureApiService.myAppointments().subscribe({
      next: (appointments) => {
        this.appointments = appointments;
      }
    });
  }
}
