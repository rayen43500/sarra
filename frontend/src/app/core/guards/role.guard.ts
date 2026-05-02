import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRole = route.data['role'] as string | undefined;
  const requiredRoles = route.data['roles'] as string[] | undefined;

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (requiredRoles && requiredRoles.includes(authService.getRole())) {
    return true;
  }

  if (!requiredRoles && (!requiredRole || authService.getRole() === requiredRole)) {
    return true;
  }

  return authService.getRole() === 'ROLE_ADMIN'
    ? router.createUrlTree(['/app/admin'])
    : router.createUrlTree(['/app/client']);
};
