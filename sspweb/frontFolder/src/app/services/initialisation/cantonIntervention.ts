import {computed, DestroyRef, effect, inject, Injectable, signal} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {CantonIntervention} from "../../model/CantonIntervention";
import {convertDateToDateTimeString} from "../../utils/date.utils";
import {catchError, concat, finalize, of, toArray} from "rxjs";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {takeUntilDestroyed} from "@angular/core/rxjs-interop";
import {UserService} from "../user";

interface CantonInterventionParams {
	version: number;
	cantonId: number;
}

@Injectable({
	providedIn: 'root'
})
export class CantonInterventionService {
	private readonly baseUrl: string = '/sspweb/api/initialisations/';
	private readonly cantonInterventionsUrl: string = this.baseUrl + 'canton-interventions';

	private readonly http = inject(HttpClient);
	private readonly destroyRef = inject(DestroyRef);
	private readonly userService = inject(UserService);

	// Signaux pour les paramètres et les cantonInterventions
	private readonly cantonInterventionParams = signal<CantonInterventionParams | null>(null);
	private readonly cantonInterventionsMap = signal<Map<number, CantonIntervention>>(new Map());

	readonly currentCantonIntervention = signal<CantonIntervention | null>(null);

	// Signaux pour les opérations
	lastUpdatedCantonIntervention = signal<CantonIntervention | null>(null);
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Computed public signal
	readonly cantonInterventions = computed(() => {
		const map = this.cantonInterventionsMap();
		return Array.from(map.values()).map(cantonIntervention => this.formatRow(cantonIntervention));
	});

	constructor() {
		// Chargement des historiques de canton quand les params changent
		effect(() => {
			const params = this.cantonInterventionParams();
			if (!this.areParamsEmpty(params)) {
				this.doLoadCantonInterventions(params!);
			}
		});
	}

	emptyCantonInterventionsMap(){
		const emptyMap = new Map<number, CantonIntervention>();
		this.cantonInterventionsMap.set(emptyMap)
	}

	loadCantonInterventions(version: number, cantonId: number) {
		this.cantonInterventionParams.set({version: version, cantonId: cantonId});
		this.cantonInterventionParams.update(params => ({
			...params,
			version: version,
			cantonId: cantonId
		}));

		let cantonInterventionParams = this.cantonInterventionParams();

		if (cantonInterventionParams) {
			this.doLoadCantonInterventions(cantonInterventionParams);
		}
	}

	selectCantonIntervention(cantonIntervention: CantonIntervention): void {
		this.currentCantonIntervention.set(cantonIntervention);
	}

