import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FeatureApiService, UserProfile } from '../../../core/services/feature-api.service';
import { AuthService } from '../../../core/services/auth.service';

interface StatCard {
  label: string;
  value: string;
  trend: string;
  colorKey: string;
  icon: string;
}

interface QuickAction {
  label: string;
  icon: string;
  fn: () => void;
}

interface Shortcut {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent {
  protected actionMessage = '';
  protected profileMessage = '';
  protected profile: UserProfile | null = null;
  protected profileAvatarFile: File | null = null;
  protected profileModel = {
    firstName: '',
    lastName: '',
    phone: ''
  };

  protected readonly stats: StatCard[] = [
    {
      label: 'Certificats délivrés',
      value: '1,248',
      trend: '+12%',
      colorKey: 'primary',
      icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="6"/><path d="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11"/></svg>`
    },
    {
      label: 'Certificats actifs',
      value: '1,032',
      trend: '+3.2%',
      colorKey: 'success',
      icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>`
    },
    {
      label: 'Certificats expirés',
      value: '164',
      trend: '+1.1%',
      colorKey: 'warning',
      icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`
    },
    {
      label: 'Certificats révoqués',
      value: '52',
      trend: '-0.4%',
      colorKey: 'danger',
      icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/></svg>`
    }
  ];

  protected readonly logs = [
    { action: 'CREATE_CERTIFICATE', actor: 'admin@cert.local', date: '2026-04-14 10:42' },
    { action: 'REVOKE_CERTIFICATE', actor: 'admin@cert.local', date: '2026-04-14 09:15' },
    { action: 'CREATE_EXAM', actor: 'admin@cert.local', date: '2026-04-13 17:20' },
    { action: 'UPDATE_USER', actor: 'admin@cert.local', date: '2026-04-13 14:05' }
  ];

  protected readonly quickActions: QuickAction[];
  protected readonly shortcuts: Shortcut[];

  constructor(
    private readonly router: Router,
    private readonly featureApiService: FeatureApiService,
    protected readonly authService: AuthService
  ) {
    this.loadProfile();
    this.quickActions = [
      {
        label: 'Ajouter un utilisateur',
        icon: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/></svg>`,
        fn: () => this.router.navigateByUrl('/app/users')
      },
      {
        label: 'Créer un certificat',
        icon: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="6"/><path d="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11"/></svg>`,
        fn: () => this.router.navigateByUrl('/app/certificates')
      },
      {
        label: 'Envoyer un email',
        icon: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>`,
        fn: () => this.router.navigateByUrl('/app/admin/communications')
      },
      {
        label: 'Generer un quiz IA',
        icon: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11h6"/><path d="M12 8v6"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg>`,
        fn: () => this.router.navigateByUrl('/app/admin/quiz')
      },
      {
        label: 'Chat Assistant IA',
        icon: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/></svg>`,
        fn: () => this.router.navigateByUrl('/app/admin/chat')
      }
    ];

    this.shortcuts = [
      { label: 'Gestion Utilisateurs', route: '/app/users', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>` },
      { label: 'Quiz IA & Resultats', route: '/app/admin/quiz', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11h6"/><path d="M12 8v6"/><rect x="3" y="4" width="18" height="16" rx="2"/></svg>` },
      { label: 'Communications Email/SMS', route: '/app/admin/communications', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>` },
      { label: 'Batch Newsletter', route: '/app/admin/batch', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>` },
      { label: 'RDV Clients', route: '/app/admin/appointments', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>` },
      { label: 'Thème & Style', route: '/app/admin/theme', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="13.5" cy="6.5" r="0.5"/><circle cx="17.5" cy="10.5" r="0.5"/><circle cx="8.5" cy="7.5" r="0.5"/><circle cx="6.5" cy="12.5" r="0.5"/><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"/></svg>` },
      { label: 'Assistant IA Gemini', route: '/app/admin/chat', icon: `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83"/></svg>` }
    ];
  }

  navigate(route: string): void {
    this.router.navigateByUrl(route);
  }

  protected saveProfile(): void {
    this.featureApiService.updateAdminProfile(this.profileModel).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileModel = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone || ''
        };
        this.profileMessage = 'Profil admin mis a jour.';
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

    this.featureApiService.uploadAdminAvatar(this.profileAvatarFile).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileAvatarFile = null;
        this.profileMessage = 'Photo admin mise a jour.';
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
    this.featureApiService.getAdminProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileModel = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone || ''
        };
      },
      error: () => {
        this.profileMessage = 'Chargement du profil admin impossible.';
      }
    });
  }
}
