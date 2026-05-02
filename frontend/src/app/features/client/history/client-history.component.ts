import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FeatureApiService, LoginHistory } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-client-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-history.component.html',
  styleUrl: './client-history.component.css'
})
export class ClientHistoryComponent {
  protected loginHistory: LoginHistory[] = [];

  constructor(private readonly featureApiService: FeatureApiService) {
    this.loadLoginHistory();
  }

  private loadLoginHistory(): void {
    this.featureApiService.getLoginHistory().subscribe({
      next: (history) => {
        this.loginHistory = history;
      }
    });
  }
}
