import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  FeatureApiService
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
    protected readonly authService: AuthService
  ) {
  }

  protected goToCertificates(): void {
    this.router.navigateByUrl('/app/certificates');
  }

  protected startExam(): void {
    this.router.navigateByUrl('/app/exams');
  }

}
