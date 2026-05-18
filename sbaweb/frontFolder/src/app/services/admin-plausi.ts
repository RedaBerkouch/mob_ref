import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

// --- Modèles TypeScript adaptés au backend ---

// --- Modèle TypeScript ---
export interface PlausiParameter {
	parameterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	defaultValue?: string;
	parameterOrder?: number;
	uniqueName?: string;
	plausiId?: number;
}

export interface Plausi {
	plausiId?: number;
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

// --- Service principal ---

@Injectable({
	providedIn: 'root'
})
export class AdminPlausiService {
	private readonly baseUrl = '/sbaweb/api/admin/plausis';

	private _plausis = signal<Plausi[]>([]);
	plausis = this._plausis.asReadonly();

	constructor(private http: HttpClient) {}

	/** 🔹 Charge tous les plausis */
	loadAll(): void {
		this.http.get<Plausi[]>(this.baseUrl).subscribe({
			next: result => {
				console.log('✅ Réponse Plausi:', result);
				if (Array.isArray(result)) {
					this._plausis.set(result);
				} else {
					console.warn('⚠️ Réponse inattendue du backend Plausi', result);
					this._plausis.set([]);
				}
			},
			error: err => {
				console.error('❌ Erreur lors du chargement des plausis', err);
				this._plausis.set([]);
			}
		});
	}

	/** 🔹 Retourne la liste brute (si tu veux l’utiliser dans un pipe RxJS) */
	getAll(): Observable<Plausi[]> {
		return this.http.get<Plausi[]>(this.baseUrl);
	}
	/** 🔹 Crée un nouveau plausi */
	/** 🔹 Crée un nouveau plausi */
	create(plausi: Plausi): void {
		this.http.post<any>(this.baseUrl, plausi).subscribe({
			next: result => {
				console.log('✅ Réponse création Plausi:', result);

				const newPlausi = result?.plausi ?? result; // tolère les 2 formats
				if (newPlausi) {
					this._plausis.set([...this._plausis(), newPlausi]);
				} else {
					console.warn('⚠️ Aucun plausi dans la réponse de création', result);
				}
			},
			error: err => console.error('❌ Erreur lors de la création du plausi', err)
		});
	}

	/** 🔹 Met à jour un plausi existant */
	update(id: number, plausi: Plausi): void {
		this.http.put<any>(`${this.baseUrl}/${id}`, plausi).subscribe({
			next: result => {
				console.log('✅ Réponse update Plausi:', result);

				const updatedPlausi = result?.plausi ?? result;
				if (updatedPlausi) {
					const updatedList = this._plausis().map(p =>
						p.plausiId === id ? updatedPlausi : p
					);
					this._plausis.set(updatedList);
				} else {
					console.warn('⚠️ Aucun plausi dans la réponse de mise à jour', result);
				}
			},
			error: err => console.error('❌ Erreur lors de la mise à jour du plausi', err)
		});
	}

	/** 🔹 Supprime un plausi */
	delete(id: number): void {
		this.http.delete<any>(`${this.baseUrl}/${id}`).subscribe({
			next: result => {
				console.log('✅ Réponse suppression Plausi:', result);

				// si state == 1 ou suppression réussie sans champ 'state'
				if (result?.state === 1 || result === true || result === null) {
					this._plausis.set(this._plausis().filter(p => p.plausiId !== id));
				} else {
					console.warn('⚠️ Réponse inattendue lors de la suppression', result);
				}
			},
			error: err => console.error('❌ Erreur lors de la suppression du plausi', err)
		});
	}

}
