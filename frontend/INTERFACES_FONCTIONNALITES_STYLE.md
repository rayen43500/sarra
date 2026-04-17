# Catalogue Interfaces, Fonctionnalites et Style

## 1) Interfaces disponibles

### Home page publique
- Route: `/`
- Objectif: Presenter la plateforme, mettre en avant les points forts, guider vers connexion et verification.
- Elements principaux:
  - Hero marketing
  - Statistiques visuelles
  - Cartes de fonctionnalites
  - Boutons CTA: `Demarrer maintenant`, `Scanner un QR`, `Connexion`, `Verifier un certificat`

### Login
- Route: `/login`
- Objectif: Authentifier l utilisateur via API JWT Spring Boot.
- Elements principaux:
  - Formulaire email/mot de passe
  - Message d erreur
  - Lien vers verification publique

### Verification publique
- Route: `/verify`
- Objectif: Verifier un certificat sans connexion.
- Modes de verification:
  - Saisie manuelle du code
  - Scan QR via camera
  - Scan QR via image importee
- Affichage resultat:
  - Titulaire
  - Date
  - Statut
  - Validite

### Layout prive (dashboard)
- Route parent: `/app`
- Structure:
  - Sidebar gauche
  - Topbar haut
  - Zone contenu dynamique
- Navigation:
  - Dashboard Admin
  - Dashboard Client
  - Certificats
  - Verification

### Dashboard Admin
- Route: `/app/admin`
- Objectif: Suivi central admin
- Sections:
  - KPIs (delivres/actifs/expires/revoques)
  - Actions admin
  - Logs recents

### Dashboard Client
- Route: `/app/client`
- Objectif: Espace client
- Sections:
  - Bloc de bienvenue
  - Resume personnel
  - Actions recommandees

### Certificats
- Route: `/app/certificates`
- Objectif: Lister les certificats et executer des actions.
- Actions par certificat:
  - PDF (telechargement reel depuis backend)
  - Details
  - Verifier (redirige vers verification publique avec code pre-rempli)

### Utilisateurs (Admin)
- Route: `/app/users`
- Objectif: CRUD utilisateurs + role + activation/desactivation.

### Examens QCM (Client)
- Route: `/app/exams`
- Objectif: Passer un QCM avec timer, correction automatique, soumission API.

## 2) Fonctionnalites et logique des boutons

### Boutons avec logique active
- Home:
  - `Connexion` -> `/login`
  - `Verifier un certificat` -> `/verify`
  - `Demarrer maintenant` -> `/login`
  - `Scanner un QR` -> `/verify`
- Login:
  - `Se connecter` -> authentification JWT reelle puis redirection dynamique selon role
- Layout:
  - Menu sidebar -> navigation routee
  - `Se deconnecter` -> suppression session + retour login
- Verification:
  - `Verifier` -> verification par code
  - `Demarrer scan camera` -> demarrage camera QR
  - `Arreter scan` -> arret camera
  - `Importer image QR` -> decode QR depuis fichier image
- Admin dashboard:
  - `Ajouter un utilisateur` -> message logique module users
  - `Creer un certificat` -> navigation vers page certificats
  - `Creer un examen` -> message logique module examens
- Client dashboard:
  - `Mes certificats` -> navigation vers certificats
  - `Passer un examen` -> message logique module examens
- Certificats:
  - `Nouveau certificat` -> message logique create endpoint
  - `PDF` -> telechargement reel via `/api/client/certificates/{id}/pdf`
  - `Details` -> details textuels du certificat
  - `Verifier` -> ouverture page verification avec code dans query param
- Utilisateurs:
  - `Creer utilisateur` -> endpoint reel admin
  - `Role Client` / `Role Admin` -> endpoint reel assignation role
  - `Activer/Desactiver` -> endpoint reel status
  - `Supprimer` -> endpoint reel suppression
- Examens QCM:
  - `Demarrer` -> lance timer
  - `Soumettre` -> calcule score + POST `/api/client/results`

## 3) Style system applique

### Palette
- Primary: `#2563EB`
- Secondary: `#1E40AF`
- Success: `#16A34A`
- Danger: `#DC2626`
- Warning: `#F59E0B`
- Background: `#F8FAFC`
- Card: `#FFFFFF`
- Text main: `#0F172A`
- Text secondary: `#475569`
- Border: `#E2E8F0`

### Regles UI
- Boutons:
  - Radius `12px`
  - Padding `10px 16px`
  - Font weight `600`
  - Hover: assombrissement + ombre
- Inputs:
  - Radius `10px`
  - Focus: bordure bleue + ring
- Cards:
  - Radius `16px`
  - Bordure legere
  - Shadow douce
  - Padding `20px`
- Layout:
  - Sidebar gauche
  - Navbar haute
  - Contenu central
  - Responsive mobile/tablette

### Typographie
- Police principale: `Manrope`
- Titres: `Sora`
- Hierarchie visuelle renforcee (taille/poids/espacement)

## 4) Etat integration backend

### Endpoints prets cote backend
- Auth: `/api/auth/login`, `/api/auth/register`
- Admin: `/api/admin/*`
- Client: `/api/client/*`
- Public verification: `/api/public/verify/{code}`

### Points de securite implementes
- JWT reel Spring Boot
- Storage token en `sessionStorage`
- Guard Angular avec verification expiration JWT
- Interceptor Angular qui injecte `Authorization: Bearer ...`
- Auto logout en cas de 401
- Hash/signature certificat verifie cote backend
- Statut dynamique certificat: ACTIVE/EXPIRED/REVOKED (+ verification TAMPERED)

### A finaliser pour production
- Remplacer les actions simulees par appels API reels pour:
  - CRUD utilisateurs
  - Creation examen UI
  - Telechargement PDF reel
  - Messages toast et confirmations

## 5) Fichiers principaux a connaitre

- `src/app/features/public/home/*`
- `src/app/features/public/verification/*`
- `src/app/features/auth/login/*`
- `src/app/features/admin/dashboard/*`
- `src/app/features/client/dashboard/*`
- `src/app/features/certificates/*`
- `src/app/layout/main-layout.component.*`
- `src/app/core/services/*`
- `src/app/core/guards/*`
- `src/app/core/interceptors/*`
- `src/styles.css`

## 6) Donnees de test et authentification

### Comptes de test backend (seed automatique)
- `admin@cert.local / Admin@123` -> role admin
- `client@cert.local / Client@123` -> role client
- `blocked@cert.local / Blocked@123` -> client desactive (status BLOCKED)

### Donnees de test metier
- Examens seedes:
  - `QCM Angular Secure Frontend`
  - `QCM Spring Boot Security API`
- Certificats seedes:
  - `CERT-DEMO-ACTIVE`
  - `CERT-DEMO-EXPIRED`
  - `CERT-DEMO-REVOKED`

### Flux auth dans l application
- Login Angular appelle `POST /api/auth/login`
- JWT stocke en `sessionStorage`
- Interceptor injecte `Authorization: Bearer <token>`
- Guards protegent les routes selon auth + role
