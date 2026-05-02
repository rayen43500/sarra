import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService, RatingSummary, ReactionSummary } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-client-community',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-community.component.html',
  styleUrl: './client-community.component.css'
})
export class ClientCommunityComponent {
  protected targetId = 1;
  protected reactionSummary: ReactionSummary | null = null;
  protected ratingScore = 5;
  protected ratingComment = '';
  protected ratingSummary: RatingSummary | null = null;
  protected feedbackMessage = '';

  constructor(private readonly featureApiService: FeatureApiService) {
    this.refreshReactionSummary();
    this.refreshRatingSummary();
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

  protected reactionLabel(value: boolean | null): string {
    if (value === true) {
      return 'Like';
    }
    if (value === false) {
      return 'Dislike';
    }
    return 'Aucune reaction';
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
}
