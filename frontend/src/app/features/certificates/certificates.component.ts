import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { CertificateService } from '../../core/services/certificate.service';
import { AuthService } from '../../core/services/auth.service';
import { AdminUser, UserAdminService } from '../../core/services/user-admin.service';

type CertificateStatus = 'ACTIVE' | 'EXPIRED' | 'REVOKED';

interface CertificateRow {
  id: number;
  code: string;
  title: string;
  holder: string;
  status: CertificateStatus;
  issueDate?: string;
  expiryDate?: string;
}

@Component({
  selector: 'app-certificates',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.css'
})
export class CertificatesComponent {
  protected actionMessage = '';
  protected loading = false;
  protected hasLoadError = false;
  protected loadErrorDetail = '';
  protected searchTerm = '';
  protected statusFilter: 'ALL' | CertificateStatus = 'ALL';
  protected certificates: CertificateRow[] = [];
  protected selectedCertificate: CertificateRow | null = null;
  protected creating = false;
  protected showCreateForm = false;
  protected clientUsers: AdminUser[] = [];
  protected createModel = {
    title: '',
    description: '',
    issuedToUserId: 0,
    issueDate: '',
    expiryDate: ''
  };

  constructor(
    private readonly router: Router,
    private readonly certificateService: CertificateService,
    private readonly authService: AuthService,
    private readonly userAdminService: UserAdminService
  ) {
    this.loadCertificates();
    if (this.isAdmin) {
      this.loadClientUsers();
    }
  }

  protected get isAdmin(): boolean {
    return this.authService.getRole() === 'ROLE_ADMIN';
  }

  protected get pageTitle(): string {
    return this.isAdmin ? 'Gestion des certificats' : 'Mes certificats';
  }

  protected get pageSubtitle(): string {
    return this.isAdmin
      ? 'Liste des certificats emis avec statut et actions administrateur.'
      : 'Liste de vos certificats avec statut et actions de consultation.';
  }

  protected get filteredCertificates(): CertificateRow[] {
    const query = this.searchTerm.trim().toLowerCase();
    return this.certificates.filter((cert) => {
      const byStatus = this.statusFilter === 'ALL' || cert.status === this.statusFilter;
      const byQuery = !query
        || cert.code.toLowerCase().includes(query)
        || cert.title.toLowerCase().includes(query)
        || cert.holder.toLowerCase().includes(query);
      return byStatus && byQuery;
    });
  }

  protected get activeCount(): number {
    return this.certificates.filter((c) => c.status === 'ACTIVE').length;
  }

  protected get expiredCount(): number {
    return this.certificates.filter((c) => c.status === 'EXPIRED').length;
  }

  protected get revokedCount(): number {
    return this.certificates.filter((c) => c.status === 'REVOKED').length;
  }

  protected loadCertificates(): void {
    this.loading = true;
    this.hasLoadError = false;
    this.loadErrorDetail = '';
    this.certificateService.getCertificatesForCurrentRole()
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (data) => {
          this.certificates = data.map((item) => ({
            id: item.id,
            code: item.code,
            title: item.title,
            holder: item.holder,
            status: item.status,
            issueDate: item.issueDate,
            expiryDate: item.expiryDate
          }));
          this.selectedCertificate = null;
        },
        error: (error: HttpErrorResponse) => {
          this.hasLoadError = true;
          this.loadErrorDetail = this.buildLoadErrorDetail(error);
        }
      });
  }

  protected createCertificate(): void {
    if (!this.isAdmin) {
      this.actionMessage = 'Action reservee aux administrateurs.';
      return;
    }
    this.showCreateForm = !this.showCreateForm;
    this.actionMessage = '';
  }

  protected submitCreateCertificate(): void {
    if (!this.isAdmin) {
      return;
    }

    if (!this.createModel.title.trim() || !this.createModel.issuedToUserId) {
      this.actionMessage = 'Titre et utilisateur client sont obligatoires.';
      return;
    }

    this.creating = true;
    this.certificateService.createAdminCertificate({
      title: this.createModel.title.trim(),
      description: this.createModel.description.trim(),
      issuedToUserId: this.createModel.issuedToUserId,
      issueDate: this.createModel.issueDate || null,
      expiryDate: this.createModel.expiryDate || null
    })
      .pipe(finalize(() => {
        this.creating = false;
      }))
      .subscribe({
        next: (created) => {
          this.actionMessage = `Certificat ${created.code} cree avec succes.`;
          this.showCreateForm = false;
          this.resetCreateForm();
          this.loadCertificates();
        },
        error: () => {
          this.actionMessage = 'Creation certificat impossible. Verifiez les donnees et les droits admin.';
        }
      });
  }

  protected downloadPdf(cert: CertificateRow): void {
    this.certificateService.downloadPdfForCurrentRole(cert.id).subscribe({
      next: (blob) => {
        const objectUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = objectUrl;
        a.download = `${cert.code}.pdf`;
        a.click();
        URL.revokeObjectURL(objectUrl);
        this.actionMessage = `PDF telecharge pour ${cert.code}.`;
      },
      error: () => {
        this.actionMessage = `Echec telechargement PDF pour ${cert.code}.`;
      }
    });
  }

  protected openDetails(cert: CertificateRow): void {
    this.selectedCertificate = cert;
  }

  protected clearDetails(): void {
    this.selectedCertificate = null;
  }

  protected goToVerification(cert: CertificateRow): void {
    this.router.navigate(['/verify'], { queryParams: { code: cert.code } });
  }

  private loadClientUsers(): void {
    this.userAdminService.list().subscribe({
      next: (users) => {
        this.clientUsers = users.filter((u) => {
          const isClient = u.roles.includes('ROLE_CLIENT');
          const isAdmin = u.roles.includes('ROLE_ADMIN');
          return isClient && !isAdmin && u.status === 'ACTIVE';
        });
      }
    });
  }

  private resetCreateForm(): void {
    this.createModel = {
      title: '',
      description: '',
      issuedToUserId: 0,
      issueDate: '',
      expiryDate: ''
    };
  }

  private buildLoadErrorDetail(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Backend injoignable (localhost:8080). Verifiez que le serveur est demarre.';
    }
    if (error.status === 401) {
      return 'Session expiree. Reconnectez-vous.';
    }
    if (error.status === 403) {
      return 'Acces refuse pour ce role ou compte bloque.';
    }
    if (error.status === 404) {
      return 'Endpoint introuvable. Verifiez la route backend.';
    }
    return `Erreur API (${error.status}).`;
  }
}
