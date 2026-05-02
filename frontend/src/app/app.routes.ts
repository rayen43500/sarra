import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { spaceRedirectGuard } from './core/guards/space-redirect.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { SignupComponent } from './features/auth/signup/signup.component';
import { AdminDashboardComponent } from './features/admin/dashboard/admin-dashboard.component';
import { AdminCommunicationsComponent } from './features/admin/communications/admin-communications.component';
import { AdminAppointmentsComponent } from './features/admin/appointments/admin-appointments.component';
import { AdminBatchComponent } from './features/admin/batch/admin-batch.component';
import { AdminThemeComponent } from './features/admin/theme/admin-theme.component';
import { AdminChatComponent } from './features/admin/chat/admin-chat.component';
import { AdminQuizComponent } from './features/admin/quiz/admin-quiz.component';
import { AdminProfileComponent } from './features/admin/profile/admin-profile.component';
import { ClientDashboardComponent } from './features/client/dashboard/client-dashboard.component';
import { ClientCommunityComponent } from './features/client/community/client-community.component';
import { ClientPaymentsComponent } from './features/client/payments/client-payments.component';
import { ClientAppointmentsComponent } from './features/client/appointments/client-appointments.component';
import { ClientNewsletterComponent } from './features/client/newsletter/client-newsletter.component';
import { ClientHistoryComponent } from './features/client/history/client-history.component';
import { ClientProfileComponent } from './features/client/profile/client-profile.component';
import { CertificatesComponent } from './features/certificates/certificates.component';
import { VerificationComponent } from './features/public/verification/verification.component';
import { HomeComponent } from './features/public/home/home.component';
import { UsersComponent } from './features/admin/users/users.component';
import { ClientExamsComponent } from './features/client/exams/client-exams.component';
import { MainLayoutComponent } from './layout/main-layout.component';

const ADMIN = { role: 'ROLE_ADMIN' };

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'signup', component: SignupComponent },
	{ path: 'verify', component: VerificationComponent },
	{
		path: 'app',
		component: MainLayoutComponent,
		canActivate: [authGuard, spaceRedirectGuard],
		children: [
			// Admin routes
			{ path: 'admin', component: AdminDashboardComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/profile', component: AdminProfileComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/communications', component: AdminCommunicationsComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/appointments', component: AdminAppointmentsComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/batch', component: AdminBatchComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/theme', component: AdminThemeComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/chat', component: AdminChatComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'admin/quiz', component: AdminQuizComponent, canActivate: [roleGuard], data: ADMIN },
			// Shared routes
			{ path: 'users', component: UsersComponent, canActivate: [roleGuard], data: ADMIN },
			{ path: 'certificates', component: CertificatesComponent, canActivate: [roleGuard], data: { roles: ['ROLE_ADMIN', 'ROLE_CLIENT'] } },
			// Client routes
			{ path: 'client', component: ClientDashboardComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/community', component: ClientCommunityComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/payments', component: ClientPaymentsComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/appointments', component: ClientAppointmentsComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/newsletter', component: ClientNewsletterComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/history', component: ClientHistoryComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'client/profile', component: ClientProfileComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: 'exams', component: ClientExamsComponent, canActivate: [roleGuard], data: { role: 'ROLE_CLIENT' } },
			{ path: '', pathMatch: 'full', redirectTo: 'client' }
		]
	},
	{ path: '**', redirectTo: '' }
];
