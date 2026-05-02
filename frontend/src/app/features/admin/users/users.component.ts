import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { UserAdminService, AdminUser, CreateAdminUserPayload } from '../../../core/services/user-admin.service';
import { FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent {
  private readonly fb = inject(FormBuilder);

  protected loading = false;
  protected message = '';
  protected users: AdminUser[] = [];

  protected form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['Password@123', [Validators.required, Validators.minLength(6)]],
    phone: [''],
    status: ['ACTIVE' as 'ACTIVE' | 'BLOCKED', [Validators.required]]
  });

  protected selectedUserForEmail: AdminUser | null = null;
  protected quickEmailSubject = '';
  protected quickEmailBody = '';
  protected isSendingEmail = false;

  constructor(
    private readonly userAdminService: UserAdminService,
    private readonly featureApiService: FeatureApiService
  ) {
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.loading = true;
    this.userAdminService.list().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: () => {
        this.message = 'Impossible de charger les utilisateurs.';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  protected createUser(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: CreateAdminUserPayload = { ...this.form.getRawValue(), role: 'ROLE_ADMIN' };
    this.userAdminService.create(payload).subscribe({
      next: () => {
        this.message = 'Utilisateur cree avec succes.';
        this.form.patchValue({
          firstName: '',
          lastName: '',
          email: '',
          password: 'Password@123',
          phone: ''
        });
        this.loadUsers();
      },
      error: () => {
        this.message = 'Echec creation utilisateur.';
      }
    });
  }

  protected toggleStatus(user: AdminUser): void {
    const status = user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    this.userAdminService.updateStatus(user.id, status).subscribe({
      next: () => {
        this.message = `Statut mis a jour pour ${user.email}.`;
        this.loadUsers();
      },
      error: () => {
        this.message = 'Echec mise a jour du statut.';
      }
    });
  }

  protected assignClientRole(user: AdminUser): void {
    this.userAdminService.assignRole(user.id, 'ROLE_CLIENT').subscribe({
      next: () => {
        this.message = `Role client assigne a ${user.email}.`;
        this.loadUsers();
      },
      error: () => {
        this.message = 'Echec assignation role.';
      }
    });
  }

  protected assignAdminRole(user: AdminUser): void {
    this.userAdminService.assignRole(user.id, 'ROLE_ADMIN').subscribe({
      next: () => {
        this.message = `Role admin assigne a ${user.email}.`;
        this.loadUsers();
      },
      error: () => {
        this.message = 'Echec assignation role.';
      }
    });
  }

  protected deleteUser(user: AdminUser): void {
    this.userAdminService.remove(user.id).subscribe({
      next: () => {
        this.message = `Utilisateur ${user.email} supprime.`;
        this.loadUsers();
      },
      error: () => {
        this.message = 'Suppression impossible.';
      }
    });
  }

  protected openEmailModal(user: AdminUser): void {
    this.selectedUserForEmail = user;
    this.quickEmailSubject = 'Information CertifyHub';
    this.quickEmailBody = `Bonjour ${user.firstName},\n\nNous vous contactons concernant votre compte CertifyHub...`;
  }

  protected closeEmailModal(): void {
    this.selectedUserForEmail = null;
  }

  protected sendQuickEmail(): void {
    if (!this.selectedUserForEmail || this.isSendingEmail) return;

    this.isSendingEmail = true;
    this.featureApiService.sendEmail({
      to: this.selectedUserForEmail.email,
      subject: this.quickEmailSubject,
      body: this.quickEmailBody
    }).subscribe({
      next: (res) => {
        this.message = `Email envoye a ${this.selectedUserForEmail?.email}`;
        this.closeEmailModal();
      },
      error: (err) => {
        this.message = 'Erreur lors de l\'envoi.';
      },
      complete: () => {
        this.isSendingEmail = false;
      }
    });
  }

  protected notifyUser(user: AdminUser, type: 'WELCOME' | 'ACCOUNT_UPDATE'): void {
    const subjects = {
      WELCOME: 'Bienvenue sur CertifyHub !',
      ACCOUNT_UPDATE: 'Mise à jour de votre compte CertifyHub'
    };
    const bodies = {
      WELCOME: `Félicitations ${user.firstName} !\n\nVotre compte a été créé avec succès sur CertifyHub. Vous pouvez maintenant accéder à vos certificats numériques.`,
      ACCOUNT_UPDATE: `Bonjour ${user.firstName},\n\nVotre compte a été mis à jour par un administrateur. Nouveau statut : ${user.status}.`
    };

    this.featureApiService.sendEmail({
      to: user.email,
      subject: subjects[type],
      body: bodies[type]
    }).subscribe({
      next: () => this.message = `Notification envoyée à ${user.email}`,
      error: () => this.message = 'Échec notification.'
    });
  }
}
