import {
	ChangeDetectionStrategy,
	Component,
	computed,
	DestroyRef,
	effect,
	inject,
	OnInit,
	signal,
	untracked,
	WritableSignal
} from '@angular/core';

import { CodeGroupStore } from '../services/CodeGroupStore';
import tableConfigs from '../../assets/tables-datamaintain-config.json';
import { WebFilter } from '../model/Delivery';
import {
	ContextFilterConfig,
	PredefinedFilter,
	WhereFilter
} from '../shared/pre-advance-filter/pre-advance-filter';
import { updateWebFilterWithPredefinedFilter } from '../core/utils/filters.util';
import {
	DatahandlerColumn,
	DatahandlerConfig,
	TableEvent
} from '../shared/datahandler/datahandler';
import { PersonService } from '../services/maintains/person';
import { QualificationService } from '../services/maintains/qualification';
import { UserService } from '../services/user';
import {
	disableDeleteButtonForPersons,
	disableUndoValidateButton,
	disableValidateButton,
	enrichDefaultValuesForPersons,
	enrichEditableColumnsForPersons
} from '../services/maintains/person.utils';
import {
	disableDeleteButtonForQualifications,
	enrichEditableColumnsForQualifications
} from '../services/maintains/qualification.utils';
import { DialogService } from '../services/dialog';
import { MaintainStateService } from '../services/maintains/maintain-state.service';
import { ObNotificationService } from '@oblique/oblique';
import { Person, Qualification } from '../model/Maintain';
import { LanguageService } from '../services/language';
import { DeliveryService } from '../services/deliveries/delivery';

/**
 * Composant de maintenance des données pour la gestion des personnes et qualifications.
 * Permet de basculer entre deux vues master (person/qualification) avec filtrage avancé.
 */
