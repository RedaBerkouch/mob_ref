import { Component, computed, effect, inject, OnInit, Signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AdminFilterService, WebFilter } from '../services/admin-filter';
import { AdminPlausiService, Plausi } from '../services/admin-plausi';
import { AdminExportService, Export } from '../services/admin-export';
import { AdminFilterParamService } from '../services/admin-filter-param';
import { AdminExportParamService } from '../services/admin-export-param';
import { AdminPlausiParamService } from '../services/admin-plausi-param';
import { CodeGroupStore } from '../services/CodeGroupStore';
import { LanguageService } from '../services/language';
import { LocalizedCode } from '../model/LocalizedCode';
import { ObNotificationService } from '@oblique/oblique';

type ColumnType = 'text' | 'checkbox' | 'select' | 'date' | 'parametre';

interface ConfigColumn {
	key: string;
	label: string;
	width: string;
	type: ColumnType;
	editable: boolean;
	optionsSource: string;
	options?: { label: string; value: any }[];
}

interface ConfigAction {
	icon: string;
	tooltip: string;
	label: string;
	disabled: boolean;
	handler: () => void;
}

interface TableConfig {
	title: string;
	isByDefaultCollapsed: boolean;
	columns: ConfigColumn[];
	actions: ConfigAction[];
}

@Component({
	selector: 'app-administration',
	standalone: false,
	templateUrl: './administration.html',
	styleUrls: ['./administration.scss'],
})
export class Administration implements OnInit {
	private codeGroupStore = inject(CodeGroupStore);
	private reloadService = inject(LanguageService);
	private readonly obNotificationService = inject(ObNotificationService);

	currentAlert: { type: 'success' | 'info' | 'warning' | 'error'; message: string } | null = null;

	objectTypeOptions = this.mapCodeGroupToOptions(
		'SBA_OBJECTTYPE',
		(item) => `${item.value} (${item.key})`,
	);
	private MebRoleOptions = this.mapCodeGroupToOptions(
		'MEB_ROLE',
		(item) => `${item.value} (${item.key})`,
	);
	private MebPlausiTypes = this.mapCodeGroupToOptions(
		'MEB_PLAUSITYPE',
		(item) => `${item.value} (${item.key})`,
	);
	private MebExportTypes = this.mapCodeGroupToOptions(
		'MEB_EXPORTTYPE',
		(item) => `${item.value} (${item.key})`,
	);

	currentFilterParamObject: any = null;
	currentPlausiParamObject: any = null;
	currentExploitationParamObject: any = null;

	tables: {
		[key: string]: {
			config: TableConfig;
			dataSource: any[];
		};
	} = {};

	newFilterName = '';

	private validationMessages: Record<string, { title: string; message: string }> = {
		FILTER_OBJECTTYPE_EMPTY: {
			title: 'Warning',
			message: 'filter.objectempty.message',
		},
		FILTER_AUTHORISATION_EMPTY: {
			title: 'Warning',
			message: 'filter.authorisationempty.message',
		},
		FILTER_SOURCE_EMPTY: {
			title: 'Warning',
			message: 'filter.sourceempty.message',
		},
		PLAUSI_TYPE_EMPTY: {
			title: 'Warning',
			message: 'plausi.typeempty.message',
		},
		PLAUSI_OBJECTLEVEL_EMPTY: {
			title: 'Warning',
			message: 'plausi.objectempty.message',
		},
		VALIDATION_ERROR: {
			title: 'Warning',
			message: 'validation.error.message',
		},
	};

