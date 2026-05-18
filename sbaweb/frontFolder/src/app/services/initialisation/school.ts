import {
	DestroyRef,
	Injectable,
	Signal,
	computed,
	effect,
	inject,
	signal,
	untracked,
} from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { School } from '../../model/School';
import { shouldDeleteOnGetBur } from './school.utils';
import {
	Observable,
	Subject,
	catchError,
	concat,
	filter,
	finalize,
	map,
	of,
	switchMap,
	toArray,
} from 'rxjs';
import { PredefinedFilter, WhereFilter } from '../../shared/pre-advance-filter/pre-advance-filter';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { getTranslation } from '../../core/utils/translation.util';
import { LanguageService } from '../language';
import { openCsvFile } from '../../core/utils/exportCsv.util';
import { ObHttpApiInterceptorEvents } from '@oblique/oblique';
import { WebFilter } from '../../core/utils/filters.util';

interface SchoolParams {
	canton?: number;
	version?: number;
	webFilters?: WebFilter[];
	whereFilters?: WhereFilter[];
	selectedConfigDeliveryIds?: number[];
	withSync?: boolean;
}

interface SyncBurResponse {
	status: number;
	message: string | null;
	data: School[] | null;
	success: boolean;
}

interface GetBurResponse {
	status: number;
	message: string | null;
	data: School | null;
	success: boolean;
}

@Injectable({
	providedIn: 'root',
})
export class SchoolService {
	private readonly baseUrl: string = '/sbaweb/api/initialisations/';
	private readonly schoolsUrl: string = `${this.baseUrl}schools`;

