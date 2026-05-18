import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
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
export class AdminFilterParamService {
	private readonly baseUrl = '/sbgweb/api/admin/filters';

	private _parameters = signal<WebFilterParameter[]>([]);
	parameters = this._parameters.asReadonly();

	constructor(private http: HttpClient) {}

	/** Charge tous les paramètres d’un filtre */
	loadAll(filterId: number): void {
		this.http.get<ParameterListResult>(`${this.baseUrl}/${filterId}/parameters`).subscribe({
			next: (result) => {
				if (result?.state === 1 && Array.isArray(result.parameters)) {
					this._parameters.set(result.parameters);
				} else {
					console.warn('Réponse inattendue lors du chargement des paramètres filtre', result);
					this._parameters.set([]);
				}
			},
			error: (err) => {
				console.error('Erreur lors du chargement des paramètres filtre', err);
				this._parameters.set([]);
			},
		});
	}

	/** Récupère un paramètre spécifique */
	getById(filterId: number, paramId: number): Observable<WebFilterParameter | undefined> {
		return this.http
			.get<ParameterResult>(`${this.baseUrl}/${filterId}/parameters/${paramId}`)
			.pipe(map((result) => result.parameter));
	}

	/** Crée un nouveau paramètre */
	create(filterId: number, param: WebFilterParameter): void {
		this.http.post<ParameterResult>(`${this.baseUrl}/${filterId}/parameters`, param).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.parameter) {
					this._parameters.set([...this._parameters(), result.parameter]);
				} else {
					console.warn('Réponse inattendue lors de la création du paramètre filtre', result);
				}
			},
			error: (err) => console.error('Erreur lors de la création du paramètre', err),
		});
	}

	/** Met à jour un paramètre */
	update(filterId: number, paramId: number, param: WebFilterParameter): void {
		this.http
			.put<ParameterResult>(`${this.baseUrl}/${filterId}/parameters/${paramId}`, param)
			.subscribe({
				next: (result) => {
					if (result?.state === 1 && result.parameter) {
						this._parameters.set(
							this._parameters().map((p) => (p.parameterId === paramId ? result.parameter! : p)),
						);
					} else {
						console.warn('Réponse inattendue lors de la mise à jour du paramètre filtre', result);
					}
				},
				error: (err) => console.error('Erreur lors de la mise à jour du paramètre', err),
			});
	}

	/** Supprime un paramètre */
	delete(filterId: number, paramId: number): void {
		this.http
			.delete<{
				message?: string;
				state: number;
			}>(`${this.baseUrl}/${filterId}/parameters/${paramId}`)
			.subscribe({
				next: (result) => {
					if (result?.state === 1) {
						this._parameters.set(this._parameters().filter((p) => p.parameterId !== paramId));
					} else {
						console.warn('Réponse inattendue lors de la suppression du paramètre filtre', result);
					}
				},
				error: (err) => console.error('Erreur lors de la suppression du paramètre', err),
			});
	}
}
