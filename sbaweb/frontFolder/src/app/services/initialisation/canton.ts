import { computed, DestroyRef, effect, inject, Injectable, Signal, signal } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { catchError, concat, filter, finalize, map, Observable, of, tap, toArray } from 'rxjs';
import { Canton, CantonParams } from '../../model/Canton';
import { openCsvFile } from '../../core/utils/exportCsv.util';
import { convertDateToDateString } from '../../utils/date.utils';
import { calculateCantonRowStyle } from './canton.utils';
import { PredefinedFilter } from '../../shared/pre-advance-filter/pre-advance-filter';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { getTranslation } from '../../core/utils/translation.util';
import { LanguageService } from '../language';
import { LastFilters, WebFilter } from '../../core/utils/filters.util';
import { PlausiError } from '../../shared/plausi-error-editor/plausi-error.model';
import { DialogService } from '../dialog';

export interface InitVersionResponse {
	status: number;
	message: string | null;
	data: boolean;
	success: boolean;
}

@Injectable({
	providedIn: 'root',
})
export class CantonService {
	private readonly baseUrl: string = '/sbaweb/api/initialisations/';
	private readonly cantonsUrl: string = this.baseUrl + 'cantons';

	private readonly languageService = inject(LanguageService);
	private readonly dialogService = inject(DialogService);
	private readonly http = inject(HttpClient);
	private readonly destroyRef = inject(DestroyRef);

	// Signaux pour les param�tres et les cantons
	private readonly cantonParams = signal<CantonParams | null>(null);
	private readonly cantonsMap = signal<Map<number, Canton>>(new Map());

	// Signal pour le canton s�lectionn�
	readonly currentCanton = signal<Canton | null>(null);

	readonly missingCantons = signal<number[]>([]);

	// Signaux pour les op�rations
	lastUpdatedCanton = signal<Canton | null>(null);
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	private readonly onPlausiErrorsLoading = signal<boolean>(false);

	// Computed public signal
	readonly cantons = computed(() => {
		const map = this.cantonsMap();

		let cantons = Array.from(map.values());
		this.formatRow(cantons);

		return cantons; //.map(canton => this.formatRow(canton));
	});

	readonly webFilters: Signal<WebFilter[]> = toSignal(
		this.http
			.get<WebFilter[]>(`${this.cantonsUrl}/predefined_filters`)
			.pipe(filter((value) => !!value)),
		{ initialValue: [] },
	);

