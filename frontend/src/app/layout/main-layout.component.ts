import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/services/auth.service';

interface NavItem {
  label: string;
  route: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent {
  private readonly adminNavItems: NavItem[] = [
    { label: 'Dashboard Admin', route: '/app/admin' },
    { label: 'Utilisateurs', route: '/app/users' },
    { label: 'Certificats (Admin)', route: '/app/certificates' },
    { label: 'Verification Publique', route: '/verify' }
  ];

  private readonly clientNavItems: NavItem[] = [
    { label: 'Dashboard Client', route: '/app/client' },
    { label: 'Examens QCM', route: '/app/exams' },
    { label: 'Mes Certificats', route: '/app/certificates' },
    { label: 'Verification Publique', route: '/verify' }
  ];

  constructor(
    public authService: AuthService,
    private readonly router: Router
  ) {}

  get isAdmin(): boolean {
    return this.authService.getRole() === 'ROLE_ADMIN';
  }

  get spaceTitle(): string {
    return this.isAdmin ? 'Espace Administrateur' : 'Espace Client';
  }

  get portalTitle(): string {
    return this.isAdmin ? 'Portail Administrateur' : 'Portail Client';
  }

  get portalSubtitle(): string {
    return this.isAdmin
      ? 'Pilotage, moderation et suivi centralises en temps reel.'
      : 'Gestion, verification et suivi centralises en temps reel.';
  }

  get navItems(): NavItem[] {
    return this.isAdmin ? this.adminNavItems : this.clientNavItems;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
