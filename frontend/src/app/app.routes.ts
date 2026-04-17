import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { spaceRedirectGuard } from './core/guards/space-redirect.guard';
import { LoginComponent } from './features/auth/login/login.component';
import { AdminDashboardComponent } from './features/admin/dashboard/admin-dashboard.component';
import { ClientDashboardComponent } from './features/client/dashboard/client-dashboard.component';
import { CertificatesComponent } from './features/certificates/certificates.component';
import { VerificationComponent } from './features/public/verification/verification.component';
import { HomeComponent } from './features/public/home/home.component';
import { UsersComponent } from './features/admin/users/users.component';
import { ClientExamsComponent } from './features/client/exams/client-exams.component';
import { MainLayoutComponent } from './layout/main-layout.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent },
	{ path: 'login', component: LoginComponent },
	{ path: 'verify', component: VerificationComponent },
	{
		path: 'app',
		component: MainLayoutComponent,
		canActivate: [authGuard, spaceRedirectGuard],
		children: [
			{
				path: 'admin',
				component: AdminDashboardComponent,
				canActivate: [roleGuard],
				data: { role: 'ROLE_ADMIN' }
			},
			{
				path: 'users',
				component: UsersComponent,
				canActivate: [roleGuard],
				data: { role: 'ROLE_ADMIN' }
			},
			{
				path: 'client',
				component: ClientDashboardComponent,
				canActivate: [roleGuard],
				data: { role: 'ROLE_CLIENT' }
			},
			{
				path: 'exams',
				component: ClientExamsComponent,
				canActivate: [roleGuard],
				data: { role: 'ROLE_CLIENT' }
			},
			{
				path: 'certificates',
				component: CertificatesComponent,
				canActivate: [roleGuard],
				data: { roles: ['ROLE_ADMIN', 'ROLE_CLIENT'] }
			},
			{ path: '', pathMatch: 'full', redirectTo: 'client' }
		]
	},
	{ path: '**', redirectTo: '' }
];
