import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ThemeService, ThemeSettings, BackgroundImage, UiThemeTokens } from '../../../core/services/theme.service';

@Component({
  selector: 'app-admin-theme',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-theme.component.html',
  styleUrl: './admin-theme.component.css'
})
export class AdminThemeComponent implements OnInit {
  themeForm: ThemeSettings = {
    appName: '',
    primaryColor: '',
    secondaryColor: '',
    logoUrl: '',
    loginBadgeText: '',
    loginHeroTitle: '',
    loginHeroSubtitle: '',
    loginAdminCardTitle: '',
    loginAdminCardDescription: '',
    loginAdminCardCredentials: '',
    loginClientCardTitle: '',
    loginClientCardDescription: '',
    loginClientCardCredentials: '',
    backgroundMode: 'STATIC'
  };

  uiThemeForm: UiThemeTokens;
  allBackgrounds: BackgroundImage[] = [];
  actionMessage = '';
  actionSuccess = false;
  activeTab: 'colors' | 'login' | 'backgrounds' = 'colors';

  constructor(private readonly themeService: ThemeService) {
    this.uiThemeForm = this.themeService.getUiThemeTokens();
  }

  ngOnInit(): void {
    this.themeService.loadThemeSettings().subscribe(settings => {
      this.themeForm = { ...settings };
      this.uiThemeForm.primary = settings.primaryColor || this.uiThemeForm.primary;
      this.uiThemeForm.secondary = settings.secondaryColor || this.uiThemeForm.secondary;
      this.themeService.updateUiThemeTokens(this.uiThemeForm);
    });
    this.loadBackgrounds();
  }

  loadBackgrounds(): void {
    this.themeService.getAllBackgrounds().subscribe(bgs => {
      this.allBackgrounds = bgs;
    });
  }

  applyVisualThemeOnly(): void {
    this.themeService.updateUiThemeTokens(this.uiThemeForm);
    this.showMsg('✅ Aperçu visuel appliqué sur toute l\'interface.', true);
  }

  resetVisualTheme(): void {
    this.uiThemeForm = this.themeService.resetUiThemeTokens();
    this.themeForm.primaryColor = this.uiThemeForm.primary;
    this.themeForm.secondaryColor = this.uiThemeForm.secondary;
    this.showMsg('🔄 Palette visuelle réinitialisée.', true);
  }

  updateTheme(): void {
    this.themeForm.primaryColor = this.uiThemeForm.primary;
    this.themeForm.secondaryColor = this.uiThemeForm.secondary;
    this.themeService.updateThemeSettings(this.themeForm).subscribe({
      next: () => {
        this.themeService.updateUiThemeTokens(this.uiThemeForm);
        this.showMsg('✅ Thème global sauvegardé et appliqué à toute la plateforme.', true);
      },
      error: () => this.showMsg('❌ Erreur lors de la mise à jour du thème.', false)
    });
  }

  onLogoUpload(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.themeService.uploadFile(file).subscribe(url => {
        this.themeForm.logoUrl = url;
        this.showMsg('✅ Logo téléchargé avec succès.', true);
      });
    }
  }

  onBackgroundUpload(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.themeService.uploadFile(file).subscribe(url => {
        this.themeService.addBackground(url).subscribe(() => this.loadBackgrounds());
      });
    }
  }

  toggleBg(id: number): void {
    this.themeService.toggleBackground(id).subscribe(() => this.loadBackgrounds());
  }

  deleteBg(id: number): void {
    if (confirm('Supprimer cette image de fond ?')) {
      this.themeService.deleteBackground(id).subscribe(() => this.loadBackgrounds());
    }
  }

  previewBg(url: string): void {
    const formatted = this.formatAssetUrl(url);
    if (formatted) {
      // Temporarily inject into root style for immediate feedback
      document.documentElement.style.setProperty('--preview-bg', `url(${formatted})`);
      this.showMsg('👁️ Aperçu temporaire activé.', true);
    }
  }

  formatAssetUrl(url: string | null | undefined): string | null {
    return url ? (this.themeService.formatUrl(url) || null) : null;
  }

  private showMsg(msg: string, success: boolean): void {
    this.actionMessage = msg;
    this.actionSuccess = success;
    setTimeout(() => this.actionMessage = '', 4000);
  }
}
