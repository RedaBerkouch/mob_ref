import {computed, DestroyRef, effect, inject, Injectable, Signal, signal, untracked} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {WebFilter} from "../../model/Delivery";
import {LanguageService} from "../language";
import {PredefinedFilter, WhereFilter} from "../../shared/pre-advance-filter/pre-advance-filter";
import {MaintainParams, Person, Qualification} from "../../model/Maintain";
import {catchError, concat, debounceTime, filter, finalize, map, Observable, of, Subject, switchMap, toArray} from "rxjs";
import {takeUntilDestroyed, toSignal} from "@angular/core/rxjs-interop";
import {MaintainStateService} from "./maintain-state.service";
import {calculateQualificationCondition} from "./qualification.utils";
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {distinctUntilChanged} from "rxjs/operators";
import {getTranslation} from "../../core/utils/translation.util";
import {PlausiError} from "../../shared/plausi-error-editor/plausi-error.model";

@Injectable({
	providedIn: 'root'
})
export class QualificationService {
	private readonly baseUrl = '/sbaweb/api/qualifications';

	private reloadService = inject(LanguageService);
	private http = inject(HttpClient);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private maintainState = inject(MaintainStateService);
	private destroyRef = inject(DestroyRef);
	private reloadTrigger$ = new Subject<void>(); // déclenche un reload

	private maintainParams = signal<MaintainParams | null>(null);
	readonly qualifications = signal<Qualification[]>([]);
	private onPlausiErrorsLoading = signal<boolean>(false);

	// ** Signals pour les opérations
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Subject pour gérer le debounce
	private personSelectionSubject = new Subject<number[]>();
	private selectedPersonIds = signal<number[]>([]);

	private readonly personIdsToLoad = computed(() => {
		const isLoading = this.maintainState.isLoadingPersons();
		const persons = this.maintainState.selectedPersons();
		const isMaster = this.maintainState.isQualificationMaster();

		if (!isMaster && !isLoading) {
			return persons.length > 0
				? persons
					.filter(p => p?.personId)
					.map(p => p.personId)
				: [];
		}
		return null;
	});

	constructor() {
		this.destroyRef.onDestroy(() => {
			this.reloadTrigger$.complete();
			this.personSelectionSubject.complete();
		});

		// 1. Effect pour Master mode
		effect(() => {
			const params = this.maintainParams();
			const isMaster = this.maintainState.isQualificationMaster();

			if (!this.isMaintainParamsEmpty(params) && isMaster) {
				untracked(() => {
					this.qualifications.set([]);
					this.loadQualifications(params ?? {});
				});
			}
		});

		// 2. Effect pour non-Master
		effect(() => {
			const personIds = this.personIdsToLoad();

			if (personIds !== null && personIds.length > 0) {
				untracked(() => {
					this.qualifications.set([]);
					console.log('personIdsToLoad:', personIds, personIds.length);
					if (personIds.length !== 0) {
						this.personSelectionSubject.next(personIds);
					}
				});
			}
		});

		this.personSelectionSubject
			.pipe(
				debounceTime(500),
				distinctUntilChanged((a, b) =>
					a.length === b.length && a.every((v, i) => v === b[i])
				),
				takeUntilDestroyed()
			)
			.subscribe(personIds => {
				this.selectedPersonIds.set(personIds);
				this.loadQualificationsByPersons(personIds);
			});

		// 3. Gérer le reload trigger
		this.reloadTrigger$
			.pipe(
				switchMap(() => {
					const isMaster = this.maintainState.isQualificationMaster();
					const params = this.maintainParams();
					const personIds = this.selectedPersonIds();

					if (isMaster && params) {
						// Reload en mode master avec les filtres
						return this.reloadQualificationsWithFilters(params);
					} else if (personIds.length > 0) {
						// Reload en mode non-master avec les qualifications
						return this.reloadQualificationsByPersons(personIds);
					}
					return of([]);
				}),
				takeUntilDestroyed()
			)
			.subscribe(qualifications => {
				this.formatQualifications(qualifications);
				this.qualifications.set(qualifications);
			});
	}

	setParams(year?: number, canton?: string, webFilters?: WebFilter[], whereFilters?: WhereFilter[]): void {
		this.maintainParams.update(params => ({
			...params,
			version: year,
			canton,
			webFilters: webFilters ?? undefined,
			whereFilters: whereFilters ?? undefined,
		}));
	}

