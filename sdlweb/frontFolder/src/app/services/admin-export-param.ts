import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {WebFilterParameter} from "./admin-filter";


@Injectable({
	providedIn: 'root'
})
export class AdminExportParamService {

	private readonly baseUrl = '/sdlweb/api/admin/exports';

	private _parameters = signal<WebFilterParameter[]>([]);
	parameters = this._parameters.asReadonly();

	constructor(private http: HttpClient) {}

	loadAll(exportId: number): void {
		this.http.get<WebFilterParameter[]>(`${this.baseUrl}/${exportId}/parameters`)
			.subscribe({
				next: params => this._parameters.set(params),
				error: err => {
					console.error('Erreur chargement paramètres export', err);
					this._parameters.set([]);
				}
			});
	}

	getById(exportId: number, paramId: number) {
		return this.http.get<WebFilterParameter>(
			`${this.baseUrl}/${exportId}/parameters/${paramId}`
		);
	}

	create(exportId: number, param: WebFilterParameter): void {
		this.http
			.post<WebFilterParameter>(`${this.baseUrl}/${exportId}/parameters`, param)
			.subscribe({
				next: created => this._parameters.set([...this._parameters(), created]),
				error: err => console.error('Erreur création param export', err)
			});
	}

	update(exportId: number, paramId: number, param: WebFilterParameter): void {
		this.http
			.put<WebFilterParameter>(`${this.baseUrl}/${exportId}/parameters/${paramId}`, param)
			.subscribe({
				next: updated => {
					this._parameters.set(
						this._parameters().map(p => p.parameterId === paramId ? updated : p)
					);
				},
				error: err => console.error('Erreur update param export', err)
			});
	}

	delete(exportId: number, paramId: number): void {
		this.http
			.delete<boolean>(`${this.baseUrl}/${exportId}/parameters/${paramId}`)
			.subscribe({
				next: ok => {
					if (ok) {
						this._parameters.set(
							this._parameters().filter(p => p.parameterId !== paramId)
						);
					}
				},
				error: err => console.error('Erreur suppression param export', err)
			});
	}
}
