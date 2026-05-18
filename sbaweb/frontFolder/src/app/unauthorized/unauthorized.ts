import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-unauthorized',
	standalone: false,
	templateUrl: './unauthorized.html',
	styleUrl: './unauthorized.scss',
})
export class Unauthorized {
	private readonly router = inject(Router);

	goHome(): void {
		this.router.navigate(['/']);
	}
}
