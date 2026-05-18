import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {TranslateService} from "@ngx-translate/core";

// --- Modèles TypeScript adaptés au backend ---

export interface WebFilterParameter {
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	defaultValue?: string;
	parameterOrder?: number | null;
	uniqueName?: string;
	parameterId?: number;
	exportId?: number | null;
	plausiId?: number | null;
	filterId?: number;
}

export interface WebFilter {
	filterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	parameters?: WebFilterParameter[];
	source?: string;
	isActive?: boolean;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
	authorisationLevel?: number;
	isDefault?: boolean;
	refObject?: number;
	filterOrder?: number | null;
}

export interface WebFilterListResult {
	message: string;
	state: number;
	filters: WebFilter[];
}

// --- Service principal ---

@Injectable({
	providedIn: 'root'
})
export class AdminFilterService {

	private readonly baseUrl = '/sbgweb/api/admin/filters';

	// Signal pour stocker la liste des filtres
	private _filters = signal<WebFilter[]>([]);
	filters = this._filters.asReadonly();

	private _loaded = signal(false);
	public readonly loaded = this._loaded.asReadonly();

	constructor(private http: HttpClient, private translate: TranslateService) {}

	/** 🔹 Charge tous les filtres depuis le backend */
	loadAll(): void {
		this._loaded.set(false);

		this.http.get<WebFilterListResult>(this.baseUrl)
			.subscribe({
				next: result => {
					if (result.state === 1 && Array.isArray(result.filters)) {
						this._filters.set(result.filters);
					} else {
						console.warn('Réponse inattendue du backend', result);
						this._filters.set([]);
					}
					this._loaded.set(true);
				},
				error: err => {
					console.error('Erreur lors du chargement des filtres', err);
					this._filters.set([]);
					this._loaded.set(true);
				}
			});
	}

	/** ✅ NEW: retourne l'année ActVersion via nameFr -> nameDe -> nameIt */
	getActVersionYear(): number | null {
		const filters = this._filters();
		const target = 'actversion';

		const eq = (v?: string) =>
			(v ?? '').toString().trim().toLowerCase() === target;

		const act =
			filters.find(f => eq(f.nameFr)) ||
			filters.find(f => eq(f.nameDe)) ||
			filters.find(f => eq(f.nameIt));

		const year = act?.source ? parseInt(act.source, 10) : NaN;
		return Number.isFinite(year) ? year : null;
	}

	/** 🔹 Récupère un filtre spécifique par ID */
	getById(id: number): Observable<WebFilter> {
		return this.http
			.get<{ message: string; state: number; filter: WebFilter }>(`${this.baseUrl}/${id}`)
			.pipe(map(result => result.filter));
	}

	/** 🔹 Crée un nouveau filtre */
	create(filter: WebFilter): void {
		this.http.post<{ message: string; state: number; filter: WebFilter }>(this.baseUrl, filter)
			.subscribe({
				next: result => {
					console.log('hraaaaaaaa',result)
					if (result.state === 1 && result.filter) {
						this._filters.set([...this._filters(), result.filter]);
					} else if (result.state === 2 && result.message) {
						const translated = this.translate.instant(result.message);
						alert(translated);
					}
				},
				error: err => console.error('Erreur lors de la création du filtre', err)
			});
	}

	/** 🔹 Met à jour un filtre existant */
	update(id: number, filter: WebFilter): void {
		this.http.put<{ message: string; state: number; filter: WebFilter }>(`${this.baseUrl}/${id}`, filter)
			.subscribe({
				next: result => {
					if (result.state === 1 && result.filter) {
						const updated = this._filters().map(f =>
							f.filterId === id ? result.filter : f
						);
						this._filters.set(updated);
					}
				},
				error: err => console.error('Erreur lors de la mise à jour du filtre', err)
			});
	}

	/** 🔹 Supprime un filtre */
	delete(id: number): void {
		this.http.delete<{ message: string; state: number }>(`${this.baseUrl}/${id}`)
			.subscribe({
				next: result => {
					if (result.state === 1) {
						this._filters.set(this._filters().filter(f => f.filterId !== id));
					}
				},
				error: err => console.error('Erreur lors de la suppression du filtre', err)
			});
	}
}
