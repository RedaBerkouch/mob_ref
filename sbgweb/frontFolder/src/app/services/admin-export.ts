import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

// --- Modèles TypeScript adaptés au backend ---

/**
 * Le backend Spring manipule des "Macro" avec un identifiant "macroid".
 * On garde aussi "exportId" pour rester compatible avec ton front actuel.
 */
export interface Export {
	exportId?: number;
	macroid?: number;

	exportOrder?: number | null;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	type?: number;
	authorisationLevel?: number;
	isActive?: boolean;
	source?: string;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
}

export interface ExportListResult {
	message?: string;
	state: number;
	macros: Export[];
}

export interface ExportResult {
	message?: string;
	state: number;
	macro: Export;
}

// --- Service principal ---

@Injectable({
	providedIn: 'root',
})
export class AdminExportService {
	private readonly baseUrl = '/sbgweb/api/admin/macros';

	private _exports = signal<Export[]>([]);
	exports = this._exports.asReadonly();

	constructor(private http: HttpClient) {}

	/**
	 * Normalise un objet venant du backend :
	 * si le backend renvoie macroid, on le recopie dans exportId.
	 */
	private fromBackend(data: Export): Export {
		if (!data) {
			return data;
		}

		const id = data.exportId ?? data.macroid;

		return {
			...data,
			exportId: id,
			macroid: id,
		};
	}

	/**
	 * Prépare l'objet à envoyer au backend :
	 * le backend attend surtout "macroid".
	 */
	private toBackend(exp: Export): Export {
		if (!exp) {
			return exp;
		}

		const id = exp.macroid ?? exp.exportId;

		return {
			...exp,
			macroid: id,
		};
	}

	/** Charge tous les exports depuis le backend (type >= 10) */
	loadAll(): void {
		this.http.get<ExportListResult>(this.baseUrl).subscribe({
			next: (result) => {
				if (result?.state === 1 && Array.isArray(result.macros)) {
					this._exports.set(
						result.macros
							.map((e) => this.fromBackend(e))
							.filter((e) => e.type != null && e.type >= 10),
					);
				} else {
					console.warn('Réponse inattendue du backend', result);
					this._exports.set([]);
				}
			},
			error: (err) => {
				console.error('Erreur lors du chargement des exports', err);
				this._exports.set([]);
			},
		});
	}

	/** Récupère un export spécifique par ID */
	getById(id: number): Observable<Export> {
		return this.http
			.get<ExportResult>(`${this.baseUrl}/${id}`)
			.pipe(map((result) => this.fromBackend(result.macro)));
	}

	/** Crée un nouvel export */
	create(exp: Export): void {
		const payload = this.toBackend(exp);

		this.http.post<ExportResult>(this.baseUrl, payload).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.macro) {
					const created = this.fromBackend(result.macro);
					this._exports.set([...this._exports(), created]);
				} else {
					console.warn('Réponse inattendue lors de la création', result);
				}
			},
			error: (err) => console.error('Erreur lors de la création de l’export', err),
		});
	}

	/** Met à jour un export existant */
	update(id: number, exp: Export): void {
		const payload = this.toBackend({ ...exp, exportId: id, macroid: id });

		this.http.put<ExportResult>(`${this.baseUrl}/${id}`, payload).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.macro) {
					const updatedExport = this.fromBackend(result.macro);

					const updatedList = this._exports().map((e) => {
						const currentId = e.exportId ?? e.macroid;
						return currentId === id ? updatedExport : e;
					});

					this._exports.set(updatedList);
				} else {
					console.warn('Réponse inattendue lors de la mise à jour', result);
				}
			},
			error: (err) => console.error('Erreur lors de la mise à jour de l’export', err),
		});
	}

	/** Supprime un export */
	delete(id: number): void {
		this.http.delete<ExportResult>(`${this.baseUrl}/${id}`).subscribe({
			next: (result) => {
				if (result?.state === 1) {
					this._exports.set(this._exports().filter((e) => (e.exportId ?? e.macroid) !== id));
				} else {
					console.warn('Réponse inattendue lors de la suppression', result);
				}
			},
			error: (err) => console.error('Erreur lors de la suppression de l’export', err),
		});
	}
}
