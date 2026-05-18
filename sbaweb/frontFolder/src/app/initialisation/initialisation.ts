import {
	Component,
	DestroyRef,
	OnInit,
	WritableSignal,
	computed,
	effect,
	inject,
	signal,
} from '@angular/core';
import { CantonService, InitVersionResponse } from '../services/initialisation/canton';
import { CantonInterventionService } from '../services/initialisation/cantonIntervention';
import { ConfigDeliveryService } from '../services/initialisation/configDelivery';
import { SchoolService } from '../services/initialisation/school';
import {
	ActionTypeForCanton,
	enrichDefaultValuesForCanton,
	enrichEditableOnConditionForCanton,
} from '../services/initialisation/canton.utils';
import {
	ActionTypeForCantonIntervention,
	enrichDefaultValuesForCantonIntervention,
} from '../services/initialisation/cantonIntervention.utils';
import {
	ActionTypeForConfigDelivery,
	enrichDefaultValuesForConfigDelivery,
} from '../services/initialisation/configDelivery.utils';
import {
	ActionTypeForSchool,
	enrichEditableColumnsForSchool,
} from '../services/initialisation/school.utils';
import { ObNotificationService } from '@oblique/oblique';
import { CodeGroupStore } from '../services/CodeGroupStore';
import { UploadFileService } from '../services/upload-file';
import { UserService } from '../services/user';
import tableConfigs from '../../assets/tables-initialisation-config.json';
import { Canton, MEB_CANTONSTATUS } from '../model/Canton';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CantonIntervention, SBA_CANTONINTERVENTIONTYPE } from '../model/CantonIntervention';
import { School } from '../model/School';
import { ConfigDelivery } from '../model/ConfigDelivery';
import {
	DatahandlerColumn,
	DatahandlerConfig,
	TableEvent,
} from '../shared/datahandler/datahandler';
import { LanguageService } from '../services/language';
import { VersionCantonFilterType } from '../shared/version-canton-filter/version-canton-filter';
import {
	ContextFilterConfig,
	PredefinedFilter,
} from '../shared/pre-advance-filter/pre-advance-filter';
import { WebFilter, updateWebFilterWithPredefinedFilter } from '../core/utils/filters.util';
import { DialogService } from '../services/dialog';

@Component({
	selector: 'app-initialisation',
	standalone: false,
	templateUrl: './initialisation.html',
	styleUrl: './initialisation.scss',
})
export class Initialisation implements OnInit {
	// Services des entités
	protected cantonService = inject(CantonService);
	protected cantonInterventionService = inject(CantonInterventionService);
	protected configDeliveryService = inject(ConfigDeliveryService);
	protected schoolService = inject(SchoolService);

	// Services "utils"
	private readonly uploadFileService = inject(UploadFileService);
	private readonly userService = inject(UserService);
	private readonly languageService = inject(LanguageService);
	private readonly dialogService = inject(DialogService);
	private readonly destroyRef = inject(DestroyRef);

	// Service Oblique
	private readonly obNotificationService = inject(ObNotificationService);

	// Store
	private readonly codeGroupStore = inject(CodeGroupStore);

	// Signaux techniques
	uploading = signal(false);
	uploadMessage = signal<string | null>(null);

	private readonly currentFilters = signal<VersionCantonFilterType>({} as VersionCantonFilterType);

	// Canton sélectionné dans le filtre (mis à jour immédiatement à la sélection, avant d'"Appliquer")
	private readonly uploadCanton = signal<number | null>(null);

	// -------------------------------------------------------------------
	// Options CodeGroups – style aligné avec DataMaintain
	// -------------------------------------------------------------------
	private readonly cantonOptionsBase = this.codeGroupStore.optionsFor(
		'CANTON',
		(item) => item.key != null ? `${item.value} (${item.key})` : item.value,
	);

	readonly cantonOptions = computed(() =>
		[...this.cantonOptionsBase()].sort((a, b) => Number(a.value) - Number(b.value)),
	);

