import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {WebFilterParameter} from "./admin-filter";


@Injectable({
	providedIn: 'root'
})
export class AdminPlausiParamService {

	private readonly baseUrl = '/sbaweb/api/admin/plausis';

	private _parameters = signal<WebFilterParameter[]>([]);
	parameters = this._parameters.asReadonly();

	constructor(private http: HttpClient) {}

	loadAll(plausiId: number): void {
		this.http
			.get<WebFilterParameter[]>(`${this.baseUrl}/${plausiId}/parameters`)
			.subscribe({
				next: list => this._parameters.set(list),
				error: err => {
					console.error('Erreur chargement paramètres plausi', err);
					this._parameters.set([]);
				}
			});
	}

	getById(plausiId: number, paramId: number) {
		return this.http.get<WebFilterParameter>(
			`${this.baseUrl}/${plausiId}/parameters/${paramId}`
		);
	}

	create(plausiId: number, param: WebFilterParameter): void {
		this.http
			.post<WebFilterParameter>(`${this.baseUrl}/${plausiId}/parameters`, param)
			.subscribe({
				next: created => this._parameters.set([...this._parameters(), created]),
				error: err => console.error('Erreur création param plausi', err)
			});
	}

	update(plausiId: number, paramId: number, param: WebFilterParameter): void {
		this.http
			.put<WebFilterParameter>(`${this.baseUrl}/${plausiId}/parameters/${paramId}`, param)
			.subscribe({
				next: updated => {
					this._parameters.set(
						this._parameters().map(p => p.parameterId === paramId ? updated : p)
					);
				},
				error: err => console.error('Erreur update param plausi', err)
			});
	}

	delete(plausiId: number, paramId: number): void {
		this.http
			.delete<boolean>(`${this.baseUrl}/${plausiId}/parameters/${paramId}`)
			.subscribe({
				next: ok => {
					if (ok) {
						this._parameters.set(
							this._parameters().filter(p => p.parameterId !== paramId)
						);
					}
				},
				error: err => console.error('Erreur suppression param plausi', err)
			});
	}
}
