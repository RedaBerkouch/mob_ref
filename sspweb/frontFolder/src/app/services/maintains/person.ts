import {
	computed,
	DestroyRef,
	effect,
	inject,
	Injectable,
	Signal,
	signal,
	untracked
} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {LanguageService} from "../language";
import {PredefinedFilter, WhereFilter} from "../../shared/pre-advance-filter/pre-advance-filter";
import {MaintainParams, MEB_DATASTATUS, Person} from "../../model/Maintain";
import {takeUntilDestroyed, toSignal} from "@angular/core/rxjs-interop";
import {
	catchError,
	concat,
	debounceTime,
	finalize,
	filter,
	map,
	Observable,
	of,
	Subject,
	switchMap,
	toArray
} from "rxjs";
import {getTranslation} from "../../core/utils/translation.util";
import {MaintainStateService} from "./maintain-state.service";
import {calculatePersonCondition} from "./person.utils";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";
import {distinctUntilChanged} from "rxjs/operators";
import {LastFilters, MEB_PLAUSISTATUS, WebFilter} from "../../model/Delivery";
import {DeliveryService} from "../deliveries/delivery";
import {PlausiError} from "../../shared/plausi-error-editor/plausi-error.model";

@Injectable({
	providedIn: 'root'
})
export class PersonService {
	private readonly baseUrl = '/sspweb/api/persons';

	private reloadService = inject(LanguageService);
	private http = inject(HttpClient);
	private maintainState = inject(MaintainStateService);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private deliveryService = inject(DeliveryService);
	private destroyRef = inject(DestroyRef);

	/** déclenche un reload "manuel" */
	private readonly reloadTrigger$ = new Subject<void>();

	private maintainParams = signal<MaintainParams | null>(null);
	readonly persons = signal<Person[]>([]);

	// Subject pour gérer le debounce
	private readonly qualificationSelectionSubject = new Subject<number[]>();
	private selectedQualificationIds = signal<number[]>([]);
	private onPlausiErrorsLoading = signal<boolean>(false);

	// Signals pour les opérations
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Computed pour dériver les qualificationIds à charger en mode non-Master
	private readonly qualificationIdsToLoad = computed(() => {
		const isLoading = this.maintainState.isLoadingQualifications();
		const qualifications = this.maintainState.selectedQualifications();
		const isMaster = this.maintainState.isPersonMaster();

		if (!isMaster && !isLoading) {
			return qualifications.length > 0
				? qualifications
					.filter(p => p?.activityId)
					.map(p => p.activityId)
				: [];
		}
		return null;
	});

