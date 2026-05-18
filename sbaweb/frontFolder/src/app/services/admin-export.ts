import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

// --- Modèles TypeScript adaptés au backend ---

export interface Export {
	exportId?: number;
	exportOrder?: number | null;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	type?: string;
	authorisationLevel?: number;
	isActive?: boolean;
	source?: string;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
}

export interface ExportListResult {
	message: string;
	state: number;
	exports: Export[];
}

export interface ExportResult {
	message: string;
	state: number;
	export: Export;
}

// --- Service principal ---

@Injectable({
	providedIn: 'root'
})
export class AdminExportService {
	private readonly baseUrl = '/sbaweb/api/admin/exports';

	// Signal pour stocker la liste des exports
	private _exports = signal<Export[]>([]);
	exports = this._exports.asReadonly();

	constructor(private http: HttpClient) {}

	/** 🔹 Charge tous les exports depuis le backend */
	loadAll(): void {
		this.http.get<ExportListResult>(this.baseUrl).subscribe({
			next: result => {
				if (result.state === 1 && Array.isArray(result.exports)) {
					this._exports.set(result.exports);
				} else {
					console.warn('Réponse inattendue du backend', result);
					this._exports.set([]);
				}
			},
			error: err => {
				console.error('Erreur lors du chargement des exports', err);
				this._exports.set([]);
			}
		});
	}

	/** 🔹 Récupère un export spécifique par ID */
	getById(id: number): Observable<Export> {
		return this.http
			.get<{ message: string; state: number; export: Export }>(`${this.baseUrl}/${id}`)
			.pipe(map(result => result.export));
	}

	/** 🔹 Crée un nouveau export */
	create(exp: Export): void {
		this.http
			.post<{ message: string; state: number; export: Export }>(this.baseUrl, exp)
			.subscribe({
				next: result => {
					if (result.state === 1 && result.export) {
						this._exports.set([...this._exports(), result.export]);
					}
				},
				error: err => console.error('Erreur lors de la création de l’export', err)
			});
	}

	/** 🔹 Met à jour un export existant */
	update(id: number, exp: Export): void {
		this.http
			.put<{ message: string; state: number; export: Export }>(`${this.baseUrl}/${id}`, exp)
			.subscribe({
				next: result => {
					if (result.state === 1 && result.export) {
						const updated = this._exports().map(e =>
							e.exportId === id ? result.export : e
						);
						this._exports.set(updated);
					}
				},
				error: err => console.error('Erreur lors de la mise à jour de l’export', err)
			});
	}

	/** 🔹 Supprime un export */
	delete(id: number): void {
		this.http
			.delete<{ message: string; state: number }>(`${this.baseUrl}/${id}`)
			.subscribe({
				next: result => {
					if (result.state === 1) {
						this._exports.set(this._exports().filter(e => e.exportId !== id));
					}
				},
				error: err => console.error('Erreur lors de la suppression de l’export', err)
			});
	}
}
