import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { Observable, forkJoin } from 'rxjs';

export interface ThemeSettings {
  appName: string;
  primaryColor: string;
  secondaryColor: string;
  logoUrl: string;
  loginBadgeText?: string;
  loginHeroTitle?: string;
  loginHeroSubtitle?: string;
  loginAdminCardTitle?: string;
  loginAdminCardDescription?: string;
  loginAdminCardCredentials?: string;
  loginClientCardTitle?: string;
  loginClientCardDescription?: string;
  loginClientCardCredentials?: string;
  backgroundMode: 'STATIC' | 'SLIDESHOW' | 'RANDOM';
}

export interface UiThemeTokens {
  primary: string;
  secondary: string;
  success: string;
  danger: string;
  warning: string;
  background: string;
  card: string;
  textMain: string;
  textSecondary: string;
  border: string;
}

export interface BackgroundImage {
  id: number;
  url: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private apiUrl = 'http://localhost:8080/api';
  private readonly uiThemeStorageKey = 'certifyhub.ui-theme';

  private readonly defaultUiTheme: UiThemeTokens = {
    primary: '#2563EB',
    secondary: '#1E40AF',
    success: '#16A34A',
    danger: '#DC2626',
    warning: '#F59E0B',
    background: '#F8FAFC',
    card: '#FFFFFF',
    textMain: '#0F172A',
    textSecondary: '#475569',
    border: '#E2E8F0'
  };

  themeSettings = signal<ThemeSettings | null>(null);
  activeBackgrounds = signal<BackgroundImage[]>([]);

  constructor(private http: HttpClient) {
    this.applyGlobalTheme(this.defaultUiTheme);
  }

  initTheme(): Observable<any> {
    return forkJoin({
      settings: this.loadThemeSettings(),
      backgrounds: this.loadActiveBackgrounds()
    });
  }

  loadThemeSettings(): Observable<ThemeSettings> {
    return this.http.get<ThemeSettings>(`${this.apiUrl}/public/theme`).pipe(
      tap(settings => {
        this.themeSettings.set(settings);
        this.applyTheme(settings);
      })
    );
  }

  loadActiveBackgrounds(): Observable<BackgroundImage[]> {
    return this.http.get<BackgroundImage[]>(`${this.apiUrl}/public/theme/backgrounds`).pipe(
      tap(bg => this.activeBackgrounds.set(bg))
    );
  }

  updateThemeSettings(settings: ThemeSettings): Observable<ThemeSettings> {
    return this.http.put<ThemeSettings>(`${this.apiUrl}/admin/theme`, settings).pipe(
      tap(s => {
        this.themeSettings.set(s);
        this.applyTheme(s);
      })
    );
  }

  uploadFile(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/admin/theme/upload`, formData, { responseType: 'text' });
  }

  getAllBackgrounds(): Observable<BackgroundImage[]> {
    return this.http.get<BackgroundImage[]>(`${this.apiUrl}/admin/theme/backgrounds`);
  }

  addBackground(url: string): Observable<BackgroundImage> {
    const formData = new FormData();
    formData.append('url', url);
    return this.http.post<BackgroundImage>(`${this.apiUrl}/admin/theme/backgrounds`, formData).pipe(
      tap(() => this.loadActiveBackgrounds().subscribe())
    );
  }

  deleteBackground(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/theme/backgrounds/${id}`).pipe(
      tap(() => this.loadActiveBackgrounds().subscribe())
    );
  }

  toggleBackground(id: number): Observable<BackgroundImage> {
    return this.http.put<BackgroundImage>(`${this.apiUrl}/admin/theme/backgrounds/${id}/toggle`, {}).pipe(
      tap(() => this.loadActiveBackgrounds().subscribe())
    );
  }

  private applyTheme(settings: ThemeSettings) {
    const uiTheme = this.getUiThemeTokens();
    const mergedTheme: UiThemeTokens = {
      ...uiTheme,
      primary: settings.primaryColor || uiTheme.primary,
      secondary: settings.secondaryColor || uiTheme.secondary
    };

    this.applyGlobalTheme(mergedTheme);
  }

  public getUiThemeTokens(): UiThemeTokens {
    try {
      const rawValue = localStorage.getItem(this.uiThemeStorageKey);
      if (!rawValue) {
        return { ...this.defaultUiTheme };
      }

      const parsed = JSON.parse(rawValue) as Partial<UiThemeTokens>;
      return {
        ...this.defaultUiTheme,
        ...parsed
      };
    } catch {
      return { ...this.defaultUiTheme };
    }
  }

  public updateUiThemeTokens(tokens: UiThemeTokens): UiThemeTokens {
    const normalized: UiThemeTokens = {
      ...this.defaultUiTheme,
      ...tokens
    };

    localStorage.setItem(this.uiThemeStorageKey, JSON.stringify(normalized));
    this.applyGlobalTheme(normalized);
    return normalized;
  }

  public resetUiThemeTokens(): UiThemeTokens {
    localStorage.removeItem(this.uiThemeStorageKey);
    this.applyGlobalTheme(this.defaultUiTheme);
    return { ...this.defaultUiTheme };
  }

  private applyGlobalTheme(theme: UiThemeTokens): void {
    const root = document.documentElement;

    root.style.setProperty('--font-heading', 'Sora, sans-serif');
    root.style.setProperty('--font-body', 'Manrope, sans-serif');

    root.style.setProperty('--color-primary', theme.primary);
    root.style.setProperty('--color-secondary', theme.secondary);
    root.style.setProperty('--color-success', theme.success);
    root.style.setProperty('--color-danger', theme.danger);
    root.style.setProperty('--color-warning', theme.warning);

    root.style.setProperty('--color-bg-app', theme.background);
    root.style.setProperty('--color-bg-card', theme.card);
    root.style.setProperty('--color-text-main', theme.textMain);
    root.style.setProperty('--color-text-muted', theme.textSecondary);
    root.style.setProperty('--color-border', theme.border);

    root.style.setProperty('--color-primary-light', `${theme.primary}1A`);
    root.style.setProperty('--color-success-bg', `${theme.success}1A`);
    root.style.setProperty('--color-danger-bg', `${theme.danger}1A`);
    root.style.setProperty('--color-warning-bg', `${theme.warning}1A`);
    root.style.setProperty('--color-border-hover', theme.secondary);
  }

  public formatUrl(url: string | null): string | null {
    if (!url) return null;
    if (url.startsWith('/api')) {
      return `http://localhost:8080${url}`;
    }
    return url;
  }
}
