import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface ChatMessage {
  role: 'user' | 'model';
  content: string;
  timestamp: Date;
}

interface GeminiPart { text: string; }
interface GeminiContent { role: string; parts: GeminiPart[]; }
interface GeminiResponse {
  candidates: { content: { parts: GeminiPart[] } }[];
}

@Injectable({ providedIn: 'root' })
export class GeminiChatService {
  private readonly API_KEY = 'AIzaSyBNn3BVrDz9KZmDk_KnFSvEPlfd5OLzb8c';
  private readonly BASE_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent';

  private readonly SYSTEM_CONTEXT = `Tu es l'assistant IA de CertifyHub, une plateforme de gestion de certificats numériques.
Tu aides les administrateurs et les clients avec :
- La gestion et la création de certificats numériques
- La vérification de certificats par code QR
- La gestion des examens et des résultats
- Les questions sur les utilisateurs et les rôles (ROLE_ADMIN, ROLE_CLIENT)
- Les paramètres de thème et de personnalisation visuelle
- L'envoi de communications par email ou SMS
- La gestion des rendez-vous clients

Réponds toujours en français, de manière concise et professionnelle. Si la question sort du contexte CertifyHub, redirige poliment vers les fonctionnalités de la plateforme.`;

  constructor(private readonly http: HttpClient) { }

  sendMessage(userMessage: string, history: ChatMessage[]): Observable<string> {
    const contents: GeminiContent[] = [
      { role: 'user', parts: [{ text: this.SYSTEM_CONTEXT }] },
      { role: 'model', parts: [{ text: 'Compris ! Je suis prêt à vous assister sur CertifyHub. Comment puis-je vous aider ?' }] },
      ...history.map(msg => ({
        role: msg.role,
        parts: [{ text: msg.content }]
      })),
      { role: 'user', parts: [{ text: userMessage }] }
    ];

    const body = { contents, generationConfig: { temperature: 0.7, maxOutputTokens: 1024 } };
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    return this.http.post<GeminiResponse>(
      `${this.BASE_URL}?key=${this.API_KEY}`,
      body,
      { headers }
    ).pipe(
      map(res => res.candidates?.[0]?.content?.parts?.[0]?.text ?? 'Désolé, je n\'ai pas pu générer de réponse.')
    );
  }
}
