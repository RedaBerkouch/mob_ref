import {computed, DestroyRef, inject, Injectable, signal, Signal} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {filter, finalize, Observable} from 'rxjs';
import {takeUntilDestroyed, toSignal} from "@angular/core/rxjs-interop";
import {Export, Parameter} from "../../model/Delivery";
import {LanguageService} from "../language";
import {getTranslation} from "../../core/utils/translation.util";
import {openCsvFile} from "../../core/utils/exportCsv.util";
import {DialogService} from "../dialog";

@Injectable({
	providedIn: 'root'
})
export class ExportationService {
	private readonly baseUrl = '/sbaweb/api/deliveries/exportations';
	private languageService = inject(LanguageService);
	private dialogService = inject(DialogService);
	private http = inject(HttpClient);
	private destroyRef = inject(DestroyRef);
	readonly currentExport = signal<Export | null>(null);
	opError = signal<string | null>(null);

	selectExportation(exportation: Export | null) {
		this.currentExport.set(exportation);
	}

	private readonly exportationsData$: Observable<Export[]> = this.http.get<Export[]>(`${this.baseUrl}`).pipe(
		filter(value => !!value),
	);

	private readonly exportationsData: Signal<Export[]> = toSignal(this.exportationsData$, {initialValue: []});

	readonly exportations: Signal<Export[]> = computed(() => {
		const lang = this.languageService.currentLanguage();
		const data = this.exportationsData();

		return data.map(exportItem => ({
			...exportItem,
			name: getTranslation(exportItem, 'name', lang),
			description: getTranslation(exportItem, 'description', lang),
			parameter: exportItem.parameters
				?.map(param => {
					const name = getTranslation(param, 'name', lang);
					return `${name}=${param.defaultValue??''}`;
				})
				.join(';')
		}));
	});

	// Handlers des actions

	downloadExport() {
		this.http.get(`${this.baseUrl}/export/csv`, {
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

	launchExploitation() {
		const currentExport = this.currentExport();
		this.opError.set(null);
		if (currentExport) {
			const lang = this.languageService.currentLanguage();
			// Transformer parameter (string) -> parameters (array)
			const payload = this.prepareExportPayload(currentExport, lang);
			this.http.post(this.baseUrl, payload, {
				responseType: 'blob',
				observe: 'response'
			}).pipe(
				finalize(() => console.log('launch exploitation completed')),
				takeUntilDestroyed(this.destroyRef)
			).subscribe({
				next: (response: HttpResponse<Blob>) => {
					openCsvFile(response, 'Export-xml.zip');
				},
				error: (error) => {
					this.opError.set(error.error || 'Erreur lors du lancement de l\'exploitation');
				}
			});
		} else {
			this.dialogService.showMessage('no.export.selected.message');
		}
	}

	/**
	 * Prépare le payload pour l'envoi au backend
	 * Convertit parameter (string) -> parameters (array) avec les valeurs mises à jour
	 *
	 * @param exportItem - Export avec parameter sous forme de string
	 * @param lang - Langue courante
	 * @returns Export prêt pour le backend
	 */
	private prepareExportPayload(exportItem: Export, lang: string): Export {
		const payload = structuredClone(exportItem);

		// Si parameter existe, le parser et reconstruire parameters
		if (exportItem.parameter && exportItem.parameters) {
			payload.parameters = this.parseParameterString(
				exportItem.parameter,
				exportItem.parameters,
				lang
			);
		} else if (exportItem.parameters) {
			// Si pas de parameter string, garder parameters tel quel
			payload.parameters = exportItem.parameters;
		}

		return payload;
	}

	/**
	 * Parse la chaîne parameter et met à jour les valeurs dans parameters
	 *
	 * Format attendu: "Nom Param1=valeur1;Nom Param2=valeur2"
	 *
	 * @param parameterString - Chaîne de paramètres modifiés
	 * @param originalParameters - Paramètres originaux avec toutes les traductions
	 * @param lang - Langue courante
	 * @returns Array de paramètres avec valeurs mises à jour
	 */
	private parseParameterString(
		parameterString: string,
		originalParameters: Parameter[],
		lang: string
	): Parameter[] {
		// Parser la chaîne: "name1=value1;name2=value2"
		const paramMap = new Map<string, string>();

		parameterString.split(';').forEach(pair => {
			const [name, value] = pair.split('=').map(s => s.trim());
			if (name && value !== undefined) {
				paramMap.set(name, value);
			}
		});

		// Mettre à jour les paramètres originaux avec les nouvelles valeurs
		return originalParameters.map(param => {
			// Récupérer le nom traduit dans la langue courante
			const translatedName = getTranslation(param, 'name', lang);

			// Si on trouve une valeur pour ce nom, la mettre à jour
			const newValue = paramMap.get(translatedName);

			return {
				...param,
				defaultValue: newValue !== undefined ? newValue : param.defaultValue
			};
		});
	}
}
