# Scenarios Client / Admin - CertifyHub

## Objectif
Documenter de maniere claire la logique fonctionnelle complete entre espaces Client/Admin/Public, incluant:
- parcours nominal
- cas d erreur
- securite
- etats UI
- roles et permissions
- logs et monitoring
- flux visuels

## Flux Visuels

### Flux 1 - Login et redirection par role
Utilisateur
-> Login
-> API Auth (token JWT)
-> Stockage session
-> Auth Guard
-> Role Guard
-> Redirect

ROLE_ADMIN -> Dashboard Admin
ROLE_CLIENT -> Dashboard Client

### Flux 2 - Examen et certification
Client
-> Chargement QCM
-> Demarrage timer
-> Soumission
-> Correction automatique
-> Verification score

Si score >= seuil
-> Resultat PASSED
-> Generation certificat
-> Notification succes

Si score < seuil
-> Resultat FAILED
-> Message echec
-> Option retry

## Regle Globale de Gestion d Etat (state management)
Chaque appel API doit gerer les 3 etats:
- loading: spinner ou skeleton + bouton desactive
- success: feedback explicite (message, badge, toast)
- error: message clair utilisateur + details techniques en console

## Scenario 1 - Authentification et Redirection

### Preconditions
- Le backend est demarre et accessible.
- Le compte utilisateur existe et possede un role actif.
- Le compte bloque ne doit pas pouvoir obtenir de token.

### Happy path
1. L utilisateur se connecte via /login.
2. Le JWT est stocke en session.
3. La route /app redirige automatiquement:
- ROLE_ADMIN -> /app/admin
- ROLE_CLIENT -> /app/client

### Cas d erreur
- Mauvais login (email/password invalides) -> message erreur login.
- Compte bloque -> acces refuse + message compte bloque.
- JWT expire -> redirection /login.
- 401 Unauthorized -> logout + redirection /login.
- 403 Forbidden (role mismatch) -> page Access Denied (403) ou redirection role-safe.
- 500 -> toast erreur + log console.

### Criteres d acceptation
- CA-LOGIN-01: un admin est toujours redirige vers /app/admin.
- CA-LOGIN-02: un client est toujours redirige vers /app/client.
- CA-LOGIN-03: sur token invalide, la session est purgee.

### Composants
- src/app/features/auth/login/login.component.ts
- src/app/core/guards/auth.guard.ts
- src/app/core/guards/space-redirect.guard.ts
- src/app/core/guards/role.guard.ts
- src/app/core/interceptors/auth.interceptor.ts

## Scenario 2 - Espace Admin

### Happy path
1. Voir dashboard admin.
2. Gerer utilisateurs (create, role, status, delete).
3. Voir certificats admin et telecharger PDF.
4. Creer un certificat via formulaire.
5. Lancer batch newsletter.
6. Gerer RDV clients (confirm/cancel).
7. Envoyer email/SMS (mode simule si provider indisponible).

### Cas d erreur
- API down -> service indisponible + bouton reessayer.
- Erreur creation certificat -> validation message + conservation formulaire.
- Echec telechargement PDF -> message erreur + bouton retry.
- 403 sur endpoints admin -> affichage access denied.

### Criteres d acceptation
- CA-ADMIN-01: un admin peut creer un certificat avec titre + client cible.
- CA-ADMIN-02: le PDF est telechargeable depuis la liste admin.
- CA-ADMIN-03: toute action create/update/delete est auditee backend.
- CA-ADMIN-04: en cas d erreur de chargement, un seul bloc erreur est affiche avec action reessayer.

### Composants
- src/app/features/admin/dashboard/admin-dashboard.component.ts
- src/app/features/admin/users/users.component.ts
- src/app/features/certificates/certificates.component.ts

### Services frontend
- src/app/core/services/user-admin.service.ts
- src/app/core/services/certificate.service.ts
- src/app/core/services/feature-api.service.ts

## Scenario 3 - Espace Client

### Happy path
1. Voir dashboard client.
2. Passer examens QCM et soumettre resultat.
3. Consulter certificats et telecharger PDF.
4. Verifier certificat par code/QR.
5. Like/dislike, rating, paiement simule, RDV, newsletter.

### Cas d erreur
- Session expiree pendant navigation -> redirection login.
- Echec soumission examen -> message + retry.
- Echec telechargement PDF -> message + retry.
- Echec paiement mock -> status FAILED + message explicite.
- Echec creation RDV -> message erreur + conservation des donnees.

### Criteres d acceptation
- CA-CLIENT-01: un client ne voit jamais les actions admin.
- CA-CLIENT-02: un client ne telecharge que ses propres certificats.
- CA-CLIENT-03: les actions critiques affichent un feedback explicite (success/error).

