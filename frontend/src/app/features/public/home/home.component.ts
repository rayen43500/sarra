import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  protected readonly features = [
    {
      title: 'Emission certifiee',
      text: 'Certificats signes numeriquement, hash verifies et traçabilite complete.'
    },
    {
      title: 'Verification QR intelligente',
      text: 'Scan camera ou image, verification publique immediate avec statut dynamique.'
    },
    {
      title: 'Dashboard de pilotage',
      text: 'KPIs en temps reel, audit logs, gestion des roles et controles admin centralises.'
    }
  ];

  protected readonly stack = ['Angular', 'Spring Boot', 'MySQL', 'JWT', 'RBAC', 'PDF', 'QR'];

  protected readonly workflow = [
    {
      step: '01',
      title: 'Creation',
      text: 'L admin cree un certificat unique pour un utilisateur cible.'
    },
    {
      step: '02',
      title: 'Signature',
      text: 'Le certificat est signe, hashé et exporte en PDF avec QR intelligent.'
    },
    {
      step: '03',
      title: 'Verification',
      text: 'Le visiteur verifie publiquement via code ou scan QR en quelques secondes.'
    }
  ];
}
