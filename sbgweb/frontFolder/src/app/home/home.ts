import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
	selector: 'app-home',
	template: '',
	standalone: false,
})
export class Home implements OnInit {
	private router = inject(Router);

	ngOnInit(): void {
		this.router.navigateByUrl('/data-delivery');
	}
}
