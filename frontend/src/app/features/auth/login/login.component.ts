import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);

  protected loading = false;
  protected error = '';

  protected form = this.fb.nonNullable.group({
    email: ['admin@cert.local', [Validators.required, Validators.email]],
    password: ['Admin@123', [Validators.required, Validators.minLength(6)]]
  });

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login(this.form.getRawValue()).subscribe({
      next: (response) => {
        const destination = response.role === 'ROLE_ADMIN' ? '/app/admin' : '/app/client';
        this.router.navigateByUrl(destination);
      },
      error: () => {
        this.error = 'Connexion impossible. Verifie le backend et les identifiants.';
        this.loading = false;
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