@Component({
	selector: 'app-data-maintain',
	standalone: false,
	templateUrl: './data-maintain.html',
	styleUrl: './data-maintain.scss',
	changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataMaintain implements OnInit {
	// Services injectés
	protected readonly personService = inject(PersonService);
	protected readonly deliveryService = inject(DeliveryService);
	protected readonly qualificationService = inject(QualificationService);
	protected readonly maintainStateService = inject(MaintainStateService);
	protected readonly languageService = inject(LanguageService);
	private readonly codeGroupStore = inject(CodeGroupStore);
	protected userService = inject(UserService);
	private readonly dialogService = inject(DialogService);
	private readonly obNotificationService = inject(ObNotificationService);

	// ✅ pour cleanup subscriptions
	private readonly destroyRef = inject(DestroyRef);

	// -------------------------------------------------------------------
	// ✅ IMPORTANT ANTI-LEAK
	// - NE PAS refaire des computed() par codegroup dans le composant
	// - On utilise CodeGroupStore.optionsFor(...) (signal stable, cached)
	// -------------------------------------------------------------------
	readonly cantonOptions = this.codeGroupStore.optionsFor('CANTON', (item) => `${item.value} (${item.key})`, true);
	private readonly genreOptions = this.codeGroupStore.optionsFor('SEX', (item) => `${item.value} (${item.key})`);
	private readonly deliveryStatusOptions = this.codeGroupStore.optionsFor('MEB_DELIVERYSTATUS');
	private readonly countriesOptions = this.codeGroupStore.optionsFor('COUNTRY', (item) => `${item.value} (${item.key})`);
	private readonly municipalityOptions = this.codeGroupStore.optionsFor('MUNICIPALITY', (item) => `${item.value} (${item.key})`);
	private readonly plausiStatusOptions = this.codeGroupStore.optionsFor('MEB_PLAUSISTATUS');
	private readonly maturityLanguagesOptions = this.codeGroupStore.optionsFor('SBA_MATURITY_LANGUAGES');
	private readonly examResultOptions = this.codeGroupStore.optionsFor('EXAM_RESULT', (item) => `${item.value} (${item.key})`);
	private readonly examTypeOptions = this.codeGroupStore.optionsFor('EXAM_TYPE', (item) => `${item.value} (${item.key})`);
	private readonly examEducationTypeOptions = this.codeGroupStore.optionsFor('EXAM_EDUCATION_TYPE', (item) => `${item.value} (${item.key})`);
	private readonly dataStatusOptions = this.codeGroupStore.optionsFor('MEB_DATASTATUS');

	// Signals pour contrôler le clear selection du datahandler (persons)
	clearPersonsSelection = signal(0);
	// Signals pour contrôler le clear selection du datahandler (qualifications)
	clearQualificationsSelection = signal(0);

	// Signals pour indiquer si la table des personnes contient des nouvelles personnes où à supprimmer
	containsNewPerson = signal(false);
	containsDeletedPerson = signal(false);
	containsUpdatedPerson = signal(false);
	containsModificationForPersons = computed(
		() => this.containsNewPerson() || this.containsDeletedPerson() || this.containsUpdatedPerson()
	);

	// Signals pour indiquer si la table des qualifications contient des nouvelles personnes où à supprimmer
	containsNewQualification = signal(false);
	containsDeletedQualification = signal(false);
	containsUpdatedQualification = signal(false);
	containsModificationForQualifications = computed(
		() => this.containsNewQualification() || this.containsDeletedQualification() || this.containsUpdatedQualification()
	);

	// Signal computed qui combine les deux erreurs (persons et qualifications)
	private readonly opError = computed(() => this.personService.opError() || this.qualificationService.opError());

	private actionByIcon: Record<string, string> = {
		file_download: 'export',
		switch_master: 'switch_master',
		done_all: 'prevalidate',
		remove_done: 'undo_validate',
		save: 'save',
		block: 'block',
		add: 'add',
		delete: 'delete'
	} as const;

	// Registry centralisé pour faciliter l'injection d'options dans les configs
	private readonly optionsRegistry = {
		cantonOptions: this.cantonOptions,
		deliveryStatusOptions: this.deliveryStatusOptions,
		plausiStatusOptions: this.plausiStatusOptions,
		countriesOptions: this.countriesOptions,
		municipalityOptions: this.municipalityOptions,
		genreOptions: this.genreOptions,
		maturityLanguagesOptions: this.maturityLanguagesOptions,
		examResultOptions: this.examResultOptions,
		examTypeOptions: this.examTypeOptions,
		examEducationTypeOptions: this.examEducationTypeOptions,
		dataStatusOptions: this.dataStatusOptions
	} as const;

	// ✅ helper: clone d’action pour éviter de muter des objets partagés (JSON)
	private cloneActions(actions: any[] = []) {
		return actions.map((a) => ({ ...a }));
	}

	// ✅ helper: clone de colonnes (shallow) – suffisant ici
	private cloneColumns(cols: DatahandlerColumn[] = []) {
		return cols.map((c) => ({ ...c }));
	}

	// Configuration dynamique pour la table des personnes
	readonly personsConfig = computed(() => {
		const baseConfig = tableConfigs['persons'] as DatahandlerConfig;

		const roles = this.userService.roles();
		const isMaster = this.maintainStateService.isPersonMaster();

		// ✅ clone pour éviter mutations cumulatives
		const baseColumns = this.cloneColumns(baseConfig.columns ?? []);
		const baseActions = this.cloneActions(baseConfig.actions ?? []);

		// Pipeline de transformation : options → editable columns
		const enrichedColumnsWithOptions = this.enrichColumnsWithOptions(baseColumns);
		const editable = enrichEditableColumnsForPersons(enrichedColumnsWithOptions, roles);

		// ✅ FORCE non-editable pour version + canton (quoi qu’il arrive)
		const locked = editable.map((c) =>
			c.key === 'version' || c.key === 'canton'
				? { ...c, editable: false, editableOnCondition: undefined }
				: c,
		);

		const enrichedColumns = enrichDefaultValuesForPersons(locked);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichedColumns ?? baseColumns,
			actions: baseActions,
		};

		const disableHandlers: Record<string, () => boolean> = {
			export: () => false,
			switch_master: () => isMaster,
			prevalidate: () =>
				disableValidateButton(
					isMaster,
					this.containsModificationForPersons(),
					this.maintainStateService.selectedPersons(),
					roles,
				),
			undo_validate: () =>
				disableUndoValidateButton(
					isMaster,
					this.containsModificationForPersons(),
					this.maintainStateService.selectedPersons(),
					roles,
				),
			save: () => !this.containsModificationForPersons(),
			block: () => !this.containsModificationForPersons(),
			add: () => false,
			delete: () =>
				disableDeleteButtonForPersons(this.maintainStateService.selectedPersons(), roles),
		};

		const actionHandlers: Record<string, () => void> = {
			export: () => this.personService.export(),
			switch_master: () => this.maintainStateService.setCurrentMaster('person'),
			prevalidate: () =>
				this.dialogService.confirmAndDo('confirm.validate.message', () =>
					this.personService.prevalidate(),
				),
			undo_validate: () =>
				this.dialogService.confirmAndDo('confirm.undo.validate.message', () =>
					this.personService.undoValidate(),
				),
			save: () => console.log('on save'),
			block: () => console.log('on block'),
			add: () => console.log('on add'),
			delete: () => console.log('on delete'),
		};

		config.actions.forEach((action) => {
			const key = this.actionByIcon[action.icon];
			action.disabledFn = disableHandlers[key] ?? (() => false);
			action.handler = actionHandlers[key] ?? (() => {});
		});

		return config;
	});

	// Configuration dynamique pour la table des qualifications
	readonly qualificationsConfig = computed(() => {
		const baseConfig = tableConfigs['qualifications'] as DatahandlerConfig;

		const roles = this.userService.roles();
		const isMaster = this.maintainStateService.isQualificationMaster();
		const selectedPersons = this.maintainStateService.selectedPersons();

		// ✅ clone pour éviter mutations cumulatives
		const baseColumns = this.cloneColumns(baseConfig.columns ?? []);
		const baseActions = this.cloneActions(baseConfig.actions ?? []);

		// Pipeline de transformation : options → editable columns
		const enrichedColumnsWithOptions = this.enrichColumnsWithOptions(baseColumns);
		const enrichedColumns = enrichEditableColumnsForQualifications(enrichedColumnsWithOptions, roles);

		const config: DatahandlerConfig = {
			...baseConfig,
			columns: enrichedColumns ?? baseColumns,
			actions: baseActions
		};

		const disableHandlers: Record<string, () => boolean> = {
			export: () => false,
			switch_master: () => isMaster,
			save: () => !this.containsModificationForQualifications(),
			block: () => !this.containsModificationForQualifications(),
			add: () => selectedPersons.length != 1,
			delete: () =>
				disableDeleteButtonForQualifications(
					this.maintainStateService.selectedQualifications(),
					this.containsNewQualification(),
					roles
				)
		};

		const actionHandlers: Record<string, () => void> = {
			export: () => this.qualificationService.export(),
			switch_master: () => this.maintainStateService.setCurrentMaster('qualification'),
			save: () => console.log('on save'),
			block: () => console.log('on block'),
			add: () => console.log('on add'),
			delete: () => console.log('on delete')
		};

		config.actions.forEach((action) => {
			const key = this.actionByIcon[action.icon];
			action.disabledFn = disableHandlers[key] ?? (() => false);
			action.handler = actionHandlers[key] ?? (() => {});
		});

		return config;
	});

	// Configuration des filtres contextuels selon le master actif
	readonly contextFiltersConfig = computed(() => {
		const baseColumns = this.maintainStateService.isPersonMaster()
			? tableConfigs.persons.columns
			: tableConfigs.qualifications.columns;

		// ✅ clone pour ne pas muter le JSON source
		return this.enrichColumnsWithOptions(this.cloneColumns(baseColumns as DatahandlerColumn[]))
			.map((col) => col as ContextFilterConfig)
			.filter((col) => col.filterableOnContext);
	});

	constructor() {
		// Effect 1: Notifier les erreurs
		effect(() => {
			const message = this.opError();
			if (message) {
				untracked(() => {
					this.obNotificationService.error({
						channel: 'oblique',
						message
					});
				});
			}
		});

		// Effect 2: Clear selections pendant les chargements
		effect(
			() => {
				const loadingPersons = this.maintainStateService.isLoadingPersons();
				const loadingQualifications = this.maintainStateService.isLoadingQualifications();

				untracked(() => {
					if (loadingPersons) this.clearPersonsSelection.set(Date.now());
					if (loadingQualifications) this.clearQualificationsSelection.set(Date.now());
				});
			});

		// Effect 3: Recharge les code groups quand la langue change
		effect(() => {
			this.languageService.currentLanguage();
			this.loadAllCodeGroups();
			this.codeGroupStore.loadValues('MUNICIPALITY', undefined, undefined, true)
		});
	}

	ngOnInit(): void {
		this.loadAllCodeGroups();
		this.codeGroupStore.loadValues('MUNICIPALITY', undefined, undefined, true)
	}

	/** Charge tous les code groups nécessaires pour les filtres et options */
	private loadAllCodeGroups(): void {
		const codeGroups = [
			'CANTON',
			'SEX',
			'COUNTRY',
			'MEB_DELIVERYSTATUS',
			'MEB_PLAUSISTATUS',
			'SBA_MATURITY_LANGUAGES',
			'EXAM_RESULT',
			'EXAM_TYPE',
			'EXAM_EDUCATION_TYPE',
			'MEB_DATASTATUS'
		];
		codeGroups.forEach((group) => this.codeGroupStore.loadValues(group));
	}

	/** Enrichit les colonnes avec les options depuis le registry */
	private enrichColumnsWithOptions(columns: DatahandlerColumn[]): DatahandlerColumn[] {
		return columns.map((col) => {
			const clonedCol = { ...col };
			if (col.optionsSource && col.optionsSource in this.optionsRegistry) {
				const optionSignal = this.optionsRegistry[col.optionsSource as keyof typeof this.optionsRegistry];
				clonedCol.options = optionSignal(); // <- array courant (le signal reste stable et caché côté store)
			}
			return clonedCol;
		});
	}

	/** Gère le changement des filtres version/canton, filtres prédéfinis, WebFilters et propage aux services */
	filterChange(filters: {
		versionFilter: number;
		cantonFilter: number;
		contextFilters: WhereFilter[];
		selectedPredefinedFilters: PredefinedFilter[];
	}): void {
		const service = this.maintainStateService.isPersonMaster() ? this.personService : this.qualificationService;

		let webFilters: WebFilter[] = [];
		if (filters.selectedPredefinedFilters) {
			webFilters = this.mapPredefinedFiltersToWebFilters(filters.selectedPredefinedFilters, service);
		}

		const canton = filters.cantonFilter && filters.cantonFilter > 0 ? String(filters.cantonFilter) : undefined;

		this.personService.setParams(filters.versionFilter, canton, webFilters, filters.contextFilters);
		this.qualificationService.setParams(filters.versionFilter, canton, webFilters, filters.contextFilters);
	}

	/** Convertit les filtres prédéfinis en WebFilters enrichis */
	private mapPredefinedFiltersToWebFilters(
		predefinedFilters: PredefinedFilter[],
		service: PersonService | QualificationService
	): WebFilter[] {
		return predefinedFilters
			.map((predefined) => {
				const loadedFilter = service.webFilters().find((f) => f.filterId === predefined.id);
				return updateWebFilterWithPredefinedFilter(loadedFilter, predefined);
			})
			.filter((f): f is WebFilter => f !== null);
	}

	/** Gère la sélection de personnes depuis la table */
	selectPersons(persons: any): void {
		if (Array.isArray(persons)) {
			this.personService.selectPersons(persons);
		} else {
			this.qualificationService.selectQualifications([]);
		}
	}

	/** Gère la sélection de qualifications depuis la table */
	selectQualifications(qualifications: any): void {
		if (Array.isArray(qualifications)) {
			this.qualificationService.selectQualifications(qualifications);
		} else {
			this.qualificationService.selectQualifications([]);
		}
	}

	handleTableEventforPersons(event: TableEvent): void {
		this.handleTableEvent<Person>(
			event,
			this.personService,
			this.containsNewPerson,
			this.containsDeletedPerson,
			this.containsUpdatedPerson,
			this.cleanItems,
			'persons'
		);
	}

	handleTableEventforQualifications(event: TableEvent): void {
		this.handleTableEvent<Qualification>(
			event,
			this.qualificationService,
			this.containsNewQualification,
			this.containsDeletedQualification,
			this.containsUpdatedQualification,
			this.cleanItems,
			'qualifications'
		);
	}

	private handleTableEvent<T>(
		event: TableEvent,
		service: { delete: (items: T[]) => any; create: (items: T[]) => any; update: (items: T[]) => any },
		containsNew: WritableSignal<boolean>,
		containsDeleted: WritableSignal<boolean>,
		containsUpdated: WritableSignal<boolean>,
		cleaners: (item: T[]) => T[],
		entityName: string
	): void {
		const { itemsToUpdate = [], itemsToCreate = [], itemsToDelete = [] } = event;

		// Early return si aucune donnée
		if (!itemsToUpdate.length) containsUpdated.set(false);
		if (!itemsToCreate.length) containsNew.set(false);
		if (!itemsToDelete.length) containsDeleted.set(false);
		if (!itemsToUpdate.length && !itemsToCreate.length && !itemsToDelete.length) return;

		if (event.action === 'save') {
			// DELETE
			if (itemsToDelete.length > 0) {
				console.log(`🗑️ Deleting ${entityName}:`, itemsToDelete);
				const cleanedDeletes = cleaners(itemsToDelete);
				service.delete(cleanedDeletes);
			}

			// CREATE
			if (itemsToCreate.length > 0) {
				console.log(`➕ Creating ${entityName}:`, itemsToCreate);
				const cleanedCreates = cleaners(itemsToCreate);
				service.create(cleanedCreates);
			}

			// UPDATE
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

				// Retirer les champs techniques
				if (['isNew', 'isDeleted', 'isModified', '_selected', 'classCondition'].includes(key)) {
					return;
				}

				// Convertir les booleans en nombres (0/1) pour les flags
				if (key.includes('Flg') || key === 'isSpecialSchool' || key === 'isToDelete') {
					cleaned[key] = value ? 1 : 0;
					return;
				}

				// Convertir les strings vides en null
				if (value === '' || value === undefined) {
					cleaned[key] = null;
				} else {
					cleaned[key] = value;
				}
			});

			return cleaned as T;
		});
	}
}
