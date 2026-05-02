import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GeminiChatService, ChatMessage } from '../../../core/services/gemini-chat.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-chat.component.html',
  styleUrl: './admin-chat.component.css'
})
export class AdminChatComponent implements OnInit {
  messages: ChatMessage[] = [];
  inputText = '';
  isLoading = false;
  hasError = false;

  readonly suggestions = [
    'Comment créer un certificat ?',
    'Combien d\'utilisateurs sont actifs ?',
    'Comment configurer le thème ?',
    'Comment envoyer un email groupé ?'
  ];

  constructor(
    private readonly geminiService: GeminiChatService,
    public readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.messages = [{
      role: 'model',
      content: '👋 Bonjour ! Je suis l\'assistant IA de **CertifyHub**. Je peux vous aider avec la gestion des certificats, des utilisateurs, du thème ou toute autre question sur la plateforme. Comment puis-je vous aider ?',
      timestamp: new Date()
    }];
  }

  sendMessage(): void {
    const text = this.inputText.trim();
    if (!text || this.isLoading) return;

    const userMsg: ChatMessage = { role: 'user', content: text, timestamp: new Date() };
    this.messages.push(userMsg);
    this.inputText = '';
    this.isLoading = true;
    this.hasError = false;

    const history = this.messages.slice(1, -1); // exclude first greeting and latest user msg

    this.geminiService.sendMessage(text, history).subscribe({
      next: (reply) => {
        this.messages.push({ role: 'model', content: reply, timestamp: new Date() });
        this.isLoading = false;
        setTimeout(() => this.scrollToBottom(), 50);
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
        this.messages.push({
          role: 'model',
          content: '❌ Une erreur est survenue. Vérifiez votre connexion internet et réessayez.',
          timestamp: new Date()
        });
      }
    });
  }

  sendSuggestion(text: string): void {
    this.inputText = text;
    this.sendMessage();
  }

  clearChat(): void {
    this.ngOnInit();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    const el = document.getElementById('chat-messages');
    if (el) el.scrollTop = el.scrollHeight;
  }

  formatMessage(content: string): string {
    return content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>');
  }
}
