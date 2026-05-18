import { computed, DestroyRef, inject, Injectable, Signal, signal } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import {
	catchError,
	concat,
	filter,
	finalize,
	Observable,
	of,
	Subject,
	switchMap,
	toArray,
} from 'rxjs';
import { ConfigDelivery } from '../../model/ConfigDelivery';
import { convertDateNumberToDate, convertDateToDateString } from '../../utils/date.utils';
import { convertAsIncrementedParameters, convertAsString } from '../../utils/parameter.util';
import { PredefinedFilter, WhereFilter } from '../../shared/pre-advance-filter/pre-advance-filter';

import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { LanguageService } from '../language';
import { getTranslation } from '../../core/utils/translation.util';
import { openCsvFile } from '../../core/utils/exportCsv.util';
import { School } from '../../model/School';
import { ObHttpApiInterceptorEvents } from '@oblique/oblique';
import { WebFilter } from '../../core/utils/filters.util';
import {normalizeWhereFilterAttribute} from "./configDelivery.utils";

interface ConfigDeliveryParam {
	version?: number;
	canton?: number;
	webFilters?: WebFilter[];
	whereFilters?: WhereFilter[];
	selectedIds?: number[];
}

@Injectable({
	providedIn: 'root',
})
export class ConfigDeliveryService {
	private readonly baseUrl: string = '/sbaweb/api/initialisations/';
	private readonly configDeliveriesUrl: string = this.baseUrl + 'config-deliveries';

	private readonly http = inject(HttpClient);
	private readonly obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private readonly reloadService = inject(LanguageService);
	private readonly destroyRef = inject(DestroyRef);
	private readonly reloadTrigger$ = new Subject<void>();

	// Signaux pour les paramètres et les configDeliveries
	private readonly configDeliveryParams = signal<ConfigDeliveryParam | null>(null);
	private readonly configDeliveriesMap = signal<Map<number, ConfigDelivery>>(new Map());

	// Signal pour la configDelivery sélectionné
	readonly currentConfigDelivery = signal<ConfigDelivery[]>([]);

	// Signaux pour les opérations
	lastUpdatedConfigDelivery = signal<ConfigDelivery | null>(null);
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// Signal pour la sélection des schools
	private selectedSchoolIds = signal<number[]>([]);

	readonly isMaster = signal<boolean>(true);

	readonly afterSave$ = new Subject<void>();

	// Computed public signal
	readonly configDeliveries = computed(() => {
		const map = this.configDeliveriesMap();
		return Array.from(map.values()).map((configDelivery) => this.formatRow(configDelivery));
	});

