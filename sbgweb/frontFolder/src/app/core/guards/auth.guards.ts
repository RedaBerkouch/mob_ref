import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs/operators';
import { UserService } from '../../services/user';

export const dataDeliveryGuard: CanActivateFn = () => {
	const userService = inject(UserService);
	const router = inject(Router);
	return userService.userInfo$.pipe(
		take(1),
		map(() =>
			userService.canAccessDataDelivery() ? true : router.createUrlTree(['/unauthorized']),
		),
	);
};

export const dataTrackingGuard: CanActivateFn = () => {
	const userService = inject(UserService);
	const router = inject(Router);

	return userService.userInfo$.pipe(
		take(1),
		map(() =>
			userService.canAccessDataTracking() ? true : router.createUrlTree(['/unauthorized']),
		),
	);
};

export const dataProcessingGuard: CanActivateFn = () => {
	const userService = inject(UserService);
	const router = inject(Router);

	return userService.userInfo$.pipe(
		take(1),
		map(() =>
			userService.canAccessDataProcessing() ? true : router.createUrlTree(['/unauthorized']),
		),
	);
};

export const adminGuard: CanActivateFn = () => {
	const userService = inject(UserService);
	const router = inject(Router);

	return userService.userInfo$.pipe(
		take(1),
		map(() =>
			userService.canAccessAdministration() ? true : router.createUrlTree(['/unauthorized']),
		),
	);
};