	readonly webFilters: Signal<WebFilter[]> = toSignal(
		this.http.get<WebFilter[]>(`${this.baseUrl}/predefined_filters`).pipe(
			filter(value => !!value)
		),
		{initialValue: []}
	);

	readonly predefinedFilters: Signal<PredefinedFilter[]> = computed(() => {
		const lang = this.reloadService.currentLanguage();
		const data = this.webFilters();

		return data.map(webFilter => ({
			id: webFilter.filterId || 0,
			label: getTranslation(webFilter, 'name', lang),
			description: getTranslation(webFilter, 'description', lang),
			parameters: webFilter.parameters
				?.map(param => {
					const name = getTranslation(param, 'name', lang);
					return `${param.uniqueName}:${name}=${param.defaultValue}`;
				})
				.join(';'),
			sql: webFilter.source || '',
			default: webFilter.isDefault || false
		}));
	});

	selectQualifications(qualifications: Qualification[] = []): void {
		this.maintainState.setSelectedQualifications(qualifications);
		console.log(`${qualifications.length} lignes sélectionnées:`);
	}

	private isMaintainParamsEmpty(params: MaintainParams | null): boolean {
		return !params || (
			params.version === undefined &&
			params.canton === undefined &&
			(!params.webFilters || params.webFilters.length === 0) &&
			(!params.whereFilters || params.whereFilters.length === 0)
		);
	}

	/**
	 * Charge les qualifications via la recherche générale (isMaster = true)
	 */
	private loadQualifications(params: MaintainParams): void {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};
		// Désactiver spinner et notification pour le téléchargement
		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingQualifications(true);

