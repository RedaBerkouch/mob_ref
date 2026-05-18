import {computed, DestroyRef, inject, Injectable, signal, Signal} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {catchError, concat, filter, finalize, map, merge, Observable, of, Subject, switchMap, tap, toArray} from 'rxjs';
import {takeUntilDestroyed, toObservable, toSignal} from "@angular/core/rxjs-interop";
import {Export, Intervention} from "../../model/Delivery";
import {LanguageService} from "../language";
import {getTranslation} from "../../core/utils/translation.util";
import {DeliveryService} from "./delivery";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";

@Injectable({
	providedIn: 'root'
})
export class InterventionService {
	private readonly baseUrl = '/sbaweb/api/deliveries/interventions';
	private http = inject(HttpClient);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);
	private deliveryService = inject(DeliveryService);
	private reloadService = inject(LanguageService);
	private destroyRef = inject(DestroyRef); // ✅ AJOUTÉ pour les fixes
	readonly currentIntervention = signal<Intervention | null>(null);
	private reloadTrigger$ = new Subject<void>(); // déclenche un reload
	readonly isLoading = signal<boolean>(false);

	private readonly interventionData$: Observable<Intervention[]> = merge(
		toObservable(this.deliveryService.currentDelivery),
		this.reloadTrigger$
	).pipe(
		switchMap(() => {
			const delivery = this.deliveryService.currentDelivery();
			if (!delivery) {
				return of([]);
			}
			this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1); // Désactiver spinner et notification pour la prochaine requête
			this.isLoading.set(true);
			return this.http.get<Intervention[]>(`${this.baseUrl}/${delivery.deliveryId}`);
		}),
		tap(() => this.isLoading.set(false)),
		filter(value => !!value)
	);

	private readonly interventionsData: Signal<Intervention[]> = toSignal(this.interventionData$, {initialValue: []});

	readonly interventions: Signal<Export[]> = computed(() => {
		const lang = this.reloadService.currentLanguage();
		const data = this.interventionsData();

		return data.map(intervention => ({
			...intervention,
			report: getTranslation(intervention, 'report', lang)
		}));
	});

	constructor() {
		this.destroyRef.onDestroy(() => {
			this.reloadTrigger$.complete();
		});
	}

	selectIntervention(intervention: Intervention) {
		this.currentIntervention.set(intervention);
	}

	// Handlers des actions

	/**
	 * Exporte les livraisons en CSV et ouvre le fichier dans un nouvel onglet
	 */
	exportIntervention(): void {
		const currentDelivery = this.deliveryService.currentDelivery();

		this.http.get(`${this.baseUrl}/${currentDelivery?.deliveryId}/export/csv`, {
			responseType: 'blob',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Export completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'interventions_export.csv');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export CSV', error);
			}
		});
	}

	showPlausiReportIntervention() {
		const interventionId = this.currentIntervention()?.interventionId;
		if (!interventionId) return;

		this.http.get(`${this.baseUrl}/${interventionId}/plausi_report`, {
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

	showDeliveryReportIntervention() {
		const interventionId = this.currentIntervention()?.interventionId;
		if (!interventionId) return;

		this.http.get(`${this.baseUrl}/${interventionId}/delivery_report`, {
			responseType: 'blob',
			observe: 'response'
		}).pipe(
			finalize(() => console.log('Delivery Report completed')),
			takeUntilDestroyed(this.destroyRef)
		).subscribe({
			next: (response: HttpResponse<Blob>) => {
				openCsvFile(response, 'DeliveryFile.zip');
			},
			error: (error) => {
				console.error('Erreur lors de l\'export du delivery', error);
			}
		});
	}

	saveIntervention() {
		const interventionId = this.currentIntervention()?.interventionId;
		if (!interventionId) return;
		console.log('saveIntervention:', interventionId);
	}

	undoIntervention() {
		const interventionId = this.currentIntervention()?.interventionId;
		if (!interventionId) return;
		console.log('undoIntervention:', interventionId);
	}

	insertInterventions(interventions: Intervention[]): void {
		const currentDelivery = this.deliveryService.currentDelivery();

		if (!currentDelivery?.deliveryId) {
			return;
		}

		const body = interventions.map(value => ({
			deliveryId: currentDelivery.deliveryId,
			type: value.type,
			reportDe: value.report,
			reportFr: value.report,
			reportIt: value.report,
		} as Intervention));

		// Créer un tableau d'observables
		const requests$ = body.map(intervention =>
			this.http.post<Intervention>(`${this.baseUrl}`, intervention)
				.pipe(
					catchError(error => {
						console.error('Erreur lors de l\'insertion d\'une intervention:', error);
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
				console.log(`${successCount}/${interventions.length} interventions insérées`);
				// Recharger les interventions
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur lors de l\'insertion:', error);
			}
		});
	}

	deleteInterventions(interventions: Intervention[]): void {
		if (!interventions || interventions.length === 0) {
			console.warn('Aucune intervention à supprimer');
			return;
		}

		// Créer un tableau d'observables (un DELETE par intervention)
		const requests$ = interventions.map(intervention =>
			this.http.delete<void>(`${this.baseUrl}/${intervention.interventionId}`)
				.pipe(
					catchError(error => {
						console.error(`Erreur lors de la suppression de l'intervention ${intervention.interventionId}:`, error);
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
				console.log(`${successCount}/${interventions.length} interventions supprimées`);
				// Recharger les interventions
				this.reloadTrigger$.next();
			},
			error: (error) => {
				console.error('Erreur globale:', error);
			}
		});
	}
}