	// Signaux pour les filtres
	readonly webFilters: Signal<WebFilter[]> = toSignal(
		this.http
			.get<WebFilter[]>(`${this.configDeliveriesUrl}/predefined_filters`)
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
			this.afterSave$.complete();
		});

		this.reloadTrigger$
			.pipe(
				switchMap(() => {
					return this.reloadConfigDeliveries();
				}),
				takeUntilDestroyed(),
			)
			.subscribe((configDelivery) => {
				this.currentConfigDelivery.set(configDelivery);
			});
	}

	loadConfigDeliveries(
		version: number,
		canton?: number,
		webFilters?: WebFilter[],
		whereFilters?: WhereFilter[],
	): void {
		const normalizedWhereFilters = whereFilters?.map(normalizeWhereFilterAttribute);

		this.configDeliveryParams.set({ version, canton });
		this.configDeliveryParams.update((params) => ({
			...params,
			version,
			canton,
			webFilters: webFilters ?? undefined,
			whereFilters: normalizedWhereFilters ?? undefined,
		}));

		let configDeliveryParams = this.configDeliveryParams();

		if (configDeliveryParams) {
			this.doLoadConfigDeliveries(configDeliveryParams);
		}
	}

	loadConfigDeliveriesAsSlave(selectedSchoolIds: number[]) {
		this.selectedSchoolIds.set(selectedSchoolIds);

		// Pour éviter un effet de bord
		selectedSchoolIds = selectedSchoolIds.length > 0 ? selectedSchoolIds : [-1];

		this.configDeliveryParams.update((params) => ({
			...params,
			selectedIds: selectedSchoolIds,
		}));

		let configDeliveryParams = this.configDeliveryParams();

		if (configDeliveryParams) {
			this.doLoadConfigDeliveries(configDeliveryParams);
		}
	}

	selectConfigDelivery(configDelivery: ConfigDelivery[] = []): void {
		this.currentConfigDelivery.set(configDelivery);
	}

	setAsSlave() {
		this.isMaster.set(false);
	}

	emptyData() {
		const newMap = new Map<number, ConfigDelivery>();
		this.configDeliveriesMap.set(newMap);
		this.selectConfigDelivery();
	}

	/* ************* *\
	/*    ACTIONS    *
	/* ************* */
	exportCSV() {
		if (this.isMaster()) {
			this.exportByFilters();
		} else {
			this.exportBySchools();
		}
	}

	switchMaster() {
		this.isMaster.set(true);
	}

	/* *** */
	/* CUD */
	/* *** */
	create(configDeliveries: ConfigDelivery[]) {
		// Créer un tableau d'observables
		const configDeliveryParams = this.configDeliveryParams();
		const body = configDeliveries.map((value) => ({
			...value,
			dlUsers: convertAsString(value.dlUsersParameters, 'DL', 4),
			roUsers: convertAsString(value.roUsersParameters, 'RO', 4),
			referenceDate: value.referenceDateDate,
			dueDate: value.dueDateDate,
			version: configDeliveryParams?.version,
		}));

		const requests$ = body.map((value) =>
			this.http.put<ConfigDelivery>(`${this.configDeliveriesUrl}`, value).pipe(
				catchError((error) => {
					let errorMessage = "Erreur lors de l'insertion d'une configuration de livraison :";
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
					console.log(
						`${successCount}/${configDeliveries.length} configurations de livraison insérées`,
					);

					const params = this.configDeliveryParams();
					if (params) {
						this.doLoadConfigDeliveries(params);
					}
					this.afterSave$.next();
				},
				error: (error) => {
					console.error("Erreur lors de l'insertion d'une configuration de livraison:", error);
				},
			});
	}

	update(configDeliveries: ConfigDelivery[]) {
		const body = configDeliveries.map((value) => ({
			...value,
			dlUsers: convertAsString(value.dlUsersParameters, 'DL', 4),
			roUsers: convertAsString(value.roUsersParameters, 'RO', 4),
			referenceDate: value.referenceDateDate,
			dueDate: value.dueDateDate,
		}));

		// Créer un tableau d'observables
		const requests$ = body.map((value) =>
			this.http.post<ConfigDelivery>(`${this.configDeliveriesUrl}`, value).pipe(
				catchError((error) => {
					let errorMessage = "Erreur lors de la sauvegarde d'une configuration de livraison:";
					console.log(error);
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
					console.log(
						`${successCount}/${configDeliveries.length} configurations de livraison insérées`,
					);

					const params = this.configDeliveryParams();
					if (params) {
						this.doLoadConfigDeliveries(params);
					}
					this.afterSave$.next();
				},
				error: (error) => {
					console.error('Erreur lors de la sauvegarde des configurations de livraison:', error);
				},
			});
	}

	delete(configDeliveries: ConfigDelivery[]) {
		if (!configDeliveries || configDeliveries.length === 0) {
			console.warn('Aucune configuration de livraison à supprimer');
			return;
		}

		// Créer un tableau d'observables (un DELETE par école)
		const requests$ = configDeliveries.map((configDelivery) =>
			this.http.delete<void>(`${this.configDeliveriesUrl}/${configDelivery.deliveryId}`).pipe(
				catchError((error) => {
					let errorMessage = `Erreur lors de la suppression de la configuration de livraison: ${configDelivery.deliveryId}:`;
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
					console.log(
						`${successCount}/${configDeliveries.length} configurations de livraison supprimées`,
					);

					const params = this.configDeliveryParams();
					if (params) {
						this.doLoadConfigDeliveries(params);
					}
					this.afterSave$.next();
				},
				error: (error) => {
					console.error('Erreur globale:', error);
				},
			});
	}

	/* ******************
	 * MÉTHODES PRIVÉES *
	 * **************** */
	private exportByFilters(): void {
		const configDeliveryParams = this.configDeliveryParams();

		const body = {
			version: configDeliveryParams?.version,
			canton: configDeliveryParams?.canton,
			selectedIds: [],
			webFilters: configDeliveryParams?.webFilters ?? [],
			whereFilters: configDeliveryParams?.whereFilters ?? [],
		};

		this.doCsvExport(body);
	}

	private exportBySchools(): void {
		const configDeliveryParams = this.configDeliveryParams();
		const selectedIds = this.getSelectedSchoolIds();

		if (selectedIds.length === 0) {
			console.warn("Aucune école sélectionnée pour l'export par sélection");
			return;
		}

		const body = {
			version: configDeliveryParams?.version,
			selectedIds: selectedIds,
			webFilters: [],
			whereFilters: [],
		};

		this.doCsvExport(body);
	}

	private doCsvExport(body: any) {
		const modeEn = body.selectedIds?.length > 0 ? 'by selection' : 'with filters';
		const modeFr = body.selectedIds?.length > 0 ? 'sélection' : 'filtrage';

		this.http
			.post<Blob>(`${this.configDeliveriesUrl}/export_csv`, body, {
				responseType: 'blob' as 'json',
				observe: 'response',
			})
			.pipe(
				finalize(() => console.log(`Export ${modeEn} completed`)),
				takeUntilDestroyed(this.destroyRef),
			)
			.subscribe({
				next: (response: HttpResponse<Blob>) => {
					openCsvFile(response, 'ConfigDeliveries.csv');
				},
				error: (error) => {
					console.error(
						`Erreur lors de l'export CSV des configurations de livraison par ${modeFr}`,
						error,
					);
				},
			});
	}

	private doLoadConfigDeliveries(configDeliveryParams: ConfigDeliveryParam): void {
		if (!configDeliveryParams) {
			return;
		}

		const body = {
			version: configDeliveryParams.version || null,
			canton: configDeliveryParams.canton || null,
			selectedIds: configDeliveryParams.selectedIds || [],
			webFilters: configDeliveryParams.webFilters || [],
			whereFilters: configDeliveryParams.whereFilters || [],
		};

		this.http
			.post<ConfigDelivery[]>(`${this.configDeliveriesUrl}/search`, body)
			.pipe(
				catchError((err) => {
					console.error('Problème lors du chargement des configurations de livraison :', err);
					return of([]);
				}),
				finalize(() => this.isOpRunning.set(false)),
			)
			.subscribe((result) => {
				// Créer la nouvelle Map
				const newMap = new Map<number, ConfigDelivery>();
				result.forEach((configDelivery) => {
					newMap.set(configDelivery.deliveryId, configDelivery);
				});

				this.configDeliveriesMap.set(newMap);
			});
	}

	private reloadConfigDeliveries(): Observable<ConfigDelivery[]> {
		let configDeliveryParams = this.configDeliveryParams();

		if (!configDeliveryParams) {
			return of([]);
		}

		const body = {
			version: configDeliveryParams.version || null,
			canton: configDeliveryParams.canton || null,
			selectedIds: configDeliveryParams.selectedIds || [],
			webFilters: configDeliveryParams.webFilters || [],
			whereFilters: configDeliveryParams.whereFilters || [],
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);

		return this.http.post<ConfigDelivery[]>(`${this.configDeliveriesUrl}/search`, body).pipe(
			catchError((err) => {
				console.error('Erreur lors du rechargement des configurations de livraisons :', err);
				return of([]);
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}

	private getSelectedSchoolIds() {
		return this.selectedSchoolIds();
	}

	private executeConfigDeliveryAction(action: string, errorMessage: string): void {
		const selectedConfigDeliveries = this.currentConfigDelivery();

		if (!selectedConfigDeliveries) {
			return;
		}

		this.executeConfigDeliveryAction$(action, errorMessage)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe();
	}

	private executeConfigDeliveryAction$(action: string, errorMessage: string) {
		const selectedConfigDeliveries = this.currentConfigDelivery();

		if (!selectedConfigDeliveries?.length) {
			console.warn('Aucune configuration de livraison sélectionnée');
			return of(null);
		}

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isOpRunning.set(true);
		this.opError.set(null);

		const body = {
			selectedIds: selectedConfigDeliveries.map((configDelivery) => configDelivery.deliveryId),
		};

		return this.http.post<School>(`${this.configDeliveriesUrl}/${action}`, body).pipe(
			catchError((error) => {
				console.error(`${errorMessage}:`, error?.error);
				this.opError.set(error?.error || errorMessage);
				return of(null);
			}),
			finalize(() => this.isOpRunning.set(false)),
		);
	}

	/**
	 * Formate une ligne "configDelivery" avec les champs calculés
	 */
	private formatRow(configDelivery: ConfigDelivery): ConfigDelivery {
		return {
			...configDelivery,
			creationDateString: convertDateToDateString(configDelivery.creationDate),
			dueDateDate: convertDateNumberToDate(configDelivery.dueDate),
			modificationDateString: convertDateToDateString(configDelivery.modificationDate),
			referenceDateDate: convertDateNumberToDate(configDelivery.referenceDate),
			roUsersParameters: convertAsIncrementedParameters(configDelivery.roUsers, 'RO', 4),
			dlUsersParameters: convertAsIncrementedParameters(configDelivery.dlUsers, 'DL', 4),
		};
	}
}