		this.http.post<Qualification[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement qualifications:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingQualifications(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				this.formatQualifications(result);
				this.qualifications.set(result);
			});
	}

	/**
	 * Charge les qualifications pour les personnes sélectionnées (isMaster = false)
	 */
	private loadQualificationsByPersons(personIds: number[]): void {
		if (personIds.length === 0) {
			this.qualifications.set([]);
			return;
		}

		console.log(`Chargement des qualifications pour ${personIds.length} personne(s)...`);

		// Créer les query params
		let params = new HttpParams();
		personIds.forEach(id => {
			params = params.append('personIds', id.toString());
		});

		// Désactiver spinner et notification pour le téléchargement
		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingQualifications(true);

		this.http.get<Qualification[]>(this.baseUrl, {params})
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement qualifications par personnes:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingQualifications(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				console.log(`${result.length} qualification(s) chargée(s)`);
				this.formatQualifications(result);
				this.qualifications.set(result);
			});
	}

	/**
	 * Recharge les Qualifications avec filtres (master mode) - version Observable
	 */
	private reloadQualificationsWithFilters(params: MaintainParams): Observable<Qualification[]> {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingQualifications(true);

		return this.http.post<Qualification[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur rechargement qualifications:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingQualifications(false))
			);
	}

	/**
	 * Recharge les Qualifications par persons (non-master mode) - version Observable
	 */
	private reloadQualificationsByPersons(personIds: number[]): Observable<Qualification[]> {
		if (personIds.length === 0) {
			return of([]);
		}

		let params = new HttpParams();
		personIds.forEach(id => {
			params = params.append('personIds', id.toString());
		});

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingQualifications(true);

		return this.http.get<Qualification[]>(this.baseUrl, {params})
			.pipe(
				catchError(err => {
					console.error('❌ Erreur rechargement des Qualifications par persons:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingQualifications(false))
			);
	}

	private formatQualifications(qualifications: Qualification[]): void {
		qualifications.forEach(qualification => {
			qualification.classCondition = calculateQualificationCondition(qualification);
		});
	}

	export(): void {
		const maintainParams = this.maintainParams();
		const isMaster = this.maintainState.isLoadingQualifications();

		// Si c'est le master, export avec filtres (POST)
		if (isMaster && maintainParams) {
			this.exportWithFilters(maintainParams);
		} else {
			// Sinon, export par sélection de personnes (GET)
			this.exportByPersonSelection();
		}
	}

	/** Export des qualifications avec filtres (master mode) */
	private exportWithFilters(maintainParams: MaintainParams): void {
		const body = {
			version: maintainParams.version ?? null,
			canton: maintainParams.canton ?? null,
			webFilters: maintainParams.webFilters ?? [],
			whereFilters: maintainParams.whereFilters ?? []
		};

		this.http.post<Blob>(`${this.baseUrl}/export/csv`, body, {
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export with filters completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'Qualification.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV avec filtres', error);
			}
		});
	}

	/** Export des qualifications par sélection de personnes (non-master mode) */
	private exportByPersonSelection(): void {
		// Récupérer les IDs des personnes sélectionnées
		const selectedPersonIds = this.getSelectedPersonIds();

		if (selectedPersonIds.length === 0) {
			console.warn('Aucune personne sélectionnée pour l\'export');
			return;
		}

		// Construire les query params
		const params = new HttpParams()
			.set('personIds', selectedPersonIds.join(','));

		this.http.get<Blob>(`${this.baseUrl}/export/csv`, {
			params,
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export by person selection completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'Qualification.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV par sélection', error);
			}
		});
	}

	/** Récupère les IDs des personnes sélectionnées */
	private getSelectedPersonIds(): number[] {
		return this.selectedPersonIds();
	}

	// ✅ FIX FUITE MÉMOIRE #5: Ajout de takeUntilDestroyed()
	create(qualifications: Qualification[]): void {
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();
		const selectedPersons = this.maintainState.selectedPersons();

		const body = qualifications.map(value => ({
			registerWithoutPlausi,
			qualification: value,
			personId: (selectedPersons.length > 0) ? selectedPersons[0].personId : undefined
		}));

		// Créer un tableau d'observables
		const requests$ = body.map(value =>
			this.http.put<Person>(`${this.baseUrl}`, value)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de l\'insertion d\'une qualification:';
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
				console.log(`${successCount}/${qualifications.length} qualifications insérées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur lors de l\'insertion:', error);
			}
		});
	}

	update(qualifications: Qualification[]): void {
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		const body = qualifications.map(value => ({
			registerWithoutPlausi,
			qualification: value
		}));

		// Créer un tableau d'observables
		const requests$ = body.map(value =>
			this.http.post<Person>(`${this.baseUrl}`, value)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de la sauvegarde d\'une qualification:';
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
				console.log(`${successCount}/${qualifications.length} qualifications insérées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur lors de la sauvegarde:', error);
			}
		});
	}

	delete(qualifications: Qualification[]): void {
		if (!qualifications || qualifications.length === 0) {
			console.warn('Aucune qualifications à supprimer');
			return;
		}
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		// Créer un tableau d'observables (un DELETE par person)
		const requests$ = qualifications.map(qualification =>
			this.http.delete<void>(`${this.baseUrl}/${qualification.qualificationId}/${registerWithoutPlausi}`)
				.pipe(
					catchError(error => {
						let errorMessage = `Erreur lors de la suppression de la qualification ${qualification.personId}:`;
						console.error(errorMessage, error);
						this.opError.set(error?.error || errorMessage);
						return of(null);
					})
				)
		);

		// Exécuter les appels séquentiellement (un après l'autre)
		concat(...requests$).pipe(
			toArray(), // Rassembler tous les résultats
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (results) => {
				const successCount = results.filter(r => r !== null).length;
				console.log(`${successCount}/${qualifications.length} qualifications supprimées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur globale:', error);
			}
		});
	}

	public getPlausiErrors(qualificationId: number): Observable<PlausiError[]> {
		if (!qualificationId || this.onPlausiErrorsLoading()) {
			return of([]);
		}

		this.onPlausiErrorsLoading.set(true);
		return this.http.get<PlausiError[]>(`${this.baseUrl}/${qualificationId}/plausi_errors`)
			.pipe(
				map( (plausiErrors) => {
						plausiErrors.forEach((plausiError) => plausiError.modificationDateString = new Date(plausiError.modificationDate).toLocaleDateString('fr-CH'));
						return plausiErrors;
					}
				),
				catchError(err => {
					console.error('❌ Erreur chargement des plausi errors:', err);
					return of([]);
				}),
				finalize(() => this.onPlausiErrorsLoading.set(false))
			);
	}
}
