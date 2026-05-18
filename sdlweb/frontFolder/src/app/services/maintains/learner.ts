import {computed, DestroyRef, effect, inject, Injectable, Signal, signal, untracked} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {WebFilter} from "../../model/Delivery";
import {LanguageService} from "../language";
import {PredefinedFilter, WhereFilter} from "../../shared/pre-advance-filter/pre-advance-filter";
import {MaintainParams, Qualification, Learner} from "../../model/Maintain";
import {catchError, concat, debounceTime, filter, finalize, map, Observable, of, Subject, switchMap, toArray} from "rxjs";
import {takeUntilDestroyed, toSignal} from "@angular/core/rxjs-interop";
import {MaintainStateService} from "./maintain-state.service";
import {calculateLearnerCondition} from "./learner.utils";
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {distinctUntilChanged} from "rxjs/operators";
import {getTranslation} from "../../core/utils/translation.util";
import {PlausiError} from "../../shared/plausi-error-editor/plausi-error.model";

@Injectable({
	providedIn: 'root'
})
export class LearnerService {
	private readonly baseUrl = '/sdlweb/api/learners';

	private reloadService = inject(LanguageService);
	private http = inject(HttpClient);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private maintainState = inject(MaintainStateService);
	private destroyRef = inject(DestroyRef);
	private reloadTrigger$ = new Subject<void>(); // déclenche un reload

	private maintainParams = signal<MaintainParams | null>(null);
	readonly learners = signal<Learner[]>([]);
	private onPlausiErrorsLoading = signal<boolean>(false);

	// ** Signals pour les opérations
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Subject pour gérer le debounce
	private qualificationSelectionSubject = new Subject<number[]>();
	private selectedQualificationIds = signal<number[]>([]);

	private readonly qualificationIdsToLoad = computed(() => {
		const isLoading = this.maintainState.isLoadingQualifications();
		const qualifications = this.maintainState.selectedQualifications();
		const isMaster = this.maintainState.isLearnerMaster();

		if (!isMaster && !isLoading) {
			return qualifications.length > 0
				? qualifications
					.filter(p => p?.classId)
					.map(p => p.classId)
				: [];
		}
		return null;
	});

	constructor() {
		this.destroyRef.onDestroy(() => {
			this.reloadTrigger$.complete();
			this.qualificationSelectionSubject.complete();
		});

		// 1. Effect pour Master mode
		effect(() => {
			const params = this.maintainParams();
			const isMaster = this.maintainState.isLearnerMaster();

			if (!this.isMaintainParamsEmpty(params) && isMaster) {
				untracked(() => {
					this.learners.set([]);
					this.loadLearners(params ?? {});
				});
			}
		});

		// 2. Effect pour non-Master
		effect(() => {
			const qualificationIds = this.qualificationIdsToLoad();

			if (qualificationIds !== null && qualificationIds.length > 0) {
				untracked(() => {
					this.learners.set([]);
					console.log('qualificationIdsToLoad:', qualificationIds, qualificationIds.length);
					if (qualificationIds.length !== 0) {
						this.qualificationSelectionSubject.next(qualificationIds);
					}
				});
			}
		});

		this.qualificationSelectionSubject
			.pipe(
				debounceTime(500),
				distinctUntilChanged((a, b) =>
					a.length === b.length && a.every((v, i) => v === b[i])
				),
				takeUntilDestroyed()
			)
			.subscribe(qualificationIds => {
				this.selectedQualificationIds.set(qualificationIds);
				this.loadLearnersByQualifications(qualificationIds);
			});

		// 3. Gérer le reload trigger
		this.reloadTrigger$
			.pipe(
				switchMap(() => {
					const isMaster = this.maintainState.isLearnerMaster();
					const params = this.maintainParams();
					const qualificationIds = this.selectedQualificationIds();

					if (isMaster && params) {
						// Reload en mode master avec les filtres
						return this.reloadLearnersWithFilters(params);
					} else if (qualificationIds.length > 0) {
						// Reload en mode non-master avec les learners
						return this.reloadLearnersByQualifications(qualificationIds);
					}
					return of([]);
				}),
				takeUntilDestroyed()
			)
			.subscribe(learners => {
				this.formatLearners(learners);
				this.learners.set(learners);
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

	selectLearners(learners: Learner[] = []): void {
		this.maintainState.setSelectedLearners(learners);
		console.log(`${learners.length} lignes sélectionnées:`);
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
	 * Charge les learners via la recherche générale (isMaster = true)
	 */
	private loadLearners(params: MaintainParams): void {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};
		// Désactiver spinner et notification pour le téléchargement
		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingLearners(true);

		this.http.post<Learner[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement learners:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingLearners(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				this.formatLearners(result);
				this.learners.set(result);
			});
	}

	/**
	 * Charge les learners pour les qualificationnes sélectionnées (isMaster = false)
	 */
	private loadLearnersByQualifications(qualificationIds: number[]): void {
		if (qualificationIds.length === 0) {
			this.learners.set([]);
			return;
		}

		console.log(`Chargement des learners pour ${qualificationIds.length} qualificationne(s)...`);

		// Créer les query params
		let params = new HttpParams();
		qualificationIds.forEach(id => {
			params = params.append('qualificationIds', id.toString());
		});

		// Désactiver spinner et notification pour le téléchargement
		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingLearners(true);

		this.http.get<Learner[]>(this.baseUrl, {params})
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement learners par qualificationnes:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingLearners(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				console.log(`${result.length} learner(s) chargée(s)`);
				this.formatLearners(result);
				this.learners.set(result);
			});
	}

	/**
	 * Recharge les Learners avec filtres (master mode) - version Observable
	 */
	private reloadLearnersWithFilters(params: MaintainParams): Observable<Learner[]> {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingLearners(true);

		return this.http.post<Learner[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur rechargement learners:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingLearners(false))
			);
	}

	/**
	 * Recharge les Learners par qualifications (non-master mode) - version Observable
	 */
	private reloadLearnersByQualifications(qualificationIds: number[]): Observable<Learner[]> {
		if (qualificationIds.length === 0) {
			return of([]);
		}

		let params = new HttpParams();
		qualificationIds.forEach(id => {
			params = params.append('qualificationIds', id.toString());
		});

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingLearners(true);

		return this.http.get<Learner[]>(this.baseUrl, {params})
			.pipe(
				catchError(err => {
					console.error('❌ Erreur rechargement des Learners par qualifications:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingLearners(false))
			);
	}

	private formatLearners(learners: Learner[]): void {
		learners.forEach(learner => {
			learner.classCondition = calculateLearnerCondition(learner);
			learner.age = learner.version - new Date(learner.birthdate).getFullYear();
		});
	}

	export(): void {
		const maintainParams = this.maintainParams();
		const isMaster = this.maintainState.isLoadingLearners();

		// Si c'est le master, export avec filtres (POST)
		if (isMaster && maintainParams) {
			this.exportWithFilters(maintainParams);
		} else {
			// Sinon, export par sélection de qualificationnes (GET)
			this.exportByQualificationSelection();
		}
	}

	/** Export des learners avec filtres (master mode) */
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
				openCsvFile(response, 'Learner.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV avec filtres', error);
			}
		});
	}