	readonly missingCantonOptions = computed(() => {
		const missingCantons = this.cantonService.missingCantons();

		return this.cantonOptionsBase()
			.filter((item) => missingCantons.includes(item.value))
			.sort((a, b) => Number(a.value) - Number(b.value));
	});

	private readonly deliveryStatusOptions = this.codeGroupStore.optionsFor('MEB_DELIVERYSTATUS');

	private readonly plausiStatusOptions = this.codeGroupStore.optionsFor('MEB_PLAUSISTATUS');

	private readonly cantonInterventionOptions = this.codeGroupStore.optionsFor(
		'SBA_CANTONINTTYPE',
		(item) => `${item.value} (${item.key})`,
	);

	private readonly municipalityOptions = this.codeGroupStore.optionsFor(
		'MUNICIPALITY',
		(item) => `${item.value} (${item.key})`,
	);

	private readonly synchStatusOptions = this.codeGroupStore.optionsFor('MEB_SYNCHSTATUS');

	// Signaux pour indiquer si la table des cantons contient des modifications
	containsNewCanton = signal(false);
	containsDeletedCanton = signal(false);
	containsUpdatedCanton = signal(false);
	containsModificationForCantons = computed(
		() => this.containsNewCanton() || this.containsDeletedCanton() || this.containsUpdatedCanton(),
	);

	// Signaux pour indiquer si la table des interventions sur les cantons contient des modifications
	containsNewCantonIntervention = signal(false);
	containsDeletedCantonIntervention = signal(false);
	containsUpdatedCantonIntervention = signal(false);
	containsModificationForCantonIntervention = computed(
		() =>
			this.containsNewCantonIntervention() ||
			this.containsDeletedCantonIntervention() ||
			this.containsUpdatedCantonIntervention(),
	);

	// Signaux pour indiquer si la table des configurations de livraison contient des modifications
	containsNewConfigDelivery = signal(false);
	containsDeletedConfigDelivery = signal(false);
	containsUpdatedConfigDelivery = signal(false);
	containsModificationForConfigDelivery = computed(
		() =>
			this.containsNewConfigDelivery() ||
			this.containsDeletedConfigDelivery() ||
			this.containsUpdatedConfigDelivery(),
	);

	// Signaux pour indiquer si la table des écoles contient des modifications
	containsNewSchool = signal(false);
	containsDeletedSchool = signal(false);
	containsUpdatedSchool = signal(false);
	containsModificationForSchool = computed(
		() => this.containsNewSchool() || this.containsDeletedSchool() || this.containsUpdatedSchool(),
	);

	// Signaux pour contrôler le clear des sélections
	clearConfigDeliverySelection = signal(0);
	clearSchoolSelection = signal(0);

	// Registre des options utilisé sur cet onglet
	private readonly optionsRegistry = {
		cantonOptions: this.cantonOptions,
		missingCantonOptions: this.missingCantonOptions,
		deliveryStatusOptions: this.deliveryStatusOptions,
		plausiStatusOptions: this.plausiStatusOptions,
		cantonInterventionOptions: this.cantonInterventionOptions,
		municipalityOptions: this.municipalityOptions,
		synchStatusOptions: this.synchStatusOptions,
	} as const;

	// Icônes des actions des différentes configurations
	private readonly actionByIconForCanton: Record<string, ActionTypeForCanton> = {
		file_download: 'export_csv',
		lock: 'finalize',
		lock_open_right: 'undo_finalize',
		done_all: 'validate',
		remove_done: 'undo_validate',
		create_plausireport: 'create_plausi_report',
		load_plausireport: 'show_last_plausi_report',
		save: 'save',
		block: 'undo',
		add: 'insert',
		delete: 'delete',
	} as const;

	private readonly actionByIconForCantonIntervention: Record<
		string,
		ActionTypeForCantonIntervention
	> = {
		load_deliveryfile: 'download_file',
		file_download: 'export_csv',
		load_plausireport: 'show_plausi_report',
		save: 'save',
		block: 'undo',
		add: 'insert',
		delete: 'delete',
	} as const;

