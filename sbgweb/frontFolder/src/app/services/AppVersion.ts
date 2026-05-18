import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface AppVersion {
	major: string;
	minor: string;
	full: string;
}

@Injectable({
	providedIn: 'root'
})
export class VersionService {

	private readonly baseUrl = '/sbgweb/api/version';

	private _version = signal<AppVersion | null>(null);
	version = this._version.asReadonly();

	constructor(private http: HttpClient) {}

	load(): void {
		this.http.get<AppVersion>(this.baseUrl)
			.subscribe({
				next: v => this._version.set(v),
				error: err => {
					console.error('Erreur chargement version', err);
					this._version.set(null);
				}
			});
	}
}
