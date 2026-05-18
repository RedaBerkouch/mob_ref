import {computed, DestroyRef, effect, inject, Injectable, Signal, signal} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {catchError, filter, finalize, interval, map, Observable, of, retry, switchMap, takeWhile, tap} from 'rxjs';
import {takeUntilDestroyed, toSignal} from "@angular/core/rxjs-interop";
import {Delivery, DeliveryParams, LastFilters, MEB_DELIVERYSTATUS, WebFilter} from "../../model/Delivery";
import {calculateDeliveryCondition} from "./delivery.utils";
import {TranslateService} from "@ngx-translate/core";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {getTranslation} from "../../core/utils/translation.util";
import {LanguageService} from "../language";
import {PredefinedFilter, WhereFilter} from "../../shared/pre-advance-filter/pre-advance-filter";
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";

@Injectable({
	providedIn: 'root'
})
export class DeliveryService {
	private readonly baseUrl = '/sbgweb/api/deliveries';
	private readonly LOADING_STATES = ['delivery.tobeloaded'] as const;

	private reloadService = inject(LanguageService);
	private http = inject(HttpClient);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private destroyRef = inject(DestroyRef);
	private translate = inject(TranslateService);

	// ** Signals pour les paramètres et les deliveries
	deliveryParams = signal<DeliveryParams | null>(null);
	private deliveriesMap = signal<Map<number, Delivery>>(new Map());
	private pollingDeliveries = new Set<number>();
	private pollingRefreshDeliveries = new Set<number>();
	private onPlausiErrorsLoading = signal<boolean>(false);
	readonly isLoading = signal<boolean>(false);

	// ** Signal pour la delivery sélectionnée
	currentDelivery = signal<Delivery | null>(null);

	// ** Signals pour les opérations
	lastUpdatedDelivery = signal<Delivery | null>(null);
	isOpRunning = signal<boolean>(false);
	opError = signal<string | null>(null);
	opWarning = signal<string | null>(null);

