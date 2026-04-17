import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserAdminService, AdminUser } from '../../../core/services/user-admin.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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
    role: ['ROLE_CLIENT' as 'ROLE_ADMIN' | 'ROLE_CLIENT', [Validators.required]],
    status: ['ACTIVE' as 'ACTIVE' | 'BLOCKED', [Validators.required]]
  });

  constructor(private readonly userAdminService: UserAdminService) {
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

    this.userAdminService.create(this.form.getRawValue()).subscribe({
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
}
