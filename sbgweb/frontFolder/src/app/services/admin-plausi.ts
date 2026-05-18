import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

// --- Modèles TypeScript adaptés au backend ---

export interface PlausiParameter {
	parameterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	defaultValue?: string;
	parameterOrder?: number;
	uniqueName?: string;
	macroId?: number;
}

export interface Plausi {
	plausiId?: number;
	macroid?: number;

	plausiOrder?: number | null;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	isActive?: boolean;
	isConfirmable?: boolean;
	source?: string;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
	validFrom?: string | null;
	validTo?: string | null;
	type?: number;
	objectLevel?: number;
	parameters?: PlausiParameter[];
}

export interface PlausiListResult {
	message?: string;
	state: number;
	macros: Plausi[];
}

export interface PlausiResult {
	message?: string;
	state: number;
	macro: Plausi;
}

// --- Service principal ---

@Injectable({
	providedIn: 'root',
})
export class AdminPlausiService {
	private readonly baseUrl = '/sbgweb/api/admin/macros';

	private _plausis = signal<Plausi[]>([]);
	plausis = this._plausis.asReadonly();

	constructor(private http: HttpClient) {}

	/**
	 * Convertit la réponse backend vers le modèle front.
	 * Le backend renvoie "macroid", alors que le front utilise "plausiId".
	 */
	private fromBackend(data: Plausi): Plausi {
		if (!data) {
			return data;
		}

		const id = data.plausiId ?? data.macroid;

		return {
			...data,
			plausiId: id,
			macroid: id,
		};
	}

	/**
	 * Convertit le modèle front vers le modèle backend.
	 */
	private toBackend(plausi: Plausi): Plausi {
		if (!plausi) {
			return plausi;
		}

		const id = plausi.macroid ?? plausi.plausiId;

		return {
			...plausi,
			macroid: id,
		};
	}

	/** Charge tous les plausis (type < 10) */
	loadAll(): void {
		this.http.get<PlausiListResult>(this.baseUrl).subscribe({
			next: (result) => {
				if (result?.state === 1 && Array.isArray(result.macros)) {
					this._plausis.set(
						result.macros
							.map((p) => this.fromBackend(p))
							.filter((p) => p.type != null && p.type < 10),
					);
				} else {
					console.warn('Réponse inattendue du backend', result);
					this._plausis.set([]);
				}
			},
			error: (err) => {
				console.error('Erreur lors du chargement des plausis', err);
				this._plausis.set([]);
			},
		});
	}

	/** Retourne la liste sous forme Observable */
	getAll(): Observable<Plausi[]> {
		return this.http
			.get<PlausiListResult>(this.baseUrl)
			.pipe(
				map((result) =>
					Array.isArray(result?.macros)
						? result.macros
								.map((p) => this.fromBackend(p))
								.filter((p) => p.type != null && p.type < 10)
						: [],
				),
			);
	}

	/** Récupère un plausi par ID */
	getById(id: number): Observable<Plausi> {
		return this.http
			.get<PlausiResult>(`${this.baseUrl}/${id}`)
			.pipe(map((result) => this.fromBackend(result.macro)));
	}

	/** Crée un nouveau plausi */
	create(plausi: Plausi): void {
		const payload = this.toBackend(plausi);

		this.http.post<PlausiResult>(this.baseUrl, payload).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.macro) {
					const created = this.fromBackend(result.macro);
					this._plausis.set([...this._plausis(), created]);
				} else {
					console.warn('Réponse inattendue lors de la création du plausi', result);
				}
			},
			error: (err) => console.error('Erreur lors de la création du plausi', err),
		});
	}

	/** Met à jour un plausi existant */
	update(id: number, plausi: Plausi): void {
		const payload = this.toBackend({ ...plausi, plausiId: id, macroid: id });

		this.http.put<PlausiResult>(`${this.baseUrl}/${id}`, payload).subscribe({
			next: (result) => {
				if (result?.state === 1 && result.macro) {
					const updatedPlausi = this.fromBackend(result.macro);

					const updatedList = this._plausis().map((p) => {
						const currentId = p.plausiId ?? p.macroid;
						return currentId === id ? updatedPlausi : p;
					});

					this._plausis.set(updatedList);
				} else {
					console.warn('Réponse inattendue lors de la mise à jour du plausi', result);
				}
			},
			error: (err) => console.error('Erreur lors de la mise à jour du plausi', err),
		});
	}

	/** Supprime un plausi */
	delete(id: number): void {
		this.http.delete<PlausiResult>(`${this.baseUrl}/${id}`).subscribe({
			next: (result) => {
				if (result?.state === 1) {
					this._plausis.set(this._plausis().filter((p) => (p.plausiId ?? p.macroid) !== id));
				} else {
					console.warn('Réponse inattendue lors de la suppression du plausi', result);
				}
			},
			error: (err) => console.error('Erreur lors de la suppression du plausi', err),
		});
	}
}