	constructor(
		private http: HttpClient,
		public adminFilterService: AdminFilterService,
		public adminPlausiService: AdminPlausiService,
		public adminExportService: AdminExportService,
		public adminFilterParamService: AdminFilterParamService,
		public adminPlausiParamService: AdminPlausiParamService,
		public adminExportParamService: AdminExportParamService,
	) {
		effect(() => {
			const filters = this.adminFilterService.filters();
			if (this.tables['adminFilter']) {
				this.tables['adminFilter'].dataSource = filters;
			}
			console.log('✅ Filtres mis à jour :', filters);
		});

		effect(() => {
			const exports = this.adminExportService.exports();
			if (this.tables['adminExploitation']) {
				this.tables['adminExploitation'].dataSource = exports;
			}
			console.log('✅ Exploitations mises à jour :', exports);
		});

		effect(() => {
			const plausis = this.adminPlausiService.plausis();
			if (this.tables['reglePlausi']) {
				this.tables['reglePlausi'].dataSource = plausis;
			}
			console.log('✅ Plausis mis à jour :', plausis);
		});

		effect(() => {
			const params = this.adminFilterParamService.parameters();
			if (this.tables['paramAdminFilter']) {
				this.tables['paramAdminFilter'].dataSource = params;
				console.log('📥 Paramètres filtre chargés :', params);
			}
		});

		effect(() => {
			const params = this.adminPlausiParamService.parameters();
			if (this.tables['paramReglePlausi']) {
				this.tables['paramReglePlausi'].dataSource = params;
				console.log('📥 Paramètres Plausi chargés :', params);
			}
		});

		effect(() => {
			const params = this.adminExportParamService.parameters();
			if (this.tables['paramExploitation']) {
				this.tables['paramExploitation'].dataSource = params;
				console.log('📥 Paramètres Export chargés :', params);
			}
		});

		effect(() => {
			this.reloadService.currentLanguage();
			this.loadAllCodeGroups();
		});

		effect(() => {
			this.updateTablesWithDynamicOptions('this.objectTypeOptions()', this.objectTypeOptions());
		});

		effect(() => {
			this.updateTablesWithDynamicOptions('this.MebRoleOptions()', this.MebRoleOptions());
		});

		effect(() => {
			this.updateTablesWithDynamicOptions('this.MebPlausiTypes()', this.MebPlausiTypes());
		});

		effect(() => {
			this.updateTablesWithDynamicOptions('this.MebExportTypes()', this.MebExportTypes());
		});
	}

