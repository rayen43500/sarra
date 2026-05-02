import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FeatureApiService } from '../../../core/services/feature-api.service';

interface BatchRun {
  label: string;
  status: 'success' | 'error' | 'running';
  time: Date;
  message: string;
}

@Component({
  selector: 'app-admin-batch',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-batch.component.html',
  styleUrl: './admin-batch.component.css'
})
export class AdminBatchComponent {
  isRunning = false;
  batchHistory: BatchRun[] = [];
  latestMessage = '';

  constructor(private readonly featureApiService: FeatureApiService) {}

  runBatch(): void {
    this.isRunning = true;
    const run: BatchRun = { label: 'newsletterDigestJob', status: 'running', time: new Date(), message: 'En cours...' };
    this.batchHistory.unshift(run);

    this.featureApiService.runNewsletterBatch().subscribe({
      next: (msg) => {
        run.status = 'success';
        run.message = msg || 'Job terminé avec succès.';
        this.latestMessage = '✅ ' + run.message;
        this.isRunning = false;
      },
      error: () => {
        run.status = 'error';
        run.message = 'Échec du lancement du batch.';
        this.latestMessage = '❌ Échec du lancement batch newsletter.';
        this.isRunning = false;
      }
    });
  }

  clearHistory(): void {
    this.batchHistory = [];
    this.latestMessage = '';
  }
}
