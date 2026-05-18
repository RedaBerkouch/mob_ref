import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {WebFilterParameter} from "./admin-filter";


@Injectable({
	providedIn: 'root'
})
export class AdminFilterParamService {

	private readonly baseUrl = '/sdlweb/api/admin/filters';

	private _parameters = signal<WebFilterParameter[]>([]);
	parameters = this._parameters.asReadonly();

	constructor(private http: HttpClient) {}

	/** Charge tous les paramètres d’un filtre */
	loadAll(filterId: number): void {
		this.http
			.get<WebFilterParameter[]>(`${this.baseUrl}/${filterId}/parameters`)
			.subscribe({
				next: params => this._parameters.set(params),
				error: err => {
					console.error('Erreur lors du chargement des paramètres filtre', err);
					this._parameters.set([]);
				}
			});
	}

	/** Récupère un paramètre spécifique */
	getById(filterId: number, paramId: number): Observable<WebFilterParameter> {
		return this.http
			.get<WebFilterParameter>(`${this.baseUrl}/${filterId}/parameters/${paramId}`);
	}

	/** Crée un nouveau paramètre */
	create(filterId: number, param: WebFilterParameter): void {
		this.http
			.post<WebFilterParameter>(`${this.baseUrl}/${filterId}/parameters`, param)
			.subscribe({
				next: created => this._parameters.set([...this._parameters(), created]),
				error: err => console.error('Erreur lors de la création du paramètre', err)
			});
	}

	/** Met à jour un paramètre */
	update(filterId: number, paramId: number, param: WebFilterParameter): void {
		this.http
			.put<WebFilterParameter>(`${this.baseUrl}/${filterId}/parameters/${paramId}`, param)
			.subscribe({
				next: updated => {
					this._parameters.set(
						this._parameters().map(p => p.parameterId === paramId ? updated : p)
					);
				},
				error: err => console.error('Erreur lors de la mise à jour du paramètre', err)
			});
	}

	/** Supprime un paramètre */
	delete(filterId: number, paramId: number): void {
		this.http
			.delete<boolean>(`${this.baseUrl}/${filterId}/parameters/${paramId}`)
			.subscribe({
				next: ok => {
					if (ok) {
						this._parameters.set(
							this._parameters().filter(p => p.parameterId !== paramId)
						);
					}
				},
				error: err => console.error('Erreur lors de la suppression du paramètre', err)
			});
	}
}
