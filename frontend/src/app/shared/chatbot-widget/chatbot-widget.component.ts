import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FeatureApiService, SocialProviders } from '../../core/services/feature-api.service';
import { AuthService } from '../../core/services/auth.service';
import { GeminiChatService, ChatMessage as GeminiMessage } from '../../core/services/gemini-chat.service';
import { CommonModule } from '@angular/common';

interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
}

@Component({
  selector: 'app-chatbot-widget',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './chatbot-widget.component.html',
  styleUrl: './chatbot-widget.component.css'
})
export class ChatbotWidgetComponent {
  protected chatOpen = false;
  protected chatInput = '';
  protected chatLoading = false;
  protected newsletterEmail = '';
  protected helperMessage = '';
  protected socialProviders: SocialProviders | null = null;
  protected messages: ChatMessage[] = [
    {
      sender: 'bot',
      text: 'Bonjour ! Je suis l\'assistant intelligent de CertifyHub. Je peux vous guider dans le choix de vos certifications ou répondre à vos questions sur la plateforme.'
    }
  ];

  constructor(
    private readonly featureApiService: FeatureApiService,
    private readonly authService: AuthService,
    private readonly geminiService: GeminiChatService
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
      security: 'Recommandation : Le parcours "Security Master". Il couvre JWT, RBAC et la sécurisation des endpoints Spring Boot.',
      frontend: 'Recommandation : Le parcours "Frontend Expert". Focus sur Angular, l\'UX premium et le responsive design.',
      backend: 'Recommandation : Le parcours "Backend Pro". Apprenez à gérer les signatures PDF, les codes QR et les APIs complexes.',
      database: 'Recommandation : Le parcours "Data Integrity". Maîtrisez MySQL, l\'audit des données et les performances.'
    };

    this.pushBotMessage(advice[track]);
  }

  sendMessage(): void {
    const content = this.chatInput.trim();
    if (!content || this.chatLoading) return;

    this.messages.push({ sender: 'user', text: content });
    this.chatInput = '';
    this.chatLoading = true;

    // Convert to Gemini history
    const history: GeminiMessage[] = this.messages
      .filter(m => !m.text.includes('Bonjour !'))
      .map(m => ({
        role: m.sender === 'user' ? 'user' : 'model',
        content: m.text,
        timestamp: new Date()
      }));

    this.geminiService.sendMessage(content, history).subscribe({
      next: (reply) => {
        this.pushBotMessage(reply);
        this.chatLoading = false;
      },
      error: () => {
        this.pushBotMessage('Désolé, je rencontre une difficulté de connexion. ' + this.getAssistantReply(content));
        this.chatLoading = false;
      }
    });
  }

  subscribeNewsletter(): void {
    if (!this.newsletterEmail.trim()) {
      this.helperMessage = 'Entrez un email valide.';
      return;
    }

    this.featureApiService.subscribeNewsletter(this.newsletterEmail.trim()).subscribe({
      next: (response) => {
        this.helperMessage = response.message;
        setTimeout(() => this.helperMessage = '', 3000);
      },
      error: () => {
        this.helperMessage = 'Service indisponible.';
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
    if (input.includes('secur')) return 'Je recommande le parcours Security : JWT, RBAC et vérification publique.';
    if (input.includes('front')) return 'Je recommande Angular Professional pour le Frontend.';
    if (input.includes('back')) return 'Spring Boot Security est le meilleur choix pour le Backend.';
    if (input.includes('data')) return 'MySQL Data Integrity est idéal pour la gestion des données.';
    return 'Dites-moi votre objectif (sécurité, frontend, backend) et je vous guiderai.';
  }
}