	ngOnInit(): void {
		this.loadAllCodeGroups();

		this.http
			.get<{ [key: string]: TableConfig }>('./assets/tables-administration-config.json')
			.subscribe((configs) => {
				for (const key of Object.keys(configs)) {
					const config = configs[key];

					config.columns.forEach((col) => {
						if (col.optionsSource === 'this.objectTypeOptions()') {
							col.options = [...this.objectTypeOptions()];
						}

						if (col.optionsSource === 'this.MebRoleOptions()') {
							col.options = [...this.MebRoleOptions()];
						}

						if (col.optionsSource === 'this.MebPlausiTypes()') {
							col.options = [...this.MebPlausiTypes()];
						}

						if (col.optionsSource === 'this.MebExportTypes()') {
							col.options = [...this.MebExportTypes()];
						}
					});

					config.actions?.forEach((action) => {
						if (action.label === 'Ajouter') {
							action.handler = () => this.handleAddRow(key);
						} else if (action.label === 'Supprimer') {
							action.handler = () => this.handleDeleteRow(key);
						}
					});

					this.tables[key] = {
						config,
						dataSource: [],
					};
				}

				if (this.tables['adminFilter']) {
					this.adminFilterService.loadAll();
				}

				if (this.tables['adminExploitation']) {
					this.adminExportService.loadAll();
				}

				if (this.tables['reglePlausi']) {
					this.adminPlausiService.loadAll();
				}
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

	private loadAllCodeGroups(): void {
		this.codeGroupStore.loadValues('SBA_OBJECTTYPE');
		this.codeGroupStore.loadValues('MEB_ROLE');
		this.codeGroupStore.loadValues('MEB_PLAUSITYPE');
		this.codeGroupStore.loadValues('MEB_EXPORTTYPE');
	}

	private updateTablesWithDynamicOptions(source: string, options: any[]) {
		Object.values(this.tables).forEach((table) => {
			table.config.columns.forEach((col) => {
				if (col.optionsSource === source) {
					col.options = [...options];
				}
			});
		});
	}

	private getFilterId(obj: any): number | undefined {
		return obj?.filterId;
	}

	private getPlausiId(obj: any): number | undefined {
		return obj?.plausiId ?? obj?.macroid;
	}

	private getExportId(obj: any): number | undefined {
		return obj?.exportId ?? obj?.macroid;
	}

	tableKeys(): string[] {
		return Object.keys(this.tables);
	}

	showAlert(type: 'success' | 'info' | 'warning' | 'error', message: string) {
		this.currentAlert = { type, message };
	}

	clearAlert() {
		this.currentAlert = null;
	}

	addFilter() {
		const newFilter: WebFilter = { nameDe: this.newFilterName };
		this.adminFilterService.create(newFilter);
		this.newFilterName = '';
	}

	deleteFilter(id: number) {
		this.adminFilterService.delete(id);
	}

	onDataChanged(updatedData: any[], tableKey: string) {
		if (this.tables[tableKey]) {
			this.tables[tableKey].dataSource = updatedData;
			console.log(`🔄 Données mises à jour depuis Datahandler pour ${tableKey}:`, updatedData);
		}
	}

	handleTableEvent(event: any, tableKey: string) {
		const { action, data } = event;

		this.clearAlert();

		const fullDataActions = new Set(['save', 'add', 'delete', 'dataChanged']);

		if (data && fullDataActions.has(action)) {
			this.tables[tableKey].dataSource = data;
			console.log(`🔄 Données mises à jour pour ${tableKey}`, data);
		}

		switch (action) {
			case 'validation-error': {
				const msg =
					this.validationMessages[event.errorType] || this.validationMessages['VALIDATION_ERROR'];

				this.obNotificationService.warning({
					title: msg.title,
					message: msg.message,
				});
				break;
			}
			case 'save':
				this.handleSave(tableKey);
				this.showAlert('success', 'Données enregistrées avec succès.');
				break;
			case 'add':
				this.showAlert('info', 'Nouvelle ligne ajoutée.');
				break;
			case 'delete':
				this.showAlert('warning', 'Ligne(s) marquées comme supprimées.');
				break;
			case 'block':
				this.showAlert('info', 'Table réinitialisée.');
				this.refreshTable(tableKey);
				break;
			default:
				console.warn(`⚠️ Action non gérée : ${action} pour ${tableKey}`);
		}
	}

	refreshTable(tableKey: string) {
		switch (tableKey) {
			case 'adminFilter':
				this.adminFilterService.loadAll();
				break;

			case 'reglePlausi':
				this.adminPlausiService.loadAll();
				break;

			case 'adminExploitation':
				this.adminExportService.loadAll();
				break;

			case 'paramAdminFilter': {
				const filterId = this.getFilterId(this.currentFilterParamObject);
				if (filterId) {
					this.adminFilterParamService.loadAll(filterId);
				}
				break;
			}

			case 'paramReglePlausi': {
				const plausiId = this.getPlausiId(this.currentPlausiParamObject);
				if (plausiId) {
					this.adminPlausiParamService.loadAll(plausiId);
				}
				break;
			}

			case 'paramExploitation': {
				const exportId = this.getExportId(this.currentExploitationParamObject);
				if (exportId) {
					this.adminExportParamService.loadAll(exportId);
				}
				break;
			}
		}

		console.log(`🔄 Table ${tableKey} rechargée depuis backend`);
	}

	handleSave(tableKey: string) {
		const table = this.tables[tableKey];
		if (!table) return;

		const data = table.dataSource;
		console.log(`💾 Sauvegarde demandée pour ${tableKey}`, data);

		switch (tableKey) {
			case 'adminFilter':
				data.forEach((item) => {
					if (item.isNew) this.adminFilterService.create(item);
					else if (item.isDeleted && this.getFilterId(item))
						this.adminFilterService.delete(this.getFilterId(item)!);
					else if (this.getFilterId(item))
						this.adminFilterService.update(this.getFilterId(item)!, item);

					item.isNew = false;
					item.isDeleted = false;
				});
				this.refreshTable(tableKey);
				break;

			case 'paramAdminFilter': {
				const filterId = this.getFilterId(this.currentFilterParamObject);

				if (!filterId) {
					console.warn('⚠️ Impossible de sauvegarder paramAdminFilter : pas de filterId');
					return;
				}

				if (!this.currentFilterParamObject.parameters) {
					this.currentFilterParamObject.parameters = [];
				}

				data.forEach((item) => {
					const existingIndex = this.currentFilterParamObject.parameters.findIndex(
						(p: any) => p.parameterId === item.parameterId,
					);

					if (item.isDeleted) {
						if (item.parameterId) {
							this.adminFilterParamService.delete(filterId, item.parameterId);
						}

						if (existingIndex !== -1) {
							this.currentFilterParamObject.parameters.splice(existingIndex, 1);
						}
					} else if (item.isNew) {
						const { parameterId, ...paramSansId } = item;
						this.adminFilterParamService.create(filterId, paramSansId);
						this.currentFilterParamObject.parameters.push(item);
					} else {
						if (item.parameterId) {
							this.adminFilterParamService.update(filterId, item.parameterId, item);
						}

						if (existingIndex !== -1) {
							this.currentFilterParamObject.parameters[existingIndex] = item;
						} else {
							this.currentFilterParamObject.parameters.push(item);
						}
					}

					item.isNew = false;
					item.isDeleted = false;
				});

				this.adminFilterParamService.loadAll(filterId);
				break;
			}

			case 'reglePlausi':
				data.forEach((item) => {
					const plausiId = this.getPlausiId(item);

					if (item.isNew) this.adminPlausiService.create(item);
					else if (item.isDeleted && plausiId) this.adminPlausiService.delete(plausiId);
					else if (plausiId) this.adminPlausiService.update(plausiId, item);

					item.isNew = false;
					item.isDeleted = false;
				});
				this.refreshTable(tableKey);
				break;

			case 'paramReglePlausi': {
				const plausiId = this.getPlausiId(this.currentPlausiParamObject);

				if (!plausiId) {
					console.warn('⚠️ Impossible de sauvegarder paramReglePlausi : pas de plausiId');
					return;
				}

				if (!this.currentPlausiParamObject.parameters) {
					this.currentPlausiParamObject.parameters = [];
				}

				data.forEach((item) => {
					const idx = this.currentPlausiParamObject.parameters.findIndex(
						(p: any) => p.parameterId === item.parameterId,
					);

					if (item.isDeleted) {
						if (item.parameterId) {
							this.adminPlausiParamService.delete(plausiId, item.parameterId);
						}

						if (idx !== -1) {
							this.currentPlausiParamObject.parameters.splice(idx, 1);
						}
					} else if (item.isNew) {
						const { parameterId, ...paramSansId } = item;
						this.adminPlausiParamService.create(plausiId, paramSansId);
						this.currentPlausiParamObject.parameters.push(item);
					} else {
						if (item.parameterId) {
							this.adminPlausiParamService.update(plausiId, item.parameterId, item);
						}

						if (idx !== -1) {
							this.currentPlausiParamObject.parameters[idx] = item;
						} else {
							this.currentPlausiParamObject.parameters.push(item);
						}
					}

					item.isNew = false;
					item.isDeleted = false;
				});

				this.adminPlausiParamService.loadAll(plausiId);
				break;
			}

			case 'adminExploitation':
				data.forEach((item) => {
					const exportId = this.getExportId(item);

					if (item.isNew) this.adminExportService.create(item);
					else if (item.isDeleted && exportId) this.adminExportService.delete(exportId);
					else if (exportId) this.adminExportService.update(exportId, item);

					item.isNew = false;
					item.isDeleted = false;
				});
				this.refreshTable(tableKey);
				break;

			case 'paramExploitation': {
				const exportId = this.getExportId(this.currentExploitationParamObject);

				if (!exportId) {
					console.warn('⚠️ Impossible de sauvegarder paramExploitation : pas de exportId');
					return;
				}

				if (!this.currentExploitationParamObject.parameters) {
					this.currentExploitationParamObject.parameters = [];
				}

				data.forEach((item) => {
					const idx = this.currentExploitationParamObject.parameters.findIndex(
						(p: any) => p.parameterId === item.parameterId,
					);

					if (item.isDeleted) {
						if (item.parameterId) {
							this.adminExportParamService.delete(exportId, item.parameterId);
						}

						if (idx !== -1) {
							this.currentExploitationParamObject.parameters.splice(idx, 1);
						}
					} else if (item.isNew) {
						const { parameterId, ...paramSansId } = item;
						this.adminExportParamService.create(exportId, paramSansId);
						this.currentExploitationParamObject.parameters.push(item);
					} else {
						if (item.parameterId) {
							this.adminExportParamService.update(exportId, item.parameterId, item);
						}

						if (idx !== -1) {
							this.currentExploitationParamObject.parameters[idx] = item;
						} else {
							this.currentExploitationParamObject.parameters.push(item);
						}
					}

					item.isNew = false;
					item.isDeleted = false;
				});

				this.adminExportParamService.loadAll(exportId);
				break;
			}
		}

		console.log('✅ Sauvegarde terminée pour', tableKey);
	}

	handleAddRow(tableKey: string) {
		console.log(`➕ Nouvelle ligne ajoutée à ${tableKey}`);
	}

	handleDeleteRow(tableKey: string) {
		console.log(`🗑️ Lignes marquées comme supprimées pour ${tableKey}`);
	}

	handleUndo(tableKey: string) {
		const table = this.tables[tableKey];
		if (!table) return;

		switch (tableKey) {
			case 'adminFilter':
				this.adminFilterService.loadAll();
				break;
			case 'reglePlausi':
				this.adminPlausiService.loadAll();
				break;
			case 'adminExploitation':
				this.adminExportService.loadAll();
				break;
			case 'paramAdminFilter':
				this.refreshTable('paramAdminFilter');
				break;
			case 'paramReglePlausi':
				this.refreshTable('paramReglePlausi');
				break;
			case 'paramExploitation':
				this.refreshTable('paramExploitation');
				break;
		}

		console.log(`↩️ Annulation des modifications sur ${tableKey}`);
	}

	handleRowSelected(selectedRow: any, tableKey: string) {
		if (tableKey === 'adminFilter') {
			this.currentFilterParamObject = selectedRow;

			const filterId = this.getFilterId(selectedRow);
			if (!filterId) {
				console.warn('⚠️ Pas de filterId -> impossible de charger les paramètres filtre.');
				if (this.tables['paramAdminFilter']) {
					this.tables['paramAdminFilter'].dataSource = [];
				}
				return;
			}

			this.adminFilterParamService.loadAll(filterId);
			return;
		}

		if (tableKey === 'reglePlausi') {
			this.currentPlausiParamObject = selectedRow;

			const plausiId = this.getPlausiId(selectedRow);
			if (!plausiId) {
				console.warn('⚠️ Pas de plausiId/macroid -> impossible de charger les paramètres plausi.');
				if (this.tables['paramReglePlausi']) {
					this.tables['paramReglePlausi'].dataSource = [];
				}
				return;
			}

			this.adminPlausiParamService.loadAll(plausiId);
			return;
		}

		if (tableKey === 'adminExploitation') {
			this.currentExploitationParamObject = selectedRow;

			const exportId = this.getExportId(selectedRow);
			if (!exportId) {
				console.warn('⚠️ Pas de exportId/macroid -> impossible de charger les paramètres export.');
				if (this.tables['paramExploitation']) {
					this.tables['paramExploitation'].dataSource = [];
				}
				return;
			}

			this.adminExportParamService.loadAll(exportId);
		}
	}
}
