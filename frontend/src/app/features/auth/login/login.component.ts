import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

interface LoginAccessCard {
  title: string;
  description: string;
  credentials: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  protected loading = false;
  protected error = '';
  protected showPassword = false;

  constructor() {
    this.route.queryParamMap.subscribe(params => {
      const token = params.get('token');
      const email = params.get('email');
      const role = params.get('role');
      if (token && email && role) {
        this.authService.setSession({ token, email, role: role as 'ROLE_ADMIN' | 'ROLE_CLIENT' });
        this.router.navigateByUrl('/app');
      }
    });
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const { email, password } = this.form.getRawValue();

    this.authService.login({ email, password }).subscribe({
      next: () => {
        this.router.navigateByUrl('/app');
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 403) {
          this.error = 'Compte desactive ou acces refuse.';
        } else {
          this.error = 'Identifiants invalides. Reessayez.';
        }
      }
    });
  }

  linkedInLogin(): void {
    window.location.href = 'http://localhost:8080/oauth2/authorization/linkedin';
  }

  get appName(): string {
    return this.themeService.themeSettings()?.appName || 'CertifyHub';
  }

  get logoUrl(): string | null {
    return this.themeService.formatUrl(this.themeService.themeSettings()?.logoUrl || null);
  }

  get loginBadgeText(): string {
    return this.themeService.themeSettings()?.loginBadgeText?.trim() || 'Certificats signes et verifiables';
  }

  get loginHeroTitle(): string {
    return this.themeService.themeSettings()?.loginHeroTitle?.trim() || 'Votre espace de confiance pour la certification numerique.';
  }

  get loginHeroSubtitle(): string {
    return this.themeService.themeSettings()?.loginHeroSubtitle?.trim()
      || 'Centralisez les preuves, validez l authentification en quelques secondes et partagez des documents inviolables avec vos partenaires.';
  }

  get loginAccessCards(): LoginAccessCard[] {
    const settings = this.themeService.themeSettings();

    return [
      {
        title: settings?.loginAdminCardTitle?.trim() || 'Acces Admin',
        description: settings?.loginAdminCardDescription?.trim() || 'Publication, signature et suivi des certificats.',
        credentials: settings?.loginAdminCardCredentials?.trim() || 'admin@cert.local / Admin@123'
      },
      {
        title: settings?.loginClientCardTitle?.trim() || 'Acces Client',
        description: settings?.loginClientCardDescription?.trim() || 'Consultation, telechargement et partage securise.',
        credentials: settings?.loginClientCardCredentials?.trim() || 'client@cert.local / Client@123'
      }
    ];
  }
}
