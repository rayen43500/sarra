import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService, SocialProviders } from '../../core/services/feature-api.service';
import { AuthService } from '../../core/services/auth.service';

interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.css'
})
export class ChatbotWidgetComponent {
  protected chatOpen = false;
  protected chatInput = '';
  protected newsletterEmail = '';
  protected helperMessage = '';
  protected socialProviders: SocialProviders | null = null;
  protected messages: ChatMessage[] = [
    {
      sender: 'bot',
      text: 'Bonjour. Je peux t aider a choisir la meilleure certification selon ton profil.'
    }
  ];

  constructor(
    private readonly featureApiService: FeatureApiService,
    private readonly authService: AuthService
  ) {
    this.newsletterEmail = this.authService.getEmail();
    this.featureApiService.getSocialProviders().subscribe({
      next: (providers) => {
        this.socialProviders = providers;
      }
    });
  }

  toggleChat(): void {
    this.chatOpen = !this.chatOpen;
  }

  chooseCertificate(track: 'security' | 'frontend' | 'backend' | 'database'): void {
    const advice: Record<string, string> = {
      security: 'Recommendation: Certification Platform Security + examen JWT/RBAC pour renforcer la confiance.',
      frontend: 'Recommendation: Certification Angular Professional avec focus UX, guards et interceptors.',
      backend: 'Recommendation: Certification Spring Boot Security API avec PDF/QR/verifications publiques.',
      database: 'Recommendation: Certification MySQL Data Integrity pour logs, audit et tracabilite.'
    };

    this.pushBotMessage(advice[track]);
  }

  sendMessage(): void {
    const content = this.chatInput.trim();
    if (!content) {
      return;
    }

    this.messages.push({ sender: 'user', text: content });
    this.chatInput = '';
    this.pushBotMessage(this.getAssistantReply(content));
  }

  subscribeNewsletter(): void {
    if (!this.newsletterEmail.trim()) {
      this.helperMessage = 'Entrez un email valide pour la newsletter.';
      return;
    }

    this.featureApiService.subscribeNewsletter(this.newsletterEmail.trim()).subscribe({
      next: (response) => {
        this.helperMessage = response.message;
      },
      error: () => {
        this.helperMessage = 'Abonnement impossible pour le moment.';
      }
    });
  }

  socialAuthUrl(providerUrl: string): string {
    return `http://localhost:8080${providerUrl}`;
  }

  private pushBotMessage(text: string): void {
    this.messages.push({ sender: 'bot', text });
  }

  private getAssistantReply(message: string): string {
    const input = message.toLowerCase();

    if (input.includes('secur') || input.includes('jwt') || input.includes('rbac')) {
      return 'Je recommande le parcours Security: JWT, RBAC, logs et verification publique QR.';
    }
    if (input.includes('front') || input.includes('angular') || input.includes('ui')) {
      return 'Je recommande la certification Angular Professional avec focus guards, interceptor, UX et responsive design.';
    }
    if (input.includes('back') || input.includes('spring') || input.includes('api')) {
      return 'Je recommande la certification Spring Boot API Security avec PDF reel, signature et QR intelligent.';
    }
    if (input.includes('data') || input.includes('sql') || input.includes('mysql')) {
      return 'Je recommande la certification MySQL Data Integrity pour modelisation, audit et performance des donnees.';
    }
    if (input.includes('exam') || input.includes('qcm')) {
      return 'Pour reussir les QCM: commence par les modules Security puis pratique sur les examens chronometres.';
    }
    if (input.includes('pdf') || input.includes('certificat')) {
      return 'Le systeme genere des certificats PDF avec identifiant unique, QR et verification cote backend.';
    }
    if (input.includes('qr') || input.includes('verify')) {
      return 'La verification publique se fait via code ou scan QR camera/image sur la page /verify.';
    }

    return 'Dis-moi ton objectif (securite, frontend, backend, base de donnees, examens) et je te propose le meilleur parcours.';
  }
}