### Composants
- src/app/features/client/dashboard/client-dashboard.component.ts
- src/app/features/client/exams/client-exams.component.ts
- src/app/features/certificates/certificates.component.ts
- src/app/features/public/verification/verification.component.ts

### Services frontend
- src/app/core/services/exam-client.service.ts
- src/app/core/services/certificate.service.ts
- src/app/core/services/feature-api.service.ts

## Scenario 4 - Examen (detail critique)

### Sequence detaillee
1. Chargement des QCM actifs.
2. Initialisation du timer (si timer actif dans examen).
3. Selection des reponses.
4. Soumission des reponses.
5. Correction automatique backend.
6. Calcul score et statut PASSED/FAILED.
7. Si score >= seuil -> generation certificat + notification.
8. Si score < seuil -> affichage echec + proposition retry.

### Cas d erreur
- Timer atteint 0 -> auto-submit.
- Reponses incompletes -> confirmation avant submit.
- API indisponible a la soumission -> message + retry.

### Criteres d acceptation
- CA-EXAM-01: les questions sont chargees depuis le backend, pas hardcodees.
- CA-EXAM-02: la correction est faite cote backend sur les optionId reelles.
- CA-EXAM-03: score >= passScore => certificat auto + notification.
- CA-EXAM-04: score < passScore => pas de certificat + message retry.

## Scenario 5 - RDV et Paiement (detail)

### RDV
1. Client cree un RDV (date + note).
2. Admin consulte la liste.
3. Admin confirme ou annule.
4. Scheduler marque MISSED si depasse.

### Paiement mock
1. Client saisit montant + description.
2. API cree transaction en mode SIMULATED.
3. Retour statut SUCCESS ou FAILED.
4. Affichage reference transaction.

### Cas d erreur
- Montant invalide -> validation front + backend.
- API down -> service indisponible + retry.

### Criteres d acceptation
- CA-RDV-01: creation RDV persistee avec statut REQUESTED.
- CA-RDV-02: admin peut CONFIRMED/CANCELLED.
- CA-PAY-01: paiement mock retourne reference + statut SUCCESS/FAILED.

## Scenario 6 - Verification Publique

### Happy path
1. Acces sans authentification via /verify.
2. Verification par code texte, scan camera ou image QR.
3. Appel backend GET /api/public/verify/{code}.
4. Affichage statut VALID, REVOKED, EXPIRED, TAMPERED, NOT_FOUND.

### Cas d erreur
- Code invalide -> message utilisateur clair.
- Certificat expire -> statut EXPIRED.
- Certificat revoque -> statut REVOKED.
- Scan QR echoue (mobile/camera) -> fallback saisie manuelle.
- Backend indisponible -> message service indisponible.

### Criteres d acceptation
- CA-PUBLIC-01: verification sans authentification.
- CA-PUBLIC-02: statut retourne parmi VALID, REVOKED, EXPIRED, TAMPERED, NOT_FOUND.
- CA-PUBLIC-03: le composant supporte code texte + scan camera + upload image.

## Securite

### Regles obligatoires
- Tous les endpoints prives exigent JWT valide.
- Interceptor frontend ajoute Authorization: Bearer token automatiquement.
- Guards frontend protegent routes sensibles.
- Verification role cote frontend pour UX.
- Verification role cote backend pour securite reelle.
- Validation backend des DTO (NotBlank, Email, Size, etc.).
- Sanitization input pour champs texte (bad words/filter).

### Cas d erreur standardises
- 401 Unauthorized -> redirection login.
- 403 Forbidden -> affichage access denied.
- 500 Internal Error -> toast error + log console.

## Roles et Permissions

### Roles supportes
- ROLE_ADMIN: administration complete.
- ROLE_CLIENT: parcours utilisateur final.
- ROLE_SUPER_ADMIN (option PFE): supervision multi-admin.

### Verification des permissions
- Frontend: affichage/masquage des actions (UX).
- Backend: controle d acces final (autorite).

## Logs et Monitoring

### Frontend
- Logs console pour erreurs API critiques.
- Trace des erreurs utilisateur (chargement, submit, download).

### Backend
- Logs techniques (niveau service, AOP timing).
- Audit actions admin (create/update/delete/revoke).
- Suivi batch et scheduler.

## Pagination, Recherche et Filtrage

### Cibles UX
- Users: pagination + recherche email/nom + filtre status/role.
- Certificates: pagination + recherche code/titre + filtre status/date.

### Comportement attendu
- Etat loading pendant fetch.
- Etat vide sans erreur si aucun resultat.
- Etat error si API indisponible.

## Notifications

### Minimum requis
- Toast success/error pour actions principales.
- Messages inline de confirmation.

### Optionnel avance
- Notifications temps reel via websocket.

## Versioning API