	private readonly actionByIconForConfigDelievery: Record<string, ActionTypeForConfigDelivery> = {
		file_download: 'export_csv',
		switch_master: 'switch_master',
		save: 'save',
		block: 'undo',
		add: 'new',
		delete: 'delete',
	} as const;

	private readonly actionByIconForSchool: Record<string, ActionTypeForSchool> = {
		file_download: 'export_csv',
		switch_master: 'switch_master',
		autorenew: 'autorenew',
		done_all: 'done_all',
		check: 'check',
		save: 'save',
		block: 'undo',
	} as const;

	// Configurations des tables
	cantonConfig = computed(() => {
		const baseConfig = tableConfigs.canton as DatahandlerConfig;

		const enrichedWithDefaultValuesColumns = enrichDefaultValuesForCanton(baseConfig.columns);
		const enrichedWithOptions = this.enrichColumnsWithOptions(enrichedWithDefaultValuesColumns);
		const enrichEditableColumns = enrichEditableOnConditionForCanton(enrichedWithOptions);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichEditableColumns ?? baseConfig.columns,
			actions: [...baseConfig.actions],
		};

		const currentCanton = this.cantonService.currentCanton();
		const isEV = this.userService.isEV();
		const missingCantons = this.cantonService.missingCantons();
		const hasSelected: boolean = currentCanton != null;

		const disableHandlers: Record<ActionTypeForCanton, () => boolean> = {
			export_csv: () => false,
			finalize: () =>
				!hasSelected ||
				(currentCanton?.deliveryStatus != null &&
					currentCanton.deliveryStatus !== MEB_CANTONSTATUS.VALIDATED),
			undo_finalize: () =>
				!hasSelected ||
				(currentCanton?.deliveryStatus != null &&
					currentCanton.deliveryStatus !== MEB_CANTONSTATUS.FINALIZED),
			validate: () =>
				!hasSelected ||
				(currentCanton?.deliveryStatus != null &&
					currentCanton.deliveryStatus !== MEB_CANTONSTATUS.DELIVERED),
			undo_validate: () =>
				!hasSelected ||
				!isEV ||
				(currentCanton?.deliveryStatus != null &&
					currentCanton.deliveryStatus !== MEB_CANTONSTATUS.VALIDATED),
			create_plausi_report: () => !hasSelected,
			show_last_plausi_report: () =>
				!hasSelected || !currentCanton?.plausiUser || currentCanton.plausiUser.length === 0,
			save: () => !this.containsModificationForCantons(),
			undo: () => !this.containsModificationForCantons(),
			insert: () => missingCantons.length === 0,
			delete: () => !hasSelected,
		};

		const actionHandlers: Record<ActionTypeForCanton, () => void> = {
			export_csv: () => this.cantonService.exportCSV(),
			finalize: () =>
				this.dialogService.confirmAndDo('confirm.finalize.message', () =>
					this.cantonService.finalise(),
				),
			undo_finalize: () =>
				this.dialogService.confirmAndDo('confirm.undo.finalize.message', () =>
					this.cantonService.undoFinalise(),
				),
			validate: () =>
				this.dialogService.confirmAndDo('confirm.validate.message', () =>
					this.cantonService.validate(),
				),
			undo_validate: () =>
				this.dialogService.confirmAndDo('confirm.undo.validate.message', () =>
					this.cantonService.undoValidate(),
				),
			create_plausi_report: () =>
				this.dialogService.confirmAndDo('plausireport.canton.confirm.message', () =>
					this.cantonService.createPlausiReport(),
				),
			show_last_plausi_report: () => this.cantonService.showPlausiReport(),
			save: () => {},
			undo: () => {},
			insert: () => {},
			delete: () => {},
		};

		config.actions.forEach((action) => {
			action.disabledFn = disableHandlers[this.actionByIconForCanton[action.icon]];
			action.handler = actionHandlers[this.actionByIconForCanton[action.icon]];
		});

		return config;
	});

	cantonInterventionConfig = computed(() => {
		const baseConfig = tableConfigs.cantonIntervention as DatahandlerConfig;

		const enrichedWithOptions = this.enrichColumnsWithOptions(baseConfig.columns);
		const enrichedWithDefaultValuesColumns =
			enrichDefaultValuesForCantonIntervention(enrichedWithOptions);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichedWithDefaultValuesColumns ?? baseConfig.columns,
			actions: [...baseConfig.actions],
		};

		const currentCantonIntervention = this.cantonInterventionService.currentCantonIntervention();

		const isDL = this.userService.isDL();
		const hasSelected: boolean = currentCantonIntervention != null;
		const type: number =
			hasSelected && currentCantonIntervention?.type
				? currentCantonIntervention.type
				: Number.MAX_VALUE;

		const disableHandlers: Record<ActionTypeForCantonIntervention, () => boolean> = {
			download_file: () => !hasSelected || type != SBA_CANTONINTERVENTIONTYPE.UPLOAD,
			export_csv: () => false,
			show_plausi_report: () =>
				!hasSelected || type != SBA_CANTONINTERVENTIONTYPE.CREATE_PLAUSIREPORT,
			save: () => !this.containsModificationForCantonIntervention(),
			undo: () => !this.containsModificationForCantonIntervention(),
			insert: () => !isDL,
			delete: () => !hasSelected || (isDL && type < SBA_CANTONINTERVENTIONTYPE.MANUAL),
		};

		const actionHandlers: Record<ActionTypeForCantonIntervention, () => void> = {
			download_file: () => this.cantonInterventionService.downloadFile(),
			export_csv: () => this.cantonInterventionService.exportCSV(),
			show_plausi_report: () => this.cantonInterventionService.showPlausiReport(),
			save: () => {},
			undo: () => {},
			insert: () => {},
			delete: () => {},
		};

		config.actions.forEach((action) => {
			action.disabledFn = disableHandlers[this.actionByIconForCantonIntervention[action.icon]];
			action.handler = actionHandlers[this.actionByIconForCantonIntervention[action.icon]];
		});

		return config;
	});

	configDeliveryConfig = computed(() => {
		const baseConfig = tableConfigs.configDelivery as DatahandlerConfig;

		const enrichedWithOptions = this.enrichColumnsWithOptions(baseConfig.columns);
		const enrichedWithDefaultValuesColumns =
			enrichDefaultValuesForConfigDelivery(enrichedWithOptions);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichedWithDefaultValuesColumns ?? baseConfig.columns,
			actions: [...baseConfig.actions],
		};

		const currentConfigDelivery = this.configDeliveryService.currentConfigDelivery();

		const isEA = this.userService.isEA();
		const isEV = this.userService.isEV();
		const isAllowedUserToAddOrDelete = isEA || isEV;

		const isMaster = this.configDeliveryService.isMaster();
		const hasSelected = currentConfigDelivery.length > 0;

		const disableHandlers: Record<ActionTypeForConfigDelivery, () => boolean> = {
			export_csv: () => false,
			switch_master: () => isMaster,
			save: () => !this.containsModificationForConfigDelivery(),
			undo: () => !this.containsModificationForConfigDelivery(),
			new: () => !isMaster || !isAllowedUserToAddOrDelete,
			delete: () => !hasSelected || !isAllowedUserToAddOrDelete,
			insert: () => false,
		};

		const actionHandlers: Record<ActionTypeForConfigDelivery, () => void> = {
			export_csv: () => this.configDeliveryService.exportCSV(),
			switch_master: () => this.setAsMaster(baseConfig.entityName),
			save: () => {},
			undo: () => {},
			new: () => {},
			delete: () => {},
			insert: () => {},
		};

		config.actions.forEach((action) => {
			action.disabledFn = disableHandlers[this.actionByIconForConfigDelievery[action.icon]];
			action.handler = actionHandlers[this.actionByIconForConfigDelievery[action.icon]];
		});

		return config;
	});

	schoolConfig = computed(() => {
		const baseConfig = tableConfigs.configSchool as DatahandlerConfig;

		const enrichedWithOptions = this.enrichColumnsWithOptions(baseConfig.columns);
		const isConfigDeliveryMaster = this.configDeliveryService.isMaster();
		const enrichedWithEditableColumns = enrichEditableColumnsForSchool(
			enrichedWithOptions,
			isConfigDeliveryMaster,
		);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichedWithEditableColumns ?? baseConfig.columns,
			actions: [...baseConfig.actions],
		};

		const currentSchools = this.schoolService.currentSchools();
		const isEV = this.userService.isEV();
		const isMaster = this.schoolService.isMaster();
		const sync_bur = this.schoolService.issync_bur();
		const hasSelected: boolean = currentSchools.length > 0;

		const disableHandlers: Record<ActionTypeForSchool, () => boolean> = {
			export_csv: () => false,
			switch_master: () => isMaster,
			autorenew: () => !isEV || !isMaster,
			done_all: () => !isEV || !isMaster || !sync_bur,
			check: () => !isEV || !isMaster || !sync_bur || !hasSelected,
			save: () => !this.containsModificationForSchool(),
			undo: () => !this.containsModificationForSchool(),
		};

		const actionHandlers: Record<ActionTypeForSchool, () => void> = {
			export_csv: () => this.schoolService.exportCSV(),
			switch_master: () => this.setAsMaster(baseConfig.entityName),
			autorenew: () => this.schoolService.sync_bur(),
			done_all: () =>
				this.schoolService.getAllBur(
					this.currentFilters().versionFilter ?? new Date().getFullYear(),
				),
			check: () =>
				this.schoolService.getBur(
					this.schoolService.currentSchools(),
					this.currentFilters().versionFilter ?? new Date().getFullYear(),
				),
			save: () => {},
			undo: () => {},
		};

		config.actions.forEach((action) => {
			action.disabledFn = disableHandlers[this.actionByIconForSchool[action.icon]];
			action.handler = actionHandlers[this.actionByIconForSchool[action.icon]];
		});

		return config;
	});

	readonly contextFiltersConfig = computed(() => {
		const baseColumns = this.configDeliveryService.isMaster()
			? tableConfigs.configDelivery.columns
			: tableConfigs.configSchool.columns;

		return this.enrichColumnsWithOptions(baseColumns as DatahandlerColumn[])
			.map((col) => col as ContextFilterConfig)
			.filter((col) => col.filterableOnContext);
	});

	constructor() {
		effect(() => {
			this.languageService.currentLanguage();
			this.loadAllCodeGroups();
		});

		effect(() => {
			const message = this.cantonService.opError();
			if (message) {
				this.obNotificationService.error({
					channel: 'oblique',
					message,
				});
			}
		});

		effect(() => {
			const message = this.cantonService.opWarning();
			if (message) {
				this.obNotificationService.warning({
					channel: 'oblique',
					message,
				});
			}
		});

		effect(() => {
			const message = this.schoolService.opError();
			if (message) {
				this.obNotificationService.error({
					channel: 'oblique',
					message,
				});
			}
		});

		effect(() => {
			const message = this.schoolService.opWarning();
			if (message) {
				this.obNotificationService.warning({
					channel: 'oblique',
					message,
				});
			}
		});

		effect(() => {
			const hasPendingDeletions = this.schoolService.schools().some((s) => s.isDeleted);
			if (hasPendingDeletions) {
				this.containsDeletedSchool.set(true);
			}
		});

		effect(() => {
			const hasPendingUpdates = this.schoolService.schools().some((s) => s.isModified);
			if (hasPendingUpdates) {
				this.containsUpdatedSchool.set(true);
			}
		});
	}

	ngOnInit() {
		this.loadAllCodeGroups();

		this.configDeliveryService.afterSave$
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.schoolService.reload());
	}

	selectCanton(canton: Canton | null): void {
		if (canton) {
			this.cantonService.selectCanton(canton);

			if (canton.cantonId) {
				const currentFilters = this.currentFilters();
				this.cantonInterventionService.loadCantonInterventions(
					currentFilters.versionFilter,
					canton.cantonId,
				);
			}
		} else {
			this.cantonInterventionService.emptyCantonInterventionsMap();
		}
	}

	selectCantonIntervention(cantonIntervention: CantonIntervention | null): void {
		if (cantonIntervention) {
			this.cantonInterventionService.selectCantonIntervention(cantonIntervention);
		}
	}

	selectConfigDelivery(configDeliveries: ConfigDelivery[] | null): void {
		if (Array.isArray(configDeliveries)) {
			this.configDeliveryService.selectConfigDelivery(configDeliveries);

			if (this.configDeliveryService.isMaster()) {
				const configDelivryIds = configDeliveries
					.filter((configDeliveries) => configDeliveries.deliveryId)
					.map((configDelivery) => configDelivery.deliveryId);

				this.schoolService.loadSchoolsAsSlave(configDelivryIds);
			}
		} else {
			this.selectConfigDelivery([]);

			if (this.configDeliveryService.isMaster()) {
				this.schoolService.selectSchool([]);
			}
		}
	}

	selectSchool(schools: any): void {
		if (Array.isArray(schools)) {
			this.schoolService.selectSchool(schools);

			if (this.schoolService.isMaster()) {
				const schoolIds = schools
					.filter((school) => school.schoolId)
					.map((school) => school.schoolId);

				this.configDeliveryService.loadConfigDeliveriesAsSlave(schoolIds);
			}
		} else {
			this.schoolService.selectSchool([]);

			if (this.schoolService.isMaster()) {
				this.configDeliveryService.emptyData();
			}
		}
	}

	initialiseYear(filters: VersionCantonFilterType): void {
		this.currentFilters.set(filters);
		this.cantonService
			.initVersion(filters.versionFilter, filters.cantonFilter, filters.syncSchool)
			.pipe(takeUntilDestroyed(this.destroyRef))
			.subscribe((response: InitVersionResponse) => {
				const withSync = !!response?.data;
				this.schoolService.setWithSync(withSync);
				this.filterChange({ ...filters, syncSchool: withSync });
			});
	}

	filterChange(filters: VersionCantonFilterType): void {
		this.currentFilters.set(filters);
		this.uploadCanton.set(filters.cantonFilter ?? null);

		this.cantonService.loadCantons(filters.versionFilter, filters.cantonFilter);

		const service = this.configDeliveryService.isMaster()
			? this.configDeliveryService
			: this.schoolService;
		let webFilters: WebFilter[] = [];

		if (filters.selectedPredefinedFilters) {
			webFilters = this.mapPredefinedFiltersToWebFilters(
				filters.selectedPredefinedFilters,
				service,
			);
		}

		if (this.configDeliveryService.isMaster()) {
			this.configDeliveryService.selectConfigDelivery([]);
			this.clearConfigDeliverySelection.set(Date.now());
			this.schoolService.selectSchool([]);
			this.clearSchoolSelection.set(Date.now());
			this.schoolService.emptyData();

			this.configDeliveryService.loadConfigDeliveries(
				filters.versionFilter,
				filters.cantonFilter,
				webFilters,
				filters.contextFilters,
			);
		} else {
			this.clearSchoolSelection.set(Date.now());
			this.schoolService.emptyData();

			this.schoolService.loadSchools(
				filters.versionFilter,
				filters.cantonFilter,
				webFilters,
				filters.contextFilters,
			);
		}

		this.cantonService.getMissingCantons();
	}

	onCantonSelectionChanged(canton: number | null): void {
		this.uploadCanton.set(canton);
	}

	uploadFile(file: File): void {
		const filters = this.currentFilters();

		this.uploadFileService
			.uploadFile(file, 'initialisations/canton-interventions/upload_file/', 'upload_file', {
				...filters,
				cantonFilter: this.uploadCanton() as number,
			})
			.pipe(finalize(() => this.uploading.set(false)))
			.subscribe({
				next: (message) => {
					console.log('Upload réussi:', message);
					this.uploadMessage.set(message);
					if (message) {
						this.obNotificationService.info({
							channel: 'oblique',
							message,
						});
					}
				},
				error: (error) => {
					console.error('Erreur upload:', error);
					this.uploadMessage.set(error.message);
					this.obNotificationService.error({
						channel: 'oblique',
						message: error.message,
					});
				},
			});
	}

	handleTableEventForCanton(event: TableEvent): void {
		this.handleTableEvent<Canton>(
			event,
			this.cantonService,
			this.containsNewCanton,
			this.containsDeletedCanton,
			this.containsUpdatedCanton,
			this.cleanItems,
			'cantons',
		);
	}

	handleTableEventForCantonIntervention(event: TableEvent): void {
		this.handleTableEvent<CantonIntervention>(
			event,
			this.cantonInterventionService,
			this.containsNewCantonIntervention,
			this.containsDeletedCantonIntervention,
			this.containsUpdatedCantonIntervention,
			this.cleanItems,
			'cantonIntervenions',
		);
	}

	handleTableEventForConfigDelivery(event: TableEvent): void {
		this.handleTableEvent<ConfigDelivery>(
			event,
			this.configDeliveryService,
			this.containsNewConfigDelivery,
			this.containsDeletedConfigDelivery,
			this.containsUpdatedConfigDelivery,
			this.cleanItems,
			'configDeliveries',
		);
	}

	handleTableEventForSchool(event: TableEvent): void {
		this.handleTableEvent<School>(
			event,
			this.schoolService,
			this.containsNewSchool,
			this.containsDeletedSchool,
			this.containsUpdatedSchool,
			this.cleanItems,
			'schools',
		);
	}

	private mapPredefinedFiltersToWebFilters(
		predefinedFilters: PredefinedFilter[],
		service: ConfigDeliveryService | SchoolService,
	): WebFilter[] {
		return predefinedFilters
			.map((predefined) => {
				const loadedFilter = service.webFilters().find((f) => f.filterId === predefined.id);
				return updateWebFilterWithPredefinedFilter(loadedFilter, predefined);
			})
			.filter((f) => f !== null);
	}

	private handleTableEvent<T>(
		event: TableEvent,
		service: {
			delete: (items: T[]) => any;
			create: (items: T[]) => any;
			update: (items: T[]) => any;
		},
		containsNew: WritableSignal<boolean>,
		containsDeleted: WritableSignal<boolean>,
		containsUpdated: WritableSignal<boolean>,
		cleaners: (item: T[]) => T[],
		entityName: string,
	): void {
		const { itemsToUpdate = [], itemsToCreate = [], itemsToDelete = [] } = event;

		if (!itemsToUpdate.length) {
			containsUpdated.set(false);
		}
		if (!itemsToCreate.length) {
			containsNew.set(false);
		}
		if (!itemsToDelete.length) {
			containsDeleted.set(false);
		}
		if (!itemsToUpdate.length && !itemsToCreate.length && !itemsToDelete.length) {
			return;
		}

		if (event.action === 'save') {
			if (itemsToDelete.length > 0) {
				console.log(`🗑️ Deleting ${entityName}:`, itemsToDelete);
				const cleanedDeletes = cleaners(itemsToDelete);
				service.delete(cleanedDeletes);
			}

			if (itemsToCreate.length > 0) {
				console.log(`➕ Creating ${entityName}:`, itemsToCreate);
				const cleanedCreates = cleaners(itemsToCreate);
				service.create(cleanedCreates);
			}

			if (itemsToUpdate.length > 0) {
				console.log(`🔄 Updating ${entityName}:`, itemsToUpdate);
				const cleanedUpdates = cleaners(itemsToUpdate);
				service.update(cleanedUpdates);
			}

			containsNew.set(false);
			containsDeleted.set(false);
			containsUpdated.set(false);
		}

		if (event.action === 'rowChanged') {
			console.log(event.action, itemsToUpdate);
			containsUpdated.set(itemsToUpdate.length > 0);
			return;
		}

		if (['add', 'delete'].includes(event.action)) {
			containsNew.set(itemsToCreate.length > 0);
			containsDeleted.set(itemsToDelete.length > 0);
		}
	}

	private cleanItems<T>(items: any[]): T[] {
		return items.map((item) => {
			const cleaned: any = {};

			Object.keys(item).forEach((key) => {
				const value = item[key];

				if (['isNew', 'isDeleted', 'isModified', '_selected', 'classCondition'].includes(key)) {
					return;
				}

				if (key.includes('Flg') || key === 'isSpecialSchool' || key === 'isToDelete') {
					cleaned[key] = value ? 1 : 0;
					return;
				}

				if (value === '' || value === undefined) {
					cleaned[key] = null;
				} else {
					cleaned[key] = value;
				}
			});

			return cleaned as T;
		});
	}

	private loadAllCodeGroups(): void {
		const codeGroups = [
			'CANTON',
			'MEB_DELIVERYSTATUS',
			'MEB_PLAUSISTATUS',
			'SBA_CANTONINTTYPE',
			'MEB_SYNCHSTATUS',
		];

		codeGroups.forEach((group) => this.codeGroupStore.loadValues(group));

		this.codeGroupStore.loadValues('MUNICIPALITY', undefined, undefined, true);
	}

	private setAsMaster(entityName?: string) {
		if (!entityName) {
			return;
		}
		console.log('set as master :', entityName, this.schoolService.isMaster());

		const filters = this.currentFilters();

		let webFilters: WebFilter[] = [];

		if (entityName === 'configDeliveries' && this.schoolService.isMaster()) {
			this.configDeliveryService.switchMaster();
			this.schoolService.setAsSlave();

			if (filters.selectedPredefinedFilters) {
				webFilters = this.mapPredefinedFiltersToWebFilters(
					filters.selectedPredefinedFilters,
					this.configDeliveryService,
				);
			}

			this.configDeliveryService.loadConfigDeliveries(
				filters.versionFilter,
				filters.cantonFilter,
				webFilters,
				filters.contextFilters,
			);
			this.schoolService.selectSchool();
			this.schoolService.emptyData();
		} else if (entityName === 'schools' && this.configDeliveryService.isMaster()) {
			this.schoolService.switchMaster();
			this.configDeliveryService.setAsSlave();

			if (filters.selectedPredefinedFilters) {
				webFilters = this.mapPredefinedFiltersToWebFilters(
					filters.selectedPredefinedFilters,
					this.configDeliveryService,
				);
			}

			this.schoolService.loadSchools(
				filters.versionFilter,
				filters.cantonFilter,
				webFilters,
				filters.contextFilters,
			);
			this.configDeliveryService.selectConfigDelivery([]);
			this.configDeliveryService.emptyData();
		}
	}

	private enrichColumnsWithOptions(columns: DatahandlerColumn[]): DatahandlerColumn[] {
		return columns.map((col) => {
			const clonedCol = { ...col };

			if (col.optionsSource && col.optionsSource in this.optionsRegistry) {
				const optionSignal =
					this.optionsRegistry[col.optionsSource as keyof typeof this.optionsRegistry];
				clonedCol.options = optionSignal();
			}

			if (col.filterOptionsSource && col.filterOptionsSource in this.optionsRegistry) {
				const filterOptionSignal =
					this.optionsRegistry[col.filterOptionsSource as keyof typeof this.optionsRegistry];
				clonedCol.filterOptions = filterOptionSignal();
			}

			return clonedCol;
		});
	}
}