	/** Export des learners par sélection de qualificationnes (non-master mode) */
	private exportByQualificationSelection(): void {
		// Récupérer les IDs des qualificationnes sélectionnées
		const selectedQualificationIds = this.getSelectedQualificationIds();

		if (selectedQualificationIds.length === 0) {
			console.warn('Aucune qualificationne sélectionnée pour l\'export');
			return;
		}

		// Construire les query params
		const params = new HttpParams()
			.set('qualificationIds', selectedQualificationIds.join(','));

		this.http.get<Blob>(`${this.baseUrl}/export/csv`, {
			params,
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export by qualification selection completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'Learner.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV par sélection', error);
			}
		});
	}

	/** Récupère les IDs des qualificationnes sélectionnées */
	private getSelectedQualificationIds(): number[] {
		return this.selectedQualificationIds();
	}

	// ✅ FIX FUITE MÉMOIRE #5: Ajout de takeUntilDestroyed()
	create(learners: Learner[]): void {
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();
		const selectedQualifications = this.maintainState.selectedQualifications();

		const body = learners.map(value => ({
			registerWithoutPlausi,
			learner: value,
			qualificationId: (selectedQualifications.length > 0) ? selectedQualifications[0].classId : undefined
		}));

		// Créer un tableau d'observables
		const requests$ = body.map(value =>
			this.http.put<Qualification>(`${this.baseUrl}`, value)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de l\'insertion d\'une learner:';
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
				console.log(`${successCount}/${learners.length} learners insérées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur lors de l\'insertion:', error);
			}
		});
	}

	update(learners: Learner[]): void {
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		const body = learners.map(value => ({
			registerWithoutPlausi,
			learner: value
		}));

		// Créer un tableau d'observables
		const requests$ = body.map(value =>
			this.http.post<Qualification>(`${this.baseUrl}`, value)
				.pipe(
					catchError(error => {
						let errorMessage = 'Erreur lors de la sauvegarde d\'une learner:';
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
				console.log(`${successCount}/${learners.length} learners insérées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur lors de la sauvegarde:', error);
			}
		});
	}

	delete(learners: Learner[]): void {
		if (!learners || learners.length === 0) {
			console.warn('Aucune learners à supprimer');
			return;
		}
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		// Créer un tableau d'observables (un DELETE par qualification)
		const requests$ = learners.map(learner =>
			this.http.delete<void>(`${this.baseUrl}/${learner.classId}/${registerWithoutPlausi}`)
				.pipe(
					catchError(error => {
						let errorMessage = `Erreur lors de la suppression de la learner ${learner.learnerId}:`;
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
				console.log(`${successCount}/${learners.length} learners supprimées`);
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur globale:', error);
			}
		});
	}

	public getPlausiErrors(learnerId: number): Observable<PlausiError[]> {
		if (!learnerId || this.onPlausiErrorsLoading()) {
			return of([]);
		}

		this.onPlausiErrorsLoading.set(true);
		return this.http.get<PlausiError[]>(`${this.baseUrl}/${learnerId}/plausi_errors`)
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

	prevalidate() {
		console.log('TODO'); // TODO
	}

	undoValidate() {
		console.log('TODO'); // TODO
	}
}