### Cible recommandee
- /api/v1/admin/users
- /api/v1/admin/certificates
- /api/v1/client/exams
- /api/v1/client/results
- /api/v1/public/verify/{code}

### Strategie
- Conserver routes actuelles pendant transition.
- Introduire v1 de maniere progressive.

### Plan de migration propose (3 etapes)
1. Exposer routes paralleles /api/v1/** sans supprimer /api/**.
2. Basculer frontend vers /api/v1/** par module (auth, examens, certificats).
3. Deprecier anciennes routes avec delai et journalisation d usage.

## Endpoints Backend (etat actuel)

### Admin
- /api/admin/users
- /api/admin/certificates
- /api/admin/features/**

### Client
- /api/client/certificates
- /api/client/exams
- /api/client/results
- /api/client/features/**

### Public
- /api/public/verify/{code}
- /api/public/newsletter/*
- /api/public/rss/certificates
- /api/public/social/providers

## Contrats API (exemples utiles soutenance)

### 1) Login
POST /api/auth/login

Request (JSON):
{
	"email": "admin@cert.local",
	"password": "Admin@123"
}

Response 200 (JSON):
{
	"token": "<jwt>",
	"email": "admin@cert.local",
	"role": "ROLE_ADMIN"
}

### 2) Creation certificat admin
POST /api/admin/certificates

Request (JSON):
{
	"title": "Spring Boot Security",
	"description": "Certification backend",
	"issuedToUserId": 5,
	"issueDate": "2026-04-16",
	"expiryDate": "2027-04-16"
}

Response 200 (JSON):
{
	"id": 12,
	"code": "CERT-AB12CD34",
	"title": "Spring Boot Security",
	"holder": "Client Demo",
	"issueDate": "2026-04-16",
	"expiryDate": "2027-04-16",
	"status": "ACTIVE",
	"pdfPath": "generated/pdfs/certificate-CERT-AB12CD34.pdf"
}

### 3) Chargement questions examen
GET /api/client/exams/{examId}/questions

Response 200 (JSON):
[
	{
		"id": 101,
		"text": "Quel composant protege les routes Angular ?",
		"points": 1.0,
		"orderIndex": 1,
		"options": [
			{ "id": 201, "text": "Interceptor" },
			{ "id": 202, "text": "Guard" }
		]
	}
]

### 4) Soumission reponses examen
POST /api/client/results/answers

Request (JSON):
{
	"examId": 1,
	"attemptNumber": 2,
	"answers": [
		{ "questionId": 101, "optionId": 202 },
		{ "questionId": 102, "optionId": 206 }
	]
}

Response 200 (JSON):
{
	"id": 44,
	"examId": 1,
	"examTitle": "QCM Angular Secure Frontend",
	"score": 2.0,
	"maxScore": 3.0,
	"percentage": 66.67,
	"passed": false,
	"submittedAt": "2026-04-16T10:20:31Z"
}

## Matrice de tests (minimum)

### Auth
- T-AUTH-01: login admin valide -> ROLE_ADMIN + redirection admin.
- T-AUTH-02: login client valide -> ROLE_CLIENT + redirection client.
- T-AUTH-03: login invalide -> message erreur.
- T-AUTH-04: token expire -> redirection login.

### Certificats
- T-CERT-01: list admin OK.
- T-CERT-02: list client limitee a ses certificats.
- T-CERT-03: PDF introuvable -> erreur + retry.

### Examens
- T-EXAM-01: chargement questions dynamique.
- T-EXAM-02: timer auto-submit.
- T-EXAM-03: score sous seuil -> pas de certificat.
- T-EXAM-04: score au-dessus seuil -> certificat auto.

### Public verify
- T-PUB-01: code valide -> VALID.
- T-PUB-02: code inconnu -> NOT_FOUND.
- T-PUB-03: cert revoque -> REVOKED.

## Non-fonctionnel (NFR) a presenter
- Performance: p95 API critique < 800 ms en local.
- Disponibilite: messages fallback clairs en cas de panne.
- Traçabilite: chaque action admin sensible est auditee.
- Securite: aucune route privee accessible sans JWT.

## Edge Cases Critiques
- Token valide mais role incoherent -> deny + redirection sure.
- Double clic soumission -> bouton desactive pendant loading.
- Requete lente -> timeout UX + message.
- Refresh page sur route protegee -> revalidation session.
- PDF non trouve -> message explicite + retry.

## Checklist Finale PFE
- Scenarios nominaux couverts
- Cas d erreur couverts
- Securite JWT/roles documentee
- Flux visuels presents
- Etats loading/success/error definis
- API detaillee et versioning planifie
- Roles et permissions clairs
- Logs et audit decrits
- Edge cases identifies
- Contrats API exemples ajoutes
- Matrice de tests minimum definie
- Criteres d acceptation formels (CA-*) ajoutes