	readonly predefinedFilters: Signal<PredefinedFilter[]> = computed(() => {
		const lang = this.languageService.currentLanguage();
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

	readonly lastFilters: Signal<LastFilters | undefined> = toSignal(
		this.http.get<LastFilters>(`${this.cantonsUrl}/lastFilters`).pipe(filter((value) => !!value)),
		{ initialValue: undefined },
	);

	constructor() {
		// Chargement des cantons quand les params changent
		effect(() => {
			const params = this.cantonParams();
			if (!this.areParamsEmpty(params)) {
				this.doLoadCantons(params!);
			}
		});
	}

	loadCantons(version: number, canton?: number) {
		this.cantonParams.set({ version: version, canton });
		this.cantonParams.update((params) => ({
			...params,
			version: version,
			canton,
		}));

		let cantonParams = this.cantonParams();
		if (cantonParams) {
			this.doLoadCantons(cantonParams);
		}
	}

	selectCanton(canton: Canton): void {
		this.currentCanton.set(canton);
	}

	/* ************* *\
	/*    ACTIONS    *
	/* ************* */
	initVersion(
		version: number,
		canton: number,
		syncSchool: boolean,
	): Observable<InitVersionResponse> {
		const body = {
			version: version,
			canton: canton,
			noSync: !syncSchool,
		};

		this.isOpRunning.set(true);
		this.opError.set(null);
		this.opWarning.set(null);

		return this.http.post<InitVersionResponse>(`${this.cantonsUrl}/init_version`, body).pipe(
			tap((response) => {
				if (response?.message) {
					if (response.success) {
						this.opWarning.set(response.message);
					} else {
						this.opError.set(response.message);
					}
				}
			}),
			catchError((error) => {
				const message = error?.error || 'Erreur lors de l’initialisation';
				this.opError.set(message);
				return of({
					status: 500,
					message,
					data: false,
					success: false,
				});
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}

	exportCSV() {
		const cantonParams = this.cantonParams();

		const body = {
			version: cantonParams?.version ?? null,
			canton: cantonParams?.canton ?? null,
			webFilters: cantonParams?.webFilters ?? [],
			whereFilters: cantonParams?.whereFilters ?? [],
		};

		this.http
			.post<Blob>(`${this.cantonsUrl}/export_csv`, body, {
				responseType: 'blob' as 'json',
				observe: 'response',
			})
			.pipe(finalize(() => console.log('Export completed')))
			.subscribe({
				next: (response: HttpResponse<Blob>) => {
					openCsvFile(response, 'Cantons.csv');
				},
				error: (error) => {
					console.error("Erreur lors de l'export CSV", error);
				},
			});
	}

	finalise() {
		this.executeCantonAction('finalize', 'Error when finalizing');
	}

	undoFinalise() {
		this.executeCantonAction('undo_finalize', 'Error when undoing finalize');
	}

	validate() {
		this.executeCantonAction('validate', 'Error when validating');
	}

	undoValidate() {
		this.executeCantonAction('undo_validate', 'Error when unvalidating');
	}

	createPlausiReport() {
		this.executeCantonAction('create_plausi_report', 'Error when creating plausi report');
	}

	showPlausiReport() {
		const cantonId = this.currentCanton()?.cantonId;

		if (!cantonId) {
			return;
		}

		this.http
			.get(`${this.cantonsUrl}/show_last_plausi_report/${cantonId}`, {
				responseType: 'blob',
				observe: 'response',
			})
			.pipe(finalize(() => console.log('Plausi Report completed')))
			.subscribe({
				next: (response: HttpResponse<Blob>) => {
					openCsvFile(response, 'plausiReport.xlsx');
				},
				error: (error) => {
					console.error("Erreur lors de l'export du rapport de plausibilisation", error);
				},
			});
	}

	/* *** */
	/* CUD */
	/* *** */
	create(cantons: Canton[]): void {
		if (!cantons || cantons.length === 0) {
			console.warn('Aucun canton � ins�rer');
			return;
		}

		this.isOpRunning.set(true);
		this.opError.set(null);

		const cantonParams = this.cantonParams();

		cantons.forEach((canton) => {
			canton.version = cantonParams?.version ?? -1;
		});

		// Cr�er un tableau d'observables (un POST par canton)
		const requests$ = cantons.map((canton) =>
			this.http.put<Canton>(`${this.cantonsUrl}`, canton).pipe(
				catchError((error) => {
					let errorMessage = "Erreur lors de la sauvegarde d'un canton :";
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
				finalize(() => this.isOpRunning.set(false)),
			),
		);

		// Ex�cuter les appels s�quentiellement
		concat(...requests$)
			.pipe(toArray(), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${cantons.length} cantons ins�r�s`);

					const params = this.cantonParams();
					if (params) {
						this.doLoadCantons(params);
					}
				},
				error: (error) => {
					console.error("Erreur lors de l'insertion d'un/de canton(s):", error);
				},
			});
	}

	update(cantons: Canton[]): void {
		// Cr�er un tableau d'observables
		const requests$ = cantons.map((value) =>
			this.http.post<Canton>(`${this.cantonsUrl}`, value).pipe(
				catchError((error) => {
					let errorMessage = "Erreur lors de la sauvegarde d'un canton:";
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
			),
		);

		// Ex�cuter les appels s�quentiellement
		concat(...requests$)
			.pipe(toArray(), takeUntilDestroyed(this.destroyRef))
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${cantons.length} cantons ins�r�s`);

					const params = this.cantonParams();
					if (params) {
						this.doLoadCantons(params);
					}
				},
				error: (error) => {
					console.error('Erreur lors de la sauvegarde des cantons:', error);
				},
			});
	}

	delete(cantons: Canton[]): void {
		if (!cantons || cantons.length === 0) {
			console.warn('Aucun canton � supprimer');
			return;
		}

		// Cr�er un tableau d'observables (un DELETE par canton)
		const requests$ = cantons.map((canton) =>
			this.http.delete<Canton>(`${this.cantonsUrl}/${canton.cantonId}`).pipe(
				catchError((error) => {
					let errorMessage = `Erreur lors de la suppression du canton : ${canton.cantonId}:`;
					console.error(errorMessage, error);
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
			),
		);

		// Ex�cuter les appels s�quentiellement
		concat(...requests$)
			.pipe(
				toArray(), // Rassembler tous les r�sultats
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe({
				next: (results) => {
					const successCount = results.filter((r) => r !== null).length;
					console.log(`${successCount}/${cantons.length} cantons supprim�s`);

					const params = this.cantonParams();
					if (params) {
						this.doLoadCantons(params);
					}
				},
				error: (error) => {
					console.error('Erreur globale :', error);
				},
			});
	}

	getMissingCantons(): void {
		const versionId = this.cantonParams()?.version ?? -1;

		const params = new HttpParams().set('version', versionId);

		this.http
			.get<number[]>(`${this.cantonsUrl}/missing_cantons/`, { params })
			.pipe(
				catchError((err) => {
					const errorMessage = `Erreur lors de la recherche des cantons manquants pour la version ${versionId} : ${err}`;
					console.error(errorMessage);
					return of([]);
				}),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe((result) => {
				this.missingCantons.set(result);
			});
	}

	public getPlausiErrors(cantonId: number): Observable<PlausiError[]> {
		if (!cantonId || this.onPlausiErrorsLoading()) {
			return of([]);
		}

		this.onPlausiErrorsLoading.set(true);
		return this.http.get<PlausiError[]>(`${this.cantonsUrl}/${cantonId}/plausi_errors`).pipe(
			map((plausiErrors) => {
				plausiErrors.forEach(
					(plausiError) =>
						(plausiError.modificationDateString = new Date(
							plausiError.modificationDate,
						).toLocaleDateString('fr-CH')),
				);
				return plausiErrors;
			}),
			catchError((err) => {
				console.error('Erreur chargement des plausi errors:', err);
				return of([]);
			}),
			finalize(() => this.onPlausiErrorsLoading.set(false)),
		);
	}

	/* **************** */
	/* M�THODES PRIV�ES */
	/* **************** */
	/**
	 * Charge les cantons
	 */
	private doLoadCantons(cantonParams: CantonParams): void {
		if (!cantonParams) {
			return;
		}

		const body = {
			version: cantonParams.version || null,
			canton: cantonParams.canton || null,
		};

		this.http
			.post<Canton[]>(`${this.cantonsUrl}/search`, body)
			.pipe(
				catchError((err) => {
					console.error('Probl�me lors du chargement des cantons:', err);
					return of([]);
				}),
			)
			.subscribe((result) => {
				// Cr�er la nouvelle Map
				const newMap = new Map<number, Canton>();
				result.forEach((canton) => {
					newMap.set(canton.cantonId, canton);
				});
				this.cantonsMap.set(newMap);
				this.formatRow(result);
			});
	}

	/**
	 * Formate une ligne "canton" avec les champs calcul�s
	 */
	private formatRow(cantons: any[] | Canton[]) {
		cantons.forEach((canton) => {
			canton.creationDateString = convertDateToDateString(canton.creationDate);
			canton.finalisationDateString = convertDateToDateString(canton.finalisationDate);
			canton.modificationDateString = convertDateToDateString(canton.modificationDate);
			canton.plausiDateString = convertDateToDateString(canton.plausiDate);
			canton.validationDateString = convertDateToDateString(canton.validationDate);
			canton.classCondition = calculateCantonRowStyle(canton);
		});
	}

	private updateCanton(canton: Canton): void {
		this.cantonsMap.update((map) => {
			const newMap = new Map(map);
			newMap.set(canton.cantonId, canton);
			return newMap;
		});

		// Mettre � jour currentCanton dans le cas o� il s'agit du m�me
		if (this.currentCanton()?.cantonId === canton.cantonId) {
			this.currentCanton.set(canton);
		}
	}

	private executeCantonAction(action: string, errorMessage: string): void {
		const cantonId = this.currentCanton()?.cantonId;

		if (!cantonId) {
			return;
		}

		this.executeCantonAction$(action, errorMessage).subscribe();
	}

	private executeCantonAction$(action: string, errorMessage: string) {
		const cantonId = this.currentCanton()?.cantonId;

		if (!cantonId) {
			return of(null);
		}

		this.isOpRunning.set(true);

		return this.http.post<Canton>(`${this.cantonsUrl}/${action}/${cantonId}`, {}).pipe(
			tap((canton) => {
				if (canton) {
					this.lastUpdatedCanton.set(canton);
					this.updateCanton(canton);
				}
			}),
			catchError((error) => {
				console.error(`${errorMessage}:`, error?.error);
				this.opError.set(error?.error || errorMessage);
				return of(null);
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}

	private areParamsEmpty(params: CantonParams | null): boolean {
		return (
			!params ||
			(params.version === undefined &&
				params.canton === undefined &&
				(!params.webFilters || params.webFilters.length === 0) &&
				(!params.whereFilters || params.whereFilters.length === 0))
		);
	}
}
