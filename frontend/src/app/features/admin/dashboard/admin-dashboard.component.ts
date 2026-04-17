import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Appointment, FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent {
  protected actionMessage = '';
  protected batchMessage = '';
  protected commsMessage = '';
  protected appointments: Appointment[] = [];

  protected emailTo = 'client@cert.local';
  protected emailSubject = 'Information certification';
  protected emailBody = 'Votre dossier de certification a ete mis a jour.';

  protected smsTo = '+21600000000';
  protected smsMessage = 'Rappel: verification certificat disponible.';

  protected readonly stats = [
    { label: 'Certificats delivres', value: '1,248', trend: '+12%' },
    { label: 'Certificats actifs', value: '1,032', trend: '+3.2%' },
    { label: 'Certificats expires', value: '164', trend: '+1.1%' },
    { label: 'Certificats revoques', value: '52', trend: '-0.4%' }
  ];

  protected readonly logs = [
    { action: 'CREATE_CERTIFICATE', actor: 'admin@cert.local', date: '2026-04-14 10:42' },
    { action: 'REVOKE_CERTIFICATE', actor: 'admin@cert.local', date: '2026-04-14 09:15' },
    { action: 'CREATE_EXAM', actor: 'admin@cert.local', date: '2026-04-13 17:20' }
  ];

  constructor(
    private readonly router: Router,
    private readonly featureApiService: FeatureApiService
  ) {
    this.loadAppointments();
  }

  protected createUser(): void {
    this.actionMessage = 'Module utilisateurs: ecran CRUD en cours de finalisation.';
  }

  protected createCertificate(): void {
    this.router.navigateByUrl('/app/certificates');
  }

  protected createExam(): void {
    this.actionMessage = 'Module examens: creation disponible via endpoint /api/admin/exams.';
  }

  protected runBatch(): void {
    this.featureApiService.runNewsletterBatch().subscribe({
      next: (message) => {
        this.batchMessage = message;
      },
      error: () => {
        this.batchMessage = 'Echec lancement batch newsletter.';
      }
    });
  }

  protected sendEmail(): void {
    this.featureApiService.sendEmail(this.emailTo, this.emailSubject, this.emailBody).subscribe({
      next: (response) => {
        this.commsMessage = response.status;
      },
      error: () => {
        this.commsMessage = 'Echec envoi email.';
      }
    });
  }

  protected sendSms(): void {
    this.featureApiService.sendSms(this.smsTo, this.smsMessage).subscribe({
      next: (response) => {
        this.commsMessage = response.status;
      },
      error: () => {
        this.commsMessage = 'Echec envoi SMS.';
      }
    });
  }

  protected updateAppointmentStatus(id: number, status: string): void {
    this.featureApiService.updateAppointmentStatus(id, status).subscribe({
      next: () => {
        this.loadAppointments();
      },
      error: () => {
        this.actionMessage = 'Impossible de mettre a jour le statut du RDV.';
      }
    });
  }

  private loadAppointments(): void {
    this.featureApiService.adminAppointments().subscribe({
      next: (appointments) => {
        this.appointments = appointments;
      }
    });
  }
}
