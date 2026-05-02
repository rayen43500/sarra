import { Component, OnInit, OnDestroy, effect } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatbotWidgetComponent } from './shared/chatbot-widget/chatbot-widget.component';
import { ThemeService, BackgroundImage } from './core/services/theme.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ChatbotWidgetComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  currentBgUrl = '';
  bgMode = 'STATIC';
  private slideInterval: ReturnType<typeof setInterval> | null = null;

  constructor(public themeService: ThemeService) {
    effect(() => {
      const settings = this.themeService.themeSettings();
      const backgrounds = this.themeService.activeBackgrounds();
      this.bgMode = settings?.backgroundMode || 'STATIC';
      this.startBackgroundRotation(backgrounds);
    });
  }

  ngOnInit() {
    this.themeService.initTheme().subscribe({
      next: (res) => {
        this.bgMode = res.settings.backgroundMode || 'STATIC';
        this.startBackgroundRotation(res.backgrounds);
      },
      error: (err) => console.error('Failed to load theme:', err)
    });
  }

  ngOnDestroy() {
    this.clearBackgroundTimer();
  }

  private startBackgroundRotation(backgrounds: BackgroundImage[]) {
    this.clearBackgroundTimer();

    if (!backgrounds || backgrounds.length === 0) {
      this.currentBgUrl = '';
      return;
    }

    if (this.bgMode === 'STATIC') {
      this.currentBgUrl = this.themeService.formatUrl(backgrounds[0].url) || '';
    } else if (this.bgMode === 'SLIDESHOW') {
      let currentIndex = 0;
      this.currentBgUrl = this.themeService.formatUrl(backgrounds[currentIndex].url) || '';
      this.slideInterval = setInterval(() => {
        currentIndex = (currentIndex + 1) % backgrounds.length;
        this.currentBgUrl = this.themeService.formatUrl(backgrounds[currentIndex].url) || '';
      }, 5000);
    } else if (this.bgMode === 'RANDOM') {
      let currentIndex = Math.floor(Math.random() * backgrounds.length);
      this.currentBgUrl = this.themeService.formatUrl(backgrounds[currentIndex].url) || '';
      this.slideInterval = setInterval(() => {
        let nextIndex;
        do {
          nextIndex = Math.floor(Math.random() * backgrounds.length);
        } while (nextIndex === currentIndex && backgrounds.length > 1);
        currentIndex = nextIndex;
        this.currentBgUrl = this.themeService.formatUrl(backgrounds[currentIndex].url) || '';
      }, 5000);
    }
  }

  private clearBackgroundTimer(): void {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
      this.slideInterval = null;
    }
  }
}