	// ** Computed signals publics
	readonly deliveries = computed(() => {
		const map = this.deliveriesMap();
		return Array.from(map.values()).map(delivery => this.formatDelivery(delivery));
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

	readonly lastFilters: Signal<LastFilters | undefined> = toSignal(
		this.http.get<LastFilters>(`${this.baseUrl}/lastFilters`).pipe(
			filter(value => !!value)
		),
		{initialValue: undefined}
	);

	constructor() {
		// ** Effect pour charger les deliveries quand les params changent
		effect(() => {
			const params = this.deliveryParams();
			if (!this.isDeliveryParamsEmpty(params)) {
				this.loadDeliveriesInternal(params!);
			}
		});

		this.destroyRef.onDestroy(() => {
			this.stopAllPolling();
		});
	}

	// =====================
	// MÉTHODES PUBLIQUES
	// =====================

	loadDeliveries(year?: number, canton?: number, webFilters?: WebFilter[], whereFilters?: WhereFilter[]): void {
		this.deliveryParams.update(params => ({
			...params,
			version: year,
			canton: canton,
			webFilters,
			whereFilters
		}));
	}

	reloadCurrentDeliveries(): void {
		const params = this.deliveryParams();
		if (params) {
			this.loadDeliveriesInternal(params);
		}
	}

	selectDelivery(delivery: Delivery | null): void {
		this.currentDelivery.set(delivery);
	}

	refreshDelivery(deliveryid: number): void {
		this.http.get<Delivery>(`${this.baseUrl}/${deliveryid}`)
			.pipe(
				catchError(err => {
					console.error(`❌ Erreur refresh ${deliveryid}:`, err);
					return of(null);
				}),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				if (result) {
					this.updateDelivery(result);

					if (this.needsPolling(result)) {
						this.startPollingForDelivery(deliveryid);
					}

					if (this.needsRefreshPolling(result)) {
						this.startPollingRefreshForDelivery(deliveryid);
					}
				}
			});
	}

	// =====================
	// ACTIONS SUR LES DELIVERIES
	// =====================

	exportDelivery(): void {
		const deliveryParams = this.deliveryParams();

		const body = {
			version: deliveryParams?.version ?? null,
			canton: deliveryParams?.canton ?? null,
			webFilters: deliveryParams?.webFilters ?? [],
			whereFilters: deliveryParams?.whereFilters ?? []
		};

		this.http.post<Blob>(`${this.baseUrl}/export/csv`, body, {
			responseType: 'blob' as 'json',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'deliveries_export.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV', error);
			}
		});
	}

	amendDelivery(): void {
		this.executeDeliveryAction('amend', 'Error when amending delivery');
	}

	replaceDelivery(): void {
		this.executeDeliveryAction('replace', 'Error when replacing delivery');
	}

	confirmDelivery(): void {
		this.executeDeliveryAction$('confirm', 'Error when confirming delivery')
			.pipe(
				filter(delivery => !!delivery),
				tap(() => {
					this.opWarning.set(this.translate.instant('upload.deliveryWithErrors.message'));
				}),
				tap(() => this.showPlausiReportDelivery()),
				finalize(() => {
					this.opWarning.set(null);
				}),
				catchError(error => {
					console.error('Error confirming delivery:', error);
					return of(null);
				}),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe();
	}

	cancelDelivery(): void {
		this.executeDeliveryAction('cancel', 'Error when cancelling delivery');
	}

	prevalidateDelivery(): void {
		this.executeDeliveryAction('validate', 'Error when validating delivery');
	}

	undoValidate(): void {
		this.executeDeliveryAction('undo_validate', 'Error when unvalidating delivery');
	}

	createPlausiReport(): void {
		this.executeDeliveryAction('plausi_report', 'Error when creating plausi report delivery');
	}

	showPlausiReportDelivery(): void {
		const deliveryid = this.currentDelivery()?.deliveryid;
		if (!deliveryid) return;

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);

		this.http.get(`${this.baseUrl}/${deliveryid}/plausi_report`, {
			responseType: 'blob',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Plausi Report completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'plausireport.xlsx');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export plausi report', error);
			}
		});
	}

	saveDelivery(): void {
		const deliveryid = this.currentDelivery()?.deliveryid;
		if (!deliveryid) return;
		console.log('💾 Save delivery:', deliveryid);
	}

	deleteDelivery(): void {
		const deliveryid = this.currentDelivery()?.deliveryid;
		if (!deliveryid) return;

		this.isOpRunning.set(true);
		this.opError.set(null);

		this.stopPollingForDelivery(deliveryid);
		this.stopPollingRefreshForDelivery(deliveryid); // Arrêter aussi le polling refresh

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.http.delete<void>(`${this.baseUrl}/${deliveryid}`)
			.pipe(
				catchError(error => {
					this.opError.set(error.error?.message || 'Error when deleting delivery');
					return of(null);
				}),
				finalize(() => this.isOpRunning.set(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(() => {
				// Recharger directement
				const params = this.deliveryParams();
				if (params) {
					this.loadDeliveriesInternal(params);
				}
			});
	}

	// =====================
	// MÉTHODES PRIVÉES
	// =====================

	private isDeliveryParamsEmpty(params: DeliveryParams | null): boolean {
		return !params || (
			params.version === undefined &&
			params.canton === undefined &&
			(!params.webFilters || params.webFilters.length === 0) &&
			(!params.whereFilters || params.whereFilters.length === 0)
		);
	}

	private executeDeliveryAction(action: string, errorMessage: string): void {
		const deliveryid = this.currentDelivery()?.deliveryid;
		if (!deliveryid) return;

		this.executeDeliveryAction$(action, errorMessage)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe();
	}

	/**
	 * Exécute une action POST sur une delivery et met à jour l'état (observable)
	 */
	private executeDeliveryAction$(action: string, errorMessage: string) {
		const deliveryid = this.currentDelivery()?.deliveryid;
		if (!deliveryid) {
			return of(null);
		}

		this.isOpRunning.set(true);
		this.opError.set(null);

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		return this.http.post<Delivery>(`${this.baseUrl}/${deliveryid}/${action}`, {})
			.pipe(
				tap(delivery => {
					if (delivery) {
						this.lastUpdatedDelivery.set(delivery);
						this.updateDelivery(delivery);

						// Redémarrer le polling si nécessaire
						if (this.needsPolling(delivery)) {
							this.startPollingForDelivery(deliveryid);
						}

						// Démarrer le polling refresh si nécessaire
						if (this.needsRefreshPolling(delivery)) {
							this.startPollingRefreshForDelivery(deliveryid);
						}
					}
				}),
				catchError(error => {
					console.error(`❌ ${errorMessage}:`, error?.error);
					this.opError.set(error?.error || errorMessage);
					return of(null);
				}),
				finalize(() => this.isOpRunning.set(false))
			);
	}

	private loadDeliveriesInternal(deliveryParams: DeliveryParams): void {
		this.stopAllPolling();

		const body = {
			version: deliveryParams.version || null,
			canton: deliveryParams.canton || null,
			webFilters: deliveryParams.webFilters || [],
			whereFilters: deliveryParams.whereFilters || []
		};

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		this.isLoading.set(true);

		this.http.post<Delivery[]>(`${this.baseUrl}/search`, body)
			.pipe(
				catchError(err => {
					console.error('❌ Erreur chargement deliveries:', err);
					return of([]);
				}),
				finalize(() => this.isLoading.set(false)),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe(result => {
				const newMap = new Map<number, Delivery>();
				result.forEach(delivery => {
					newMap.set(delivery.deliveryid, delivery);

					if (this.needsPolling(delivery)) {
						this.startPollingForDelivery(delivery.deliveryid);
					}

					if (this.needsRefreshPolling(delivery)) {
						this.startPollingRefreshForDelivery(delivery.deliveryid);
					}
				});

				this.deliveriesMap.set(newMap);
				console.log('result', newMap)
			});
	}

	private updateDelivery(delivery: Delivery): void {
		this.deliveriesMap.update(map => {
			const newMap = new Map(map);
			newMap.set(delivery.deliveryid, delivery);
			return newMap;
		});

		if (this.currentDelivery()?.deliveryid === delivery.deliveryid) {
			this.currentDelivery.set(delivery);
		}
	}

	private formatDelivery(delivery: Delivery): Delivery {
		return {
			...delivery,
			classCondition: calculateDeliveryCondition(delivery),
			nrplausiperson: this.translateLoadingState(delivery.nrplausiperson),
			nrplausievent: this.translateLoadingState(delivery.nrplausievent)
		};
	}

	private translateLoadingState(value: string): string {
		const loadingStates: Record<string, string> = {
			'delivery.tobeloaded': this.translate.instant('delivery.tobeloaded'),
			'delivery.queued': this.translate.instant('delivery.queued')
		};

		return loadingStates[value] || value;
	}

	private needsPolling(delivery: Delivery): boolean {
		return this.LOADING_STATES.includes(delivery.nrplausiperson as any)
			|| this.LOADING_STATES.includes(delivery.nrplausievent as any);
	}

	private needsRefreshPolling(delivery: Delivery): boolean {
		return delivery.status === MEB_DELIVERYSTATUS.IMPORTED || (delivery as any).creatingReport;
	}

	private startPollingForDelivery(deliveryid: number): void {
		if (this.pollingDeliveries.has(deliveryid)) {
			return;
		}

		this.pollingDeliveries.add(deliveryid);

		interval(2000)
			.pipe(
				switchMap(() => {
					this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);

					return this.http.get<Delivery>(`${this.baseUrl}/${deliveryid}`).pipe(
						catchError(err => {
							console.error(`❌ Erreur polling ${deliveryid}:`, err);
							return of(null);
						})
					);
				}),
				filter(result => result != null),
				takeWhile(delivery => {
					const shouldContinue = this.needsPolling(delivery);

					if (!shouldContinue) {
						this.pollingDeliveries.delete(deliveryid);
					}

					return shouldContinue;
				}, true),
				retry({count: 3, delay: 2000}),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe({
				next: (delivery) => {
					this.updateDelivery(delivery);
				},
				error: (err) => {
					console.error(`❌ Erreur finale polling ${deliveryid}:`, err);
					this.pollingDeliveries.delete(deliveryid);
				},
				complete: () => {
					this.pollingDeliveries.delete(deliveryid);
				}
			});
	}

	private startPollingRefreshForDelivery(deliveryid: number): void {
		if (this.pollingRefreshDeliveries.has(deliveryid)) {
			return;
		}

		console.log(`🔄 Démarrage polling refresh pour ${deliveryid}`);
		this.pollingRefreshDeliveries.add(deliveryid);

		interval(20000)
			.pipe(
				switchMap(() => {
					this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);

					return this.http.get<Delivery>(`${this.baseUrl}/${deliveryid}/refresh`).pipe(
						catchError(err => {
							console.error(`❌ Erreur polling refresh ${deliveryid}:`, err);
							return of(null);
						})
					);
				}),
				filter(result => result != null),
				takeWhile(delivery => {
					const shouldContinue = this.needsRefreshPolling(delivery);

					if (!shouldContinue) {
						console.log(`✅ Polling refresh terminé pour ${deliveryid}`);
						this.pollingRefreshDeliveries.delete(deliveryid);
					}

					return shouldContinue;
				}, true),
				retry({count: 3, delay: 5000}),
				takeUntilDestroyed(this.destroyRef)
			)
			.subscribe({
				next: (delivery) => {
					this.updateDelivery(delivery);
				},
				error: (err) => {
					console.error(`❌ Erreur finale polling refresh ${deliveryid}:`, err);
					this.pollingRefreshDeliveries.delete(deliveryid);
				},
				complete: () => {
					this.pollingRefreshDeliveries.delete(deliveryid);
				}
			});
	}

	private stopAllPolling(): void {
		this.pollingDeliveries.clear();
		this.pollingRefreshDeliveries.clear();
	}

	private stopPollingForDelivery(deliveryid: number): void {
		if (this.pollingDeliveries.has(deliveryid)) {
			this.pollingDeliveries.delete(deliveryid);
		}
	}

	private stopPollingRefreshForDelivery(deliveryid: number): void {
		if (this.pollingRefreshDeliveries.has(deliveryid)) {
			this.pollingRefreshDeliveries.delete(deliveryid);
		}
	}
}
