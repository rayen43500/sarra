import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-client-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-payments.component.html',
  styleUrl: './client-payments.component.css'
})
export class ClientPaymentsComponent {
  protected paymentAmount = 20;
  protected paymentDescription = 'Pack preparation certification';
  protected paymentMessage = '';

  constructor(private readonly featureApiService: FeatureApiService) {}

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
}
