import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService, SocialProviders } from '../../../core/services/feature-api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-newsletter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-newsletter.component.html',
  styleUrl: './client-newsletter.component.css'
})
export class ClientNewsletterComponent {
  protected newsletterEmail = '';
  protected socialProviders: SocialProviders | null = null;
  protected feedbackMessage = '';

  constructor(
    private readonly featureApiService: FeatureApiService,
    protected readonly authService: AuthService
  ) {
    this.newsletterEmail = this.authService.getEmail();
    this.featureApiService.getSocialProviders().subscribe({
      next: (providers) => {
        this.socialProviders = providers;
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
}
