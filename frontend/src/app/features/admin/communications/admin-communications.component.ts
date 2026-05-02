import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-admin-communications',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-communications.component.html',
  styleUrl: './admin-communications.component.css'
})
export class AdminCommunicationsComponent {
  // Email
  emailTo = 'client@cert.local';
  emailSubject = 'Information certification';
  emailBody = 'Votre dossier de certification a ete mis a jour.';
  emailLoading = false;
  bulkEmailLoading = false;
  emailMessage = '';
  emailSuccess = false;

  // SMS
  smsTo = '+21600000000';
  smsMessage = 'Rappel: verification certificat disponible.';
  smsLoading = false;
  smsStatus = '';
  smsSuccess = false;

  // Gmail config display
  showGmailConfig = false;
  gmailUser = '';
  gmailPassword = '';
  showGmailPassword = false;

  constructor(private readonly featureApiService: FeatureApiService) {}

  sendEmail(): void {
    if (!this.emailTo || !this.emailSubject || !this.emailBody) return;
    this.emailLoading = true;
    this.emailMessage = '';
    this.featureApiService.sendEmail({
      to: this.emailTo,
      subject: this.emailSubject,
      body: this.emailBody
    }).subscribe({
      next: (response) => {
        this.emailLoading = false;
        this.emailSuccess = response.status.startsWith('EMAIL_SENT');
        this.emailMessage = this.emailSuccess ? '✅ Email envoyé avec succès !' : response.status;
      },
      error: (err) => {
        this.emailLoading = false;
        this.emailSuccess = false;
        this.emailMessage = '❌ ' + (err?.error?.status || 'Échec de l\'envoi email.');
      }
    });
  }

  sendEmailToAllUsers(): void {
    if (!this.emailSubject || !this.emailBody) return;
    this.bulkEmailLoading = true;
    this.emailMessage = '';
    this.featureApiService.sendEmailToAll({
      subject: this.emailSubject,
      body: this.emailBody
    }).subscribe({
      next: (response) => {
        this.bulkEmailLoading = false;
        this.emailSuccess = response.failed === 0;
        this.emailMessage = `Envoi global termine: ${response.sent}/${response.total} envoyes, ${response.failed} echoues.`;
      },
      error: () => {
        this.bulkEmailLoading = false;
        this.emailSuccess = false;
        this.emailMessage = 'Echec de l envoi global.';
      }
    });
  }

  sendSms(): void {
    if (!this.smsTo || !this.smsMessage) return;
    this.smsLoading = true;
    this.smsStatus = '';
    this.featureApiService.sendSms({
      to: this.smsTo,
      message: this.smsMessage
    }).subscribe({
      next: (response) => {
        this.smsLoading = false;
        this.smsSuccess = true;
        this.smsStatus = '✅ ' + response.status;
      },
      error: () => {
        this.smsLoading = false;
        this.smsSuccess = false;
        this.smsStatus = '❌ Échec de l\'envoi SMS.';
      }
    });
  }

  toggleGmailPassword(): void {
    this.showGmailPassword = !this.showGmailPassword;
  }
}
