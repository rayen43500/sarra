import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const spaceRedirectGuard: CanActivateFn = (
  _route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (state.url !== '/app') {
    return true;
  }

  return authService.getRole() === 'ROLE_ADMIN'
    ? router.createUrlTree(['/app/admin'])
    : router.createUrlTree(['/app/client']);
};
