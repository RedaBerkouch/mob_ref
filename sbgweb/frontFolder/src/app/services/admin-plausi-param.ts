import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { WebFilterParameter } from './admin-filter';

export interface ParameterListResult {
	message?: string;
	state: number;
	parameters?: WebFilterParameter[];
}

export interface ParameterResult {
	message?: string;
	state: number;
	parameter?: WebFilterParameter;
}

@Injectable({
	providedIn: 'root',
})
export class AdminPlausiParamService {
	private readonly baseUrl = '/sbgweb/api/admin/macros';

	private _parameters = signal<WebFilterParameter[]>([]);
	parameters = this._parameters.asReadonly();

	constructor(private http: HttpClient) {}

	loadAll(plausiId: number): void {
		this.http.get<ParameterListResult>(`${this.baseUrl}/${plausiId}/parameters`).subscribe({
			next: (result) => {
				if (result?.state === 1 && Array.isArray(result.parameters)) {
					this._parameters.set(result.parameters);
				} else {
					console.warn('Réponse inattendue paramètres plausi', result);
					this._parameters.set([]);
				}
			},
			error: (err) => {
				console.error('Erreur chargement paramètres plausi', err);
				this._parameters.set([]);
			},
		});
	}

	getById(plausiId: number, paramId: number): Observable<WebFilterParameter | undefined> {
		return this.http
			.get<ParameterResult>(`${this.baseUrl}/${plausiId}/parameters/${paramId}`)
			.pipe(map((result) => result.parameter));
	}

	create(plausiId: number, param: WebFilterParameter): void {
		this.http.post<ParameterResult>(`${this.baseUrl}/${plausiId}/parameters`, param).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.parameter) {
					this._parameters.set([...this._parameters(), result.parameter]);
				} else {
					console.warn('Réponse inattendue création param plausi', result);
				}
			},
			error: (err) => console.error('Erreur création param plausi', err),
		});
	}

	update(plausiId: number, paramId: number, param: WebFilterParameter): void {
		this.http
			.put<ParameterResult>(`${this.baseUrl}/${plausiId}/parameters/${paramId}`, param)
			.subscribe({
				next: (result) => {
					if (result?.state === 1 && result.parameter) {
						this._parameters.set(
							this._parameters().map((p) => (p.parameterId === paramId ? result.parameter! : p)),
						);
					} else {
						console.warn('Réponse inattendue update param plausi', result);
					}
				},
				error: (err) => console.error('Erreur update param plausi', err),
			});
	}

	delete(plausiId: number, paramId: number): void {
		this.http
			.delete<{
				message?: string;
				state: number;
			}>(`${this.baseUrl}/${plausiId}/parameters/${paramId}`)
			.subscribe({
				next: (result) => {
					if (result?.state === 1) {
						this._parameters.set(this._parameters().filter((p) => p.parameterId !== paramId));
					} else {
						console.warn('Réponse inattendue suppression param plausi', result);
					}
				},
				error: (err) => console.error('Erreur suppression param plausi', err),
			});
	}
}
