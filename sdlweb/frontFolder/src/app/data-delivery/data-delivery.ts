import {
	Component,
	computed,
	effect,
	inject,
	OnInit,
	signal,
	Signal,
	untracked,
} from '@angular/core';
import {DatahandlerConfig, DoubleClickEvent} from "../shared/datahandler/datahandler";
import {CodeGroupStore} from "../services/CodeGroupStore";
import {LocalizedCode} from "../model/LocalizedCode";
import {LanguageService} from "../services/language";
import {UploadFileService} from "../services/upload-file";

import tableConfigs from '../../assets/tables-datadelivery-config.json';
import {finalize} from "rxjs";
import {Delivery, Export, Intervention, MEB_DELIVERYSTATUS, MEB_INTERVENTIONTYPE, MEB_PLAUSISTATUS, WebFilter} from "../model/Delivery";
import {UserService} from "../services/user";
import {DeliveryService} from "../services/deliveries/delivery";
import {ActionTypeForDelivery, isActionDisabledForDelivery} from "../services/deliveries/delivery.utils";
import {InterventionService} from "../services/deliveries/intervention";
import {ActionTypeForLivraisons, isActionDisabledForIntervention} from "../services/deliveries/intervention.utils";
import {ExportationService} from "../services/deliveries/exportation";
import {ObNotificationService} from "@oblique/oblique";
import {HelpLink} from "../shared/aide-documentation/aide-documentation";
import {ContextFilterConfig, PredefinedFilter, WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {updateWebFilterWithPredefinedFilter} from "../core/utils/filters.util";
import {DialogService} from "../services/dialog";

@Component({
	selector: 'app-data-delivery',
	standalone: false,
	templateUrl: './data-delivery.html',
	styleUrl: './data-delivery.scss',
})
export class DataDelivery implements OnInit {
	protected deliveryService = inject(DeliveryService);
	protected interventionService = inject(InterventionService);
	protected exportationService = inject(ExportationService);
	private codeGroupStore = inject(CodeGroupStore);
	private reloadService = inject(LanguageService);
	private uploadFileService = inject(UploadFileService);
	private userService = inject(UserService);
	private readonly obNotificationService = inject(ObNotificationService);
	private readonly dialogService = inject(DialogService);

	uploading = signal(false);
	uploadMessage = signal<string | null>(null);
	private inSync = signal(true);
	private isCurrentUpdateForIntervention = signal(false);

	// Signals pour contrôler le clear selection du datahandler (Intervention uniquement)
	clearInterventionsSelection = signal(0);

	// Signal pour contrôler le clear selection du datahandler des livraisons
	clearDeliverySelection = signal(0);

	cantonOptions = this.mapCodeGroupToOptions('CANTON', (item) => item.key != null ? `${item.value} (${item.key})` : item.value);
	private deliveryStatusOptions = this.mapCodeGroupToOptions('MEB_DELIVERYSTATUS');
	private plausiStatusOptions = this.mapCodeGroupToOptions('MEB_PLAUSISTATUS');
	private interventionStatusOptions = this.mapCodeGroupToOptions('MEB_INTERVENTIONTYPE');

	private actionByIconForDelivery: Record<string, ActionTypeForDelivery> = {
		file_download: 'export',
		note_add: 'amend',
		autorenew: 'replace',
		check: 'confirm',
		reply: 'cancel',
		done_all: 'prevalidate',
		remove_done: 'undo_validate',
		load_plausireport: 'create_plausi_report',
		create_plausireport: 'show_plausi_report',
		save: 'save',
		delete_forever: 'delete',
	} as const;

	private actionByIconForIntervention: Record<string, ActionTypeForLivraisons> = {
		file_download: 'export',
		create_plausireport: 'show_plausi_report',
		load_deliveryfile: 'show_delivery',
		save: 'save',
		block: 'block',
		add: 'add',
		delete: 'delete',
	} as const;

	private readonly optionsRegistry = {
		cantonOptions: this.cantonOptions,
		deliveryStatusOptions: this.deliveryStatusOptions,
		plausiStatusOptions: this.plausiStatusOptions,
		interventionStatusOptions: this.interventionStatusOptions,
	} as const;

	livraisonsConfig = computed(() => {
		const baseConfig = tableConfigs.livraisons as DatahandlerConfig;
		const config = structuredClone(baseConfig);

		config.columns
			.filter((col) => col.optionsSource && col.optionsSource in this.optionsRegistry)
			.forEach(
				(col) =>
					(col.options =
						this.optionsRegistry[col.optionsSource as keyof typeof this.optionsRegistry]()),
			);

		// Mise à jour des boutons selon la sélection
		const livraison = this.deliveryService.currentDelivery();
		const userRoles = this.userService.roles();

		// Déterminer le message de suppression
		const deleteMessage =
			livraison?.deliveryStatus === MEB_DELIVERYSTATUS.INITIALIZED
				? 'confirm.delete.permanent.delivery.message'
				: 'confirm.delete.delivery.message'; // confirm.undo.prevalidate.message

		const undoValidateMessage =
			livraison?.deliveryStatus === MEB_DELIVERYSTATUS.PREVALIDATED
				? 'confirm.undo.prevalidate.message'
				: 'confirm.undo.validate.message';

		const actionHandlers: Record<ActionTypeForDelivery, () => void> = {
			export: () => this.deliveryService.exportDelivery(),
			amend: () => this.deliveryService.amendDelivery(),
			replace: () => this.deliveryService.replaceDelivery(),
			confirm: () => this.deliveryService.confirmDelivery(),
			cancel: () => this.deliveryService.cancelDelivery(),
			prevalidate: () =>
				this.dialogService.confirmAndDo('confirm.validate.message', () =>
					this.deliveryService.prevalidateDelivery(),
				),
			undo_validate: () =>
				this.dialogService.confirmAndDo(undoValidateMessage, () =>
					this.deliveryService.undoValidate(),
				),
			create_plausi_report: () => this.deliveryService.createPlausiReport(),
			show_plausi_report: () => this.deliveryService.showPlausiReportDelivery(),
			save: () => this.deliveryService.saveDelivery(),
			delete: () =>
				this.dialogService.confirmAndDo(deleteMessage, () => this.deliveryService.deleteDelivery()),
		};

		config.actions.forEach((action) => {
			// Déterminer si les boutons doit être disabled
			action.disabled = isActionDisabledForDelivery(
				this.actionByIconForDelivery[action.icon],
				livraison,
				userRoles,
				this.inSync(),
			);
			// Injecter les actions handler
			action.handler = actionHandlers[this.actionByIconForDelivery[action.icon]];
		});

		return config;
	});

	historiesConfig = computed(() => {
		const baseConfig = tableConfigs.histories as DatahandlerConfig;
		const config = structuredClone(baseConfig);

		config.columns
			.filter((col) => col.optionsSource && col.optionsSource in this.optionsRegistry)
			.forEach(
				(col) =>
					(col.options =
						this.optionsRegistry[col.optionsSource as keyof typeof this.optionsRegistry]()),
			);

		// Rendre editable la colonne 'report' si l'intervention est de type MANUAL'
		config.columns
			.filter((col) => col.key === 'report')
			.forEach((col) => {
				col.editableOnCondition = (row: Intervention, rowIndex) => {
					return row.type === MEB_INTERVENTIONTYPE.MANUAL;
				};
			});

		// Mise à jour des boutons selon la sélection
		const intervention = this.interventionService.currentIntervention();
		const userRoles = this.userService.roles();

		const actionHandlers: Record<ActionTypeForLivraisons, () => void> = {
			export: () => this.interventionService.exportIntervention(),
			show_plausi_report: () => this.interventionService.showPlausiReportIntervention(),
			show_delivery: () => this.interventionService.showDeliveryReportIntervention(),
			save: () => this.interventionService.saveIntervention(),
			block: () => this.interventionService.undoIntervention(),
			add: () => {},
			delete: () => {},
		};

		config.actions.forEach((action) => {
			// Déterminer si les boutons doit être disabled
			action.disabled = isActionDisabledForIntervention(
				this.actionByIconForIntervention[action.icon],
				intervention,
				userRoles,
				!this.isCurrentUpdateForIntervention(),
			);
			// Injecter les actions handler
			action.handler = actionHandlers[this.actionByIconForIntervention[action.icon]];
		});

		return config;
	});

	exportationsConfig: DatahandlerConfig = {
		...tableConfigs.exportations,
		actions: tableConfigs.exportations.actions?.map((action) => ({
			...action,
			handler: this.getExportActionHandler(action.icon),
		})),
	} as DatahandlerConfig;

	contextFiltersConfig = computed(() => {
		const baseConfig = tableConfigs.livraisons.columns as ContextFilterConfig[];
		const config = structuredClone(baseConfig);

		config
			.filter((col) => col.optionsSource && col.optionsSource in this.optionsRegistry)
			.forEach(
				(col) =>
					(col.options =
						this.optionsRegistry[col.optionsSource as keyof typeof this.optionsRegistry]()),
			);

		return config.filter((col) => !!col.filterableOnContext);
	});

	helpLinks: HelpLink[] = [
		{
			label: 'deliver.download.quickguide.text',
			url: 'deliver.download.quickguide.href',
		} as HelpLink,
		{
			label: 'deliver.download.techManual.name',
			url: 'deliver.download.techManual.href',
		} as HelpLink,
		{
			label: 'deliver.download.selfadmin.name',
			url: 'deliver.download.selfadmin.href',
		} as HelpLink,
	];

	constructor() {
		// Effect 1: Selectionne la 1ère livraison lors d'un chargement initial ou quand la livraison courante n'est plus dans la liste
		effect(() => {
			const deliveriesData = this.deliveryService.deliveries();
			if (deliveriesData?.length > 0) {
				const currentDelivery = untracked(() => this.deliveryService.currentDelivery());
				if (
					!currentDelivery ||
					!deliveriesData.some((d) => d.deliveryId === currentDelivery.deliveryId)
				) {
					this.deliveryService.selectDelivery(deliveriesData[0]);
				}
			}
		});

		// Effect 2: Recharge les code groups quand la langue change
		effect(() => {
			this.reloadService.currentLanguage();
			this.loadAllCodeGroups();
		});

		// Effect 3: Charge les livraisons si pas de filtres
		effect(() => {
			const filters = this.deliveryService.lastFilters();
			if (!filters || (filters.canton === undefined && filters.version === undefined)) {
				this.deliveryService.loadDeliveries();
			}
		});

		// Effect 4: Notifie le message d'upload
		effect(() => {
			const message = this.uploadMessage();
			if (message) {
				this.dialogService.confirmAndDo(message, () => {
					this.deliveryService.reloadCurrentDeliveries();
					this.uploadMessage.set(null);
				});
			}
		});

		// Effect 5: Notifie les messages d'erreurs
		effect(() => {
			const message = this.deliveryService.opError();
			if (message) {
				this.obNotificationService.error({
					channel: 'oblique',
					message,
				});
			}
		});

		// Effect 6: Notifie les messages de warning
		effect(() => {
			const message = this.deliveryService.opWarning();
			if (message) {
				this.obNotificationService.warning({
					channel: 'oblique',
					message,
				});
			}
		});
	}

	ngOnInit(): void {
		// Chargement initial des code groups
		this.loadAllCodeGroups();
	}

	private loadAllCodeGroups(): void {
		this.codeGroupStore.loadValues('CANTON');
		this.codeGroupStore.loadValues('MEB_DELIVERYSTATUS');
		this.codeGroupStore.loadValues('MEB_PLAUSISTATUS');
		this.codeGroupStore.loadValues('MEB_INTERVENTIONTYPE');
	}

	filterChange(filters: {
		versionFilter: number;
		cantonFilter: number;
		contextFilters: WhereFilter[];
		selectedPredefinedFilters: PredefinedFilter[];
	}): void {
		let webFilters: WebFilter[] = [];
		if (filters.selectedPredefinedFilters) {
			webFilters = this.mapPredefinedFiltersToWebFilters(filters.selectedPredefinedFilters);
		}
		this.clearDeliverySelection.set(Date.now());
		this.deliveryService.loadDeliveries(
			filters.versionFilter,
			filters.cantonFilter,
			webFilters,
			filters.contextFilters,
		);
	}

	uploadFile(file: File): void {
		this.uploadFileService.uploadFile(file, 'upload.do', 'delivery').subscribe({
			next: (message) => {
				console.log('Upload réussi:', message);
				this.uploadMessage.set(message);
				// recharger la liste des livraisons ?
				// this.deliveryService.refreshDeliveries();
			},
			error: (error) => {
				console.error('Erreur upload:', error);
				this.uploadMessage.set(error.message);
			},
		});
	}

	private mapCodeGroupToOptions(
		codeGroup: string,
		labelFormatter?: (item: LocalizedCode) => string,
	): Signal<{ label: string; value: any }[]> {
		const defaultFormatter = (item: LocalizedCode) => item.value;
		const formatter = labelFormatter || defaultFormatter;

		return computed(() => {
			const values = this.codeGroupStore.getValuesSignal(codeGroup)();
			return values.map((item) => ({
				label: formatter(item),
				value: item.key,
			}));
		});
	}

	selectDelivery(delivery: Delivery | null): void {
		this.deliveryService.selectDelivery(delivery);
		this.clearInterventionsSelection.set(Date.now());
	}

	selectIntervention(intervention: Intervention | null): void {
		if (intervention) {
			this.interventionService.selectIntervention(intervention);
		}
	}

	selectExportation(exportation: Export | null): void {
		this.exportationService.selectExportation(exportation);
	}

	private getExportActionHandler(icon: string): () => void {
		const handlers: Record<string, () => void> = {
			file_download: () => this.exportationService.downloadExport(),
			open_in_new: () => this.exportationService.launchExploitation(),
		};
		return handlers[icon];
	}

	private mapPredefinedFiltersToWebFilters(
		selectedPredefinedList: PredefinedFilter[],
	): WebFilter[] {
		return selectedPredefinedList
			.map((predefinedFilter) => {
				// Trouver le WebFilter complet chargé correspondant
				const loadedFilter = this.deliveryService
					.webFilters()
					.find((f) => f.filterId === predefinedFilter.id);
				return updateWebFilterWithPredefinedFilter(loadedFilter, predefinedFilter);
			})
			.filter((f) => f !== null) as WebFilter[];
	}

	handleTableEventforIntervention(event: {
		action: string;
		data?: any[];
		errorType?: string;
		invalidRows?: any[];
	}): void {
		if (!event.data) {
			return;
		}

		const itemsToDelete = event.data.filter((item) => item.isDeleted && !item.isNew);
		const itemsToCreate = event.data.filter((item) => item.isNew && !item.isDeleted);
		const hasChanges = itemsToDelete.length > 0 || itemsToCreate.length > 0;

		this.isCurrentUpdateForIntervention.set(hasChanges);

		if (event.action === 'save') {
			if (itemsToDelete.length > 0) {
				this.interventionService.deleteInterventions(itemsToDelete as Intervention[]);
			}
			if (itemsToCreate.length > 0) {
				this.interventionService.insertInterventions(itemsToCreate as Intervention[]);
			}
		}
	}
}
