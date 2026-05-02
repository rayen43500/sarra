import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService, UserProfile } from '../../../core/services/feature-api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-profile.component.html',
  styleUrl: './client-profile.component.css'
})
export class ClientProfileComponent {
  protected profileMessage = '';
  protected profile: UserProfile | null = null;
  protected profileAvatarFile: File | null = null;
  protected profileModel = {
    firstName: '',
    lastName: '',
    phone: ''
  };

  constructor(
    private readonly featureApiService: FeatureApiService,
    protected readonly authService: AuthService
  ) {
    this.loadProfile();
  }

  protected saveProfile(): void {
    this.featureApiService.updateProfile(this.profileModel).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileModel = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone || ''
        };
        this.profileMessage = 'Profil mis a jour.';
      },
      error: () => {
        this.profileMessage = 'Mise a jour du profil impossible.';
      }
    });
  }

  protected onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.profileAvatarFile = input.files?.[0] ?? null;
  }

  protected uploadAvatar(): void {
    if (!this.profileAvatarFile) {
      this.profileMessage = 'Selectionnez une image.';
      return;
    }

    this.featureApiService.uploadProfileAvatar(this.profileAvatarFile).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileAvatarFile = null;
        this.profileMessage = 'Photo de profil mise a jour.';
      },
      error: () => {
        this.profileMessage = 'Upload image impossible.';
      }
    });
  }

  protected avatarUrl(): string {
    return this.featureApiService.assetUrl(this.profile?.avatarUrl);
  }

  private loadProfile(): void {
    this.featureApiService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileModel = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone || ''
        };
      },
      error: () => {
        this.profileMessage = 'Chargement du profil impossible.';
      }
    });
  }
}
