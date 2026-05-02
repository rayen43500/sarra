import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly themeService = inject(ThemeService);

  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  protected loading = false;
  protected error = '';
  protected showPassword = false;

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.register(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigateByUrl('/app');
      },
      error: (err) => {
        this.loading = false;
        if (err?.error?.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Inscription impossible. Verifiez vos informations.';
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
}