	/* ************* *\
	/*    ACTIONS    *
	/* ************* */
	exportCSV() {
		const cantonInterventionParams = this.cantonInterventionParams();

		this.http.get<Blob>(`${this.cantonInterventionsUrl}/export_csv/${cantonInterventionParams?.cantonId}`, {
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export completed'))
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'CantonInterventions.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV', error);
			}
		});
	}

	showPlausiReport() {
		const cantonInterventionId = this.currentCantonIntervention()?.cantonId;

		if (!cantonInterventionId) {
			return;
		}

		this.http.get(`${this.cantonInterventionsUrl}/show_plausi_report/${cantonInterventionId}`, {
			responseType: 'blob',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Plausi Report completed'))
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'plausiReport.xlsx');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export du rapport de plausibilisation', error);
			}
		});
	}

	create(cantonInterventions: CantonIntervention[]) {
		if (!cantonInterventions || cantonInterventions.length === 0) {
			console.log('Aucun historique de canton à sauvegarder');
			return;
		}

		this.isOpRunning.set(true);
		this.opError.set(null);

		const cantonInterventionParams = this.cantonInterventionParams();
		const user =  this.userService.user();

		cantonInterventions.forEach(cantonIntervention => {
			cantonIntervention.version = cantonInterventionParams?.version ?? -1;
			cantonIntervention.cantonId = cantonInterventionParams?.cantonId ?? -1;
			cantonIntervention.interventionUser = user.username;
		});

		// Créer un tableau d'observables (un POST par cantonInterventions)
		const requests$ = cantonInterventions.map(cantonIntervention =>
			this.http.put<CantonIntervention>(`${this.cantonInterventionsUrl}`, cantonIntervention)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de la sauvegarde d\'un historique de canton :';
						this.opError.set(error?.error || errorMessage);
						return of(null);
					}),
					finalize(() => this.isOpRunning.set(false))
				)
		);

		// Exécuter les appels séquentiellement
		concat(...requests$).pipe(
			toArray(),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (results) => {
				const successCount = results.filter(r => r !== null).length;
				console.log(`${successCount}/${cantonInterventions.length} historiques de canton insérés`);

				const params = this.cantonInterventionParams();


				if (params) {
					this.doLoadCantonInterventions(params);
				}
			},
			error: (error) => {
				console.error('Erreur lors de l\'insertion d\'historique(s) de canton :', error);
			}
		});
	}

	update(cantonInterventions: CantonIntervention[]): void {
		const body = cantonInterventions.map(value => ({
			...value
		}));

		// Créer un tableau d'observables
		const requests$ = body.map(value =>
			this.http.post<CantonIntervention>(`${this.cantonInterventionsUrl}`, value)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de la sauvegarde d\'un historique de canton';
						this.opError.set(error?.error || errorMessage);
						return of(null);
					})
				)
		);

		// Exécuter les appels séquentiellement
		concat(...requests$).pipe(
			toArray(),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (results) => {
				const successCount = results.filter(r => r !== null).length;
				console.log(`${successCount}/${cantonInterventions.length} historiques de canton mis à jour`);

				const params = this.cantonInterventionParams();

				if (params) {
					this.doLoadCantonInterventions(params);
				}
			},
			error: (error) => {
				console.error('Erreur lors de la sauvegarde des historiques de canton :', error);
			}
		});
	}

	delete(cantonInterventions: CantonIntervention[]) {
		if (!cantonInterventions || cantonInterventions.length === 0) {
			console.log('Aucun historique de canton à supprimer');
			return;
		}

		// Créer un tableau d'observables (un DELETE par canton)
		const requests$ = cantonInterventions.map(canton =>
			this.http.delete<CantonIntervention>(`${this.cantonInterventionsUrl}/${canton.interventionId}`)
				.pipe(
					catchError(error => {
						let errorMessage = `Erreur lors de la suppression de l'historique de canton : ${canton.interventionId}:`;
						console.error(errorMessage, error);
						this.opError.set(error?.error || errorMessage);
						return of(null);
					})
				)
		);

		// Ex�cuter les appels s�quentiellement
		concat(...requests$).pipe(
			toArray(), // Rassembler tous les r�sultats
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (results) => {
				const successCount = results.filter(r => r !== null).length;
				console.log(`${successCount}/${cantonInterventions.length} historiques de canton supprimés`);

				const params = this.cantonInterventionParams();
				if (params) {
					this.doLoadCantonInterventions(params);
				}
			},
			error: (error) => {
				console.error('Erreur globale :', error);
			}
		});
	}

	/* ******************
	 * MÉTHODES PRIVÉES *
	 * **************** */
	/**
	 * Charge les cantonInterventions
	 */
	private doLoadCantonInterventions(cantonInterventionParams?: CantonInterventionParams): void {
		if (!cantonInterventionParams) {
			return;
		}

		this.http.post<CantonIntervention[]>(`${this.cantonInterventionsUrl}/search`, cantonInterventionParams.cantonId)
			.pipe(
				catchError(err => {
					console.error('Problème lors du chargement des historiques de cantons:', err);
					return of([]);
				})
			)
			.subscribe(result => {
				// Créer la nouvelle Map
				const newMap = new Map<number, CantonIntervention>();
				result.forEach(canton => {
					newMap.set(canton.interventionId, canton);
				});

				this.cantonInterventionsMap.set(newMap);
			});
	}

	/**
	 * Formate une ligne "cantonIntervention" avec les champs calculés
	 */
	private formatRow(cantonIntervention: CantonIntervention): CantonIntervention{
		return {
			...cantonIntervention,
			interventionDateString: convertDateToDateTimeString(cantonIntervention.interventionDate)
		};
	}

	private areParamsEmpty(params: CantonInterventionParams | null): boolean {
		return params?.cantonId === undefined;
	}
}