	private readonly http = inject(HttpClient);
	private readonly obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private readonly reloadService = inject(LanguageService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly reloadTrigger$ = new Subject<void>();

	// Signaux pour les paramètres et les schools
	private readonly schoolParams = signal<SchoolParams | null>(null);
	private readonly schoolsMap = signal<Map<number, School>>(new Map());

	// Signal pour le school sélectionné
	readonly currentSchools = signal<School[]>([]);

	// Signaux pour les opérations
	lastUpdatedSchool = signal<School | null>(null);
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Signal pour la sélection des config deliveries
	private readonly selectedConfigDeliveryIds = signal<number[]>([]);

	readonly issync_bur = signal<boolean>(false);
	readonly isMaster = signal<boolean>(false);

	// Computed public signal
	readonly schools = computed(() => {
		const map = this.schoolsMap();
		return Array.from(map.values());
	});

	// Signaux pour les filtres
	readonly webFilters: Signal<WebFilter[]> = toSignal(
		this.http
			.get<WebFilter[]>(`${this.schoolsUrl}/predefined_filters`)
			.pipe(filter((value) => !!value)),
		{ initialValue: [] },
	);

	readonly predefinedFilters: Signal<PredefinedFilter[]> = computed(() => {
		const lang = this.reloadService.currentLanguage();
		const data = this.webFilters();

		return data.map((webFilter) => ({
			id: webFilter.filterId || 0,
			label: getTranslation(webFilter, 'name', lang),
			description: getTranslation(webFilter, 'description', lang),
			parameters: webFilter.parameters
				?.map((param) => {
					const name = getTranslation(param, 'name', lang);
					return `${param.uniqueName}:${name}=${param.defaultValue}`;
				})
				.join(';'),
			sql: webFilter.source || '',
			default: webFilter.isDefault || false,
		}));
	});

	constructor() {
		this.destroyRef.onDestroy(() => {
			this.reloadTrigger$.complete();
		});

		this.reloadTrigger$
			.pipe(
				switchMap(() => {
					return this.reloadSchools();
				}),
				takeUntilDestroyed(),
			)
			.subscribe((schools) => {
				this.currentSchools.set(schools);
			});
	}

	loadSchoolsAsSlave(selectedConfigDeliveryIds: number[]) {
		this.selectedConfigDeliveryIds.set(selectedConfigDeliveryIds);

		// Pour éviter un effet de bord
		selectedConfigDeliveryIds =
			selectedConfigDeliveryIds.length > 0 ? selectedConfigDeliveryIds : [-1];

		this.schoolParams.set({ selectedConfigDeliveryIds });
		this.schoolParams.update((params) => ({
			...params,
			selectedIds: selectedConfigDeliveryIds,
		}));

		const schoolParams = this.schoolParams();

		if (schoolParams) {
			this.doLoadSchools(schoolParams);
		}
	}

	loadSchools(
		version: number,
		canton?: number,
		webFilters?: WebFilter[],
		whereFilters?: WhereFilter[],
	) {
		this.schoolParams.set({ version, canton });
		this.schoolParams.update((params) => ({
			...params,
			version,
			canton,
			webFilters: webFilters ?? undefined,
			whereFilters: whereFilters ?? undefined,
		}));

		const schoolParams = this.schoolParams();

		if (schoolParams) {
			this.doLoadSchools(schoolParams);
		}
	}

	selectSchool(schools: School[] = []): void {
		this.currentSchools.set(schools);
	}

	setAsSlave() {
		this.isMaster.set(false);
	}

	emptyData(): void {
		const newMap = new Map<number, School>();
		this.schoolsMap.set(newMap);
		this.selectSchool();
	}

	reload(): void {
		const params = this.schoolParams();
		if (params) {
			this.doLoadSchools(params);
		}
	}

	/* ************* *
	 *    ACTIONS    *
	 * ************* */
	exportCSV(): void {
		if (this.isMaster()) {
			this.exportByFilters();
		} else {
			this.exportByConfigDelivery();
		}
	}

	switchMaster(): void {
		this.isMaster.set(true);
	}

	getBur(selectedSchools: School[], version: number): void {
		if (!selectedSchools?.length) {
			return;
		}

		this.startBurAction();

		const requests$ = selectedSchools.map((school) =>
			this.http.post<GetBurResponse>(`${this.schoolsUrl}/get_bur`, { ...school, version }).pipe(
				catchError((error) => {
					console.error('Error importing BUR school:', error?.error);
					this.opError.set(error?.error || 'Error importing BUR school');
					return of(null);
				}),
				map((response) => ({ response, originalSchool: school })),
			),
		);

		concat(...requests$)
			.pipe(
				toArray(),
				finalize(() => this.isOpRunning.set(false)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe((results: Array<{ response: GetBurResponse | null; originalSchool: School }>) => {
				const newMap = new Map(this.schoolsMap());
				results.forEach(({ response, originalSchool }) => {
					if (response !== null) {
						if (response.message) {
							response.success
								? this.opWarning.set(response.message)
								: this.opError.set(response.message);
						}
						if (response.success) {
							if (response.data === null) {
								newMap.set(originalSchool.schoolId, {
									...originalSchool,
									isDeleted: true,
									syncParameter: true,
								});
							} else {
								const updated = this.applyBurTransformation(response.data, version);
								newMap.set(updated.schoolId, updated);
							}
						}
					}
				});
				this.schoolsMap.set(newMap);
			});
	}

	sync_bur(): void {
		this.startBurAction();

		this.http
			.post<SyncBurResponse>(`${this.schoolsUrl}/sync_bur`, null)
			.pipe(
				catchError((error) => {
					console.error('Error when synchronizing BUR:', error?.error);
					this.opError.set(error?.error || 'Error when synchronizing BUR');
					return of(null);
				}),
				finalize(() => this.isOpRunning.set(false)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe((response) => {
				if (response) {
					this.issync_bur.set(response.success);
					this.handleBurActionMessage(response);
					const params = this.schoolParams();
					this.schoolParams.update((p) => (p ? { ...p, withSync: response.success } : p));
					if (params) {
						this.doLoadSchools({ ...params, withSync: response.success });
					}
				}
			});
	}

	getAllBur(version: number): void {
		const canton = this.schoolParams()?.canton ?? 0;
		this.startBurAction();

		this.http
			.post<SyncBurResponse>(`${this.schoolsUrl}/get_all_bur/${canton}`, null)
			.pipe(
				catchError((error) => {
					console.error('Error when getting all BUR data:', error?.error);
					this.opError.set(error?.error || 'Error when getting all BUR data');
					return of(null);
				}),
				finalize(() => this.isOpRunning.set(false)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe((response) => {
				if (response) {
					this.handleBurActionMessage(response);
					if (response.data != null) {
						this.schoolsMap.set(this.buildSchoolMap(response.data, version));
					}
				}
			});
	}

	/* *** */
	/* CUD */
	/* *** */
	create(schools: School[]): void {
		// Créer un tableau d'observables
		const requests$ = schools.map((value) =>
			this.http.put<School>(`${this.schoolsUrl}`, value).pipe(
				catchError((error) => {
					const errorMessage = "Erreur lors de l'insertion d'une école:";
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
			),
		);

		// Exécuter les appels séquentiellement
		concat(...requests$)
			.pipe(toArray(), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${schools.length} écoles insérées`);
					this.reloadTrigger$.next();
				},
				error: (error) => {
					console.error("Erreur lors de l'insertion d'une école:", error);
				},
			});
	}

	update(schools: School[]): void {
		// Créer un tableau d'observables
		const requests$ = schools.map((value) =>
			this.http.post<School>(`${this.schoolsUrl}`, value).pipe(
				catchError((error) => {
					const errorMessage = "Erreur lors de la sauvegarde d'une école:";
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
			),
		);

		// Exécuter les appels séquentiellement
		concat(...requests$)
			.pipe(toArray(), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${schools.length} écoles sauvegardées`);
					const params = this.schoolParams();
					if (params) {
						this.doLoadSchools(params);
					}
				},
				error: (error) => {
					console.error('Erreur lors de la sauvegarde des écoles:', error);
				},
			});
	}

	delete(schools: School[]): void {
		if (schools?.length === 0) {
			console.warn('Aucune école à supprimer');
			return;
		}

		// Créer un tableau d'observables (un DELETE par école)
		const requests$ = schools.map((school) =>
			this.http.delete<void>(`${this.schoolsUrl}/${school.schoolId}`).pipe(
				catchError((error) => {
					const errorMessage = `Erreur lors de la suppression de l'école: ${school.schoolId}:`;
					console.error(errorMessage, error);
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
			),
		);

		// Exécuter les appels séquentiellement (un après l'autre)
		concat(...requests$)
			.pipe(
				toArray(), // Rassembler tous les résultats
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${schools.length} écoles supprimées`);
					this.reloadTrigger$.next();
				},
				error: (error) => {
					console.error('Erreur globale:', error);
				},
			});
	}

	/* **************** *
	 * MÉTHODES PRIVÉES *
	 * **************** */
	private exportByFilters(): void {
		const schoolParams = this.schoolParams();

		const body = {
			version: schoolParams?.version,
			canton: schoolParams?.canton,
			selectedIds: [],
			webFilters: schoolParams?.webFilters ?? [],
			whereFilters: schoolParams?.whereFilters ?? [],
		};

		this.doCsvExport(body);
	}

	private exportByConfigDelivery(): void {
		const schoolParams = this.schoolParams();
		const selectedIds = this.getSelectedConfigDeliveryIds();

		if (selectedIds.length === 0) {
			console.warn("Aucune configuration de livraison sélectionnée pour l'export");
			return;
		}

		const body = {
			version: schoolParams?.version,
			selectedIds,
			webFilters: [],
			whereFilters: [],
			withSync: false,
		};

		this.doCsvExport(body);
	}

	private doCsvExport(body: any) {
		const modeEn = body.selectedIds?.length > 0 ? 'by selection' : 'with filters';
		const modeFr = body.selectedIds?.length > 0 ? 'sélection' : 'filtrage';

		this.http
			.post<Blob>(`${this.schoolsUrl}/export_csv`, body, {
				responseType: 'blob' as 'json',
				observe: 'response',
			})
			.pipe(
				finalize(() => console.log(`Export ${modeEn} completed`)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe({
				next: (response: HttpResponse<Blob>) => {
					openCsvFile(response, 'BurSchools.csv');
				},
				error: (error) => {
					console.error(`Erreur lors de l'export CSV des BurSchool par ${modeFr}`, error);
				},
			});
	}

	private doLoadSchools(schoolParams: SchoolParams, burImportVersion?: number): void {
		if (!schoolParams) {
			return;
		}

		const body = {
			version: schoolParams.version || null,
			canton: schoolParams.canton || null,
			selectedIds: schoolParams.selectedConfigDeliveryIds || [],
			webFilters: schoolParams.webFilters || [],
			whereFilters: schoolParams.whereFilters || [],
			withSync: schoolParams.withSync ?? false,
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);

		this.http
			.post<School[]>(`${this.schoolsUrl}/search`, body)
			.pipe(
				catchError((err) => {
					console.error('Problème lors du chargement des BurSchools:', err);
					return of([]);
				}),
				finalize(() => this.isOpRunning.set(false)),
			)
			.subscribe((result) => {
				this.schoolsMap.set(this.buildSchoolMap(result, burImportVersion));
			});
	}

	private applyBurTransformation(school: School, version: number): School {
		const updated: School = { ...school, syncParameter: true };
		if (shouldDeleteOnGetBur(school, version)) {
			updated.isDeleted = true;
		} else {
			updated.canton = school.cantonBur;
			updated.label = school.nameBur;
			updated.charPublFlg = school.burCharPublFlg;
			updated.charPrivSubFlg = school.burCharPrivSubFlg;
			updated.charPrivNoSubFlg = school.burCharPrivNoSubFlg;
			updated.isSpecialSchool = school.isSpecialSchoolBur;
			updated.municipality = school.municipalityBur;
			updated.isModified = true;
		}
		return updated;
	}

	private buildSchoolMap(schools: School[], burImportVersion?: number): Map<number, School> {
		const newMap = new Map<number, School>();
		schools.forEach((school) => {
			if (burImportVersion !== undefined) {
				const updated = this.applyBurTransformation(school, burImportVersion);
				newMap.set(updated.schoolId, updated);
			} else {
				newMap.set(school.schoolId, { ...school });
			}
		});
		return newMap;
	}

	private reloadSchools(): Observable<School[]> {
		const schoolParams = this.schoolParams();

		if (!schoolParams) {
			return of([]);
		}

		const body = {
			version: schoolParams.version || null,
			canton: schoolParams.canton || null,
			selectedIds: schoolParams.selectedConfigDeliveryIds || [],
			webFilters: schoolParams.webFilters || [],
			whereFilters: schoolParams.whereFilters || [],
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);

		return this.http.post<School[]>(`${this.schoolsUrl}/search`, body).pipe(
			catchError((err) => {
				console.error('Erreur lors du rechargement des écoles :', err);
				return of([]);
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}

	private getSelectedConfigDeliveryIds() {
		return this.selectedConfigDeliveryIds();
	}

	private startBurAction(): void {
		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);
		this.opError.set(null);
		this.opWarning.set(null);
	}

	private handleBurActionMessage(response: SyncBurResponse): void {
		if (response.message) {
			if (response.success) {
				this.opWarning.set(response.message);
			} else {
				this.opError.set(response.message);
			}
		}
	}

	private executeSchoolAction(action: string, errorMessage: string): void {
		const selectedSchools = this.currentSchools();

		if (!selectedSchools) {
			return;
		}

		this.executeSchoolAction$(action, errorMessage)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe();
	}

	private executeSchoolAction$(action: string, errorMessage: string) {
		const selectedSchools = this.currentSchools();

		if (!selectedSchools?.length) {
			console.warn('Aucune école sélectionnée');
			return of(null);
		}

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);
		this.opError.set(null);

		const body = {
			selectedIds: selectedSchools.map((school) => school.schoolId),
		};

		return this.http.post<School>(`${this.schoolsUrl}/${action}`, body).pipe(
			catchError((error) => {
				console.error(`${errorMessage}:`, error?.error);
				this.opError.set(error?.error || errorMessage);
				return of(null);
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}
}