	constructor() {
		// ✅ cleanup subjects (même si root => utile si scope change un jour)
		this.destroyRef.onDestroy(() => {
			this.reloadTrigger$.complete();
			this.qualificationSelectionSubject.complete();
		});

		/**
		 * 1) MASTER MODE : quand maintainParams change et isMaster=true => loadPersons()
		 *    ✅ Pas de subscribe fantôme : loadPersons gère déjà takeUntilDestroyed.
		 */
		effect(() => {
			const params = this.maintainParams();
			const isMaster = this.maintainState.isPersonMaster();

			if (!this.isMaintainParamsEmpty(params) && isMaster) {
				untracked(() => {
					this.persons.set([]);
					this.loadPersons(params ?? {});
				});
			}
		});

		/**
		 * 2) NON-MASTER : quand la sélection qualifications change (et pas loading)
		 */
		effect(() => {
			const qualificationIds = this.qualificationIdsToLoad();

			if (qualificationIds !== null) {
				untracked(() => {
					this.persons.set([]);
					if (qualificationIds.length !== 0) {
						this.qualificationSelectionSubject.next(qualificationIds);
					}
				});
			}
		});

		/**
		 * 2bis) Debounce qualifications => loadPersonsByQualifications
		 */
		this.qualificationSelectionSubject
			.pipe(
				debounceTime(500),
				// ✅ compare robuste (ordre + valeur)
				map(ids => [...ids].sort((a, b) => a - b)),
				distinctUntilChanged((a, b) =>
					a.length === b.length && a.every((v, i) => v === b[i])
				),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(qualificationIds => {
				this.selectedQualificationIds.set(qualificationIds);
				this.loadPersonsByQualifications(qualificationIds);
			});

		/**
		 * 3) Reload trigger centralisé (switchMap => annule la requête précédente)
		 */
		this.reloadTrigger$
			.pipe(
				switchMap(() => {
					const isMaster = this.maintainState.isPersonMaster();
					const params = this.maintainParams();
					const qualificationIds = this.selectedQualificationIds();

					if (isMaster && params) {
						return this.reloadPersonsWithFilters(params);
					} else if (qualificationIds.length > 0) {
						return this.reloadPersonsByQualifications(qualificationIds);
					}
					return of([]);
				}),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(persons => {
				this.formatPersons(persons);
				this.persons.set(persons);
			});
	}

	// -------------------- PUBLIC API (signatures inchangées) --------------------

	setParams(year?: number, canton?: string, webFilters?: WebFilter[], whereFilters?: WhereFilter[]): void {
		this.maintainParams.update(params => ({
			...params,
			version: year,
			canton,
			webFilters: webFilters ?? undefined,
			whereFilters: whereFilters ?? undefined,
		}));
	}

	selectPersons(persons: Person[] = []): void {
		this.maintainState.setSelectedPersons(persons);
	}

	readonly lastFilters: Signal<LastFilters | undefined> = toSignal(
		this.http.get<LastFilters>(`${this.baseUrl}/lastFilters`).pipe(
			filter(value => !!value)
		),
		{initialValue: undefined}
	);

	readonly sessionFilters = signal({
		version: this.deliveryService.deliveryParams()?.version,
		canton: this.deliveryService.deliveryParams()?.canton
	});

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

	export(): void {
		const maintainParams = this.maintainParams();
		const isMaster = this.maintainState.isPersonMaster();
		if (isMaster && maintainParams) {
			this.exportWithFilters(maintainParams);
		} else {
			this.exportByQualificationSelection();
		}
	}

	prevalidate() {
		this.executeAction('validate', 'Error when validating persons');
	}

	undoValidate() {
		this.executeAction('undo_validate', 'Error when unvalidating persons');
	}

	create(persons: Person[]): void { // TODO gérer quand c'est pas Master
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		const body = persons.map(value => ({
			registerWithoutPlausi,
			person: {
				...value,
				plausiStatus: MEB_PLAUSISTATUS.UNDEFINED,
				deliveryStatus: MEB_DATASTATUS.DELIVERED,
			}
		}));

		const requests$ = body.map(value =>
			this.http.put<Person>(`${this.baseUrl}`, value).pipe(
				catchError(error => {
					const errorMessage = 'Erreur lors de l\'insertion d\'une personne:';
					this.opError.set(error?.error || errorMessage);
					return of(null);
				})
			)
		);

		concat(...requests$)
			.pipe(
				toArray(),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe({
				next: (results) => {
					const successCount = results.filter(r => r !== null).length;
					console.log(`${successCount}/${persons.length} persons insérées`);
					this.reloadTrigger$.next();
				},
				error: (error) => console.error('Erreur lors de l\'insertion:', error)
			});
	}

	update(persons: Person[]): void { // TODO gérer quand c'est pas Master
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		const body = persons.map(value => ({
			registerWithoutPlausi,
			person: value
		}));

		const requests$ = body.map(value =>
			this.http.post<Person>(`${this.baseUrl}`, value).pipe(
				catchError(error => {
					const errorMessage = 'Erreur lors de la sauvegarde d\'une personne:';
					this.opError.set(error?.error || errorMessage);
					return of(null);
				})
			)
		);

		concat(...requests$)
			.pipe(
				toArray(),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe({
				next: (results) => {
					const successCount = results.filter(r => r !== null).length;
					console.log(`${successCount}/${persons.length} persons sauvegardées`);
					this.reloadTrigger$.next();
				},
				error: (error) => console.error('Erreur lors de la sauvegarde:', error)
			});
	}

	delete(persons: Person[]): void { // TODO gérer quand c'est pas Master
		if (!persons || persons.length === 0) {
			console.warn('Aucune personnes à supprimer');
			return;
		}
		const registerWithoutPlausi = this.maintainState.registerWithoutPlausi();

		const requests$ = persons.map(person =>
			this.http.delete<void>(`${this.baseUrl}/${person.personId}/${registerWithoutPlausi}`).pipe(
				catchError(error => {
					const errorMessage = `Erreur lors de la suppression de la personne ${person.personId}:`;
					console.error(errorMessage, error);
					this.opError.set(error?.error || errorMessage);
					return of(null);
				})
			)
		);

		concat(...requests$)
			.pipe(
				toArray(),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe({
				next: (results) => {
					const successCount = results.filter(r => r !== null).length;
					console.log(`${successCount}/${persons.length} personnes supprimées`);
					this.reloadTrigger$.next();
				},
				error: (error) => console.error('Erreur globale:', error)
			});
	}

	public getPlausiErrors(personId: number): Observable<PlausiError[]> {
		// ✅ anti "double fetch" + évite garder le flag bloqué
		if (!personId || this.onPlausiErrorsLoading()) {
			return of([]);
		}

		this.onPlausiErrorsLoading.set(true);

		return this.http.get<PlausiError[]>(`${this.baseUrl}/${personId}/plausi_errors`).pipe(
			map(plausiErrors => {
				plausiErrors.forEach(plausiError => {
					plausiError.modificationDateString = new Date(plausiError.modificationDate).toLocaleDateString('fr-CH');
				});
				return plausiErrors;
			}),
			catchError(err => {
				console.error('❌ Erreur chargement des plausi errors:', err);
				return of([]);
			}),
			finalize(() => this.onPlausiErrorsLoading.set(false))
		);
	}

	// -------------------- PRIVATE HELPERS --------------------

	private isMaintainParamsEmpty(params: MaintainParams | null): boolean {
		return !params || (
			params.version === undefined &&
			params.canton === undefined &&
			(!params.webFilters || params.webFilters.length === 0) &&
			(!params.whereFilters || params.whereFilters.length === 0)
		);
	}

	/**
	 * Charge les persons via la recherche générale (isMaster = true)
	 */
	private loadPersons(params: MaintainParams): void {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingPersons(true);

		this.http.post<Person[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement persons:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingPersons(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				this.formatPersons(result);
				this.persons.set(result);
			});
	}

	/**
	 * Charge les persons pour les qualifications sélectionnées (isMaster = false)
	 */
	private loadPersonsByQualifications(qualificationIds: number[]): void {
		if (qualificationIds.length === 0) {
			this.persons.set([]);
			return;
		}

		let params = new HttpParams();
		qualificationIds.forEach(id => {
			params = params.append('qualificationIds', id.toString());
		});

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingPersons(true);

		this.http.get<Person[]>(this.baseUrl, {params})
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement des personnes par qualifications:', err);
					return of([]);
				}),
				finalize(() => this.maintainState.setLoadingPersons(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				this.formatPersons(result);
				this.persons.set(result);
			});
	}

	private reloadPersonsWithFilters(params: MaintainParams): Observable<Person[]> {
		const body = {
			version: params.version || null,
			canton: params.canton || null,
			webFilters: params.webFilters || [],
			whereFilters: params.whereFilters || []
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingPersons(true);

		return this.http.post<Person[]>(`${this.baseUrl}/search`, body).pipe(
			catchError(err => {
				console.error('❌ Erreur rechargement persons:', err);
				return of([]);
			}),
			finalize(() => this.maintainState.setLoadingPersons(false))
		);
	}

	private reloadPersonsByQualifications(qualificationIds: number[]): Observable<Person[]> {
		if (qualificationIds.length === 0) {
			return of([]);
		}

		let params = new HttpParams();
		qualificationIds.forEach(id => {
			params = params.append('qualificationIds', id.toString());
		});

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.maintainState.setLoadingPersons(true);

		return this.http.get<Person[]>(this.baseUrl, {params}).pipe(
			catchError(err => {
				console.error('❌ Erreur rechargement des personnes par qualifications:', err);
				return of([]);
			}),
			finalize(() => this.maintainState.setLoadingPersons(false))
		);
	}

	private formatPersons(persons: Person[]): void {
		persons.forEach(result => {
			result.classCondition = calculatePersonCondition(result);
		});
	}

	/** Export des personnes avec filtres (master mode) */
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
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => openCsvFile(response, 'Persons.csv'),
			error: (error) => console.error('Erreur lors de l\'export CSV avec filtres', error)
		});
	}

	/** Export des personnes par sélection de qualifications (non-master mode) */
	private exportByQualificationSelection(): void {
		const selectedQualificationIds = this.getSelectedQualificationIds();

		if (selectedQualificationIds.length === 0) {
			console.warn('Aucune personne sélectionnée pour l\'export');
			return;
		}

		const params = new HttpParams().set('qualificationIds', selectedQualificationIds.join(','));

		this.http.get<Blob>(`${this.baseUrl}/export/csv`, {
			params,
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => openCsvFile(response, 'Persons.csv'),
			error: (error) => console.error('Erreur lors de l\'export CSV par sélection', error)
		});
	}

	private getSelectedQualificationIds() {
		return this.selectedQualificationIds();
	}

	/**
	 * Exécute une action POST sur les persons selectionnées et met à jour l'état (souscription)
	 */
	private executeAction(action: string, errorMessage: string): void {
		const selectedPersons = this.maintainState.selectedPersons;
		if (!selectedPersons) return;

		this.executePersonAction$(action, errorMessage)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe();
	}

	/**
	 * Exécute une action POST sur une liste de persons et met à jour l'état
	 */
	private executePersonAction$(action: string, errorMessage: string): Observable<Person | null> {
		const selectedPersons = this.maintainState.selectedPersons();
		if (!selectedPersons?.length) {
			console.warn('Aucune personne sélectionnée');
			return of(null);
		}

		this.isOpRunning.set(true);
		this.opError.set(null);

		const body = {
			personIds: selectedPersons.map(person => person.personId)
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);

		return this.http.post<Person>(`${this.baseUrl}/${action}`, body).pipe(
			catchError(error => {
				console.error(`❌ ${errorMessage}:`, error?.error);
				this.opError.set(error?.error || errorMessage);
				return of(null);
			}),
			finalize(() => this.isOpRunning.set(false))
		);
	}
}
