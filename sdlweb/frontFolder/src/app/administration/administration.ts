import {Component, computed, effect, inject, OnInit, Signal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {AdminFilterService, WebFilter, WebFilterParameter} from '../services/admin-filter';
import { AdminPlausiService, Plausi } from '../services/admin-plausi';
import { AdminExportService, Export } from '../services/admin-export';
import {AdminFilterParamService} from "../services/admin-filter-param";
import {AdminExportParamService} from "../services/admin-export-param";
import {AdminPlausiParamService} from "../services/admin-plausi-param";
import {CodeGroupStore} from "../services/CodeGroupStore";
import {LanguageService} from "../services/language";
import {LocalizedCode} from "../model/LocalizedCode";
import {ObNotificationService} from "@oblique/oblique";

type ColumnType = 'text' | 'checkbox' | 'select' | 'date' | 'parametre';

interface ConfigColumn {
	key: string;
	label: string;
	width: string;
	type: ColumnType;
	editable: boolean;
	optionsSource: string;
	options?:  { label: string; value: any }[];
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
	styleUrls: ['./administration.scss']
})
export class Administration implements OnInit {
	private codeGroupStore = inject(CodeGroupStore);
	private reloadService = inject(LanguageService);
	 aaa = 'SBA_OBJECTTYPE';
	 bbb = 'MEB_ROLE';
	currentAlert: { type: 'success' | 'info' | 'warning' | 'error', message: string } | null = null;
	objectTypeOptions = this.mapCodeGroupToOptions('SBA_OBJECTTYPE', item => `${item.value} (${item.key})`);
	private MebRoleOptions = this.mapCodeGroupToOptions('MEB_ROLE', item => `${item.value} (${item.key})`);
	private MebPlausiTypes = this.mapCodeGroupToOptions('MEB_PLAUSITYPE', item => `${item.value} (${item.key})`);
	private MebExportTypes = this.mapCodeGroupToOptions('MEB_EXPORTTYPE', item => `${item.value} (${item.key})`);
	private mapCodeGroupToOptions(
		codeGroup: string,
		labelFormatter?: (item: LocalizedCode) => string
	): Signal<{ label: string, value: any }[]> {
		const defaultFormatter = (item: LocalizedCode) => item.value;
		const formatter = labelFormatter || defaultFormatter;

		return computed(() => {
			const values = this.codeGroupStore.getValuesSignal(codeGroup)();
			return values.map(item => ({
				label: formatter(item),
				value: item.key
			}));
		});
	}
	private loadAllCodeGroups(): void {
		this.codeGroupStore.loadValues('SBA_OBJECTTYPE');
		this.codeGroupStore.loadValues('MEB_ROLE');
		this.codeGroupStore.loadValues('MEB_PLAUSITYPE');
		this.codeGroupStore.loadValues('MEB_EXPORTTYPE');
	}
	constructor(
		private http: HttpClient,
		public adminFilterService: AdminFilterService,
		public adminPlausiService: AdminPlausiService,
		public adminExportService: AdminExportService,
		public adminFilterParamService: AdminFilterParamService,
		public adminPlausiParamService: AdminPlausiParamService,
	public adminExportParamService: AdminExportParamService
	) {
		// === Synchronisation automatique avec les signals ===

		// Filtres
		effect(() => {
			const filters = this.adminFilterService.filters();
			if (this.tables['adminFilter']) {
				this.tables['adminFilter'].dataSource = filters;
			}
			console.log('✅ Filtres mis à jour :', filters);
		});

		// Exploitations
		effect(() => {
			const exports = this.adminExportService.exports();
			if (this.tables['adminExploitation']) {
				this.tables['adminExploitation'].dataSource = exports;
			}
			console.log('✅ Exploitations mises à jour :', exports);
		});

		// Plausis
		effect(() => {
			const plausis = this.adminPlausiService.plausis();
			if (this.tables['reglePlausi']) {
				this.tables['reglePlausi'].dataSource = plausis;
			}
			console.log('✅ Plausis mis à jour :', plausis);
		});

		// Paramètres du filtre sélectionné
		effect(() => {
			const params = this.adminFilterParamService.parameters();
			if (this.tables['paramAdminFilter']) {
				this.tables['paramAdminFilter'].dataSource = params;
				console.log('📥 Paramètres chargés depuis backend :', params);
			}
		});
// Paramètres d'une règle Plausi
		effect(() => {
			const params = this.adminPlausiParamService.parameters();
			if (this.tables['paramReglePlausi']) {
				this.tables['paramReglePlausi'].dataSource = params;
				console.log('📥 Paramètres Plausi chargés :', params);
			}
		});

// Paramètres d'une exploitation
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
			const opts = this.objectTypeOptions();
			this.updateTablesWithDynamicOptions('this.objectTypeOptions()', opts);
		});

		effect(() => {
			const opts = this.MebRoleOptions();
			this.updateTablesWithDynamicOptions('this.MebRoleOptions()', opts);
		});

		effect(() => {
			const opts = this.MebPlausiTypes();
			this.updateTablesWithDynamicOptions('this.MebPlausiTypes()', opts);
		});

		effect(() => {
			const opts = this.MebExportTypes();
			this.updateTablesWithDynamicOptions('this.MebExportTypes()', opts);
		});

	}

	tables: {
		[key: string]: {
			config: TableConfig;
			dataSource: any[];
		}
	} = {};

	showAlert(type: 'success' | 'info' | 'warning' | 'error', message: string) {
		this.currentAlert = { type, message };

		// 🔥 Laisse l'alert visible jusqu'à action manuelle
		// ✔ Pas de timeout → persistent comme Oblique Alert le veut
	}

	clearAlert() {
		this.currentAlert = null;
	}


	ngOnInit(): void {
		// Chargement initial des code groups
		this.loadAllCodeGroups();



			this.http.get<{ [key: string]: TableConfig }>('./assets/tables-administration-config.json')
				.subscribe((configs) => {

					// 🔹 Création dynamique des tables
					for (const key of Object.keys(configs)) {
						const config = configs[key];

						// 🔥 Correction automatique des options dynamiques
						config.columns.forEach(col => {


							if (col.optionsSource === 'this.objectTypeOptions()') {
								col.options = this.objectTypeOptions(); // Signal réel
							}

							if (col.optionsSource === 'this.MebRoleOptions()') {
								col.options = this.MebRoleOptions();    // Signal réel
							}

							if (col.optionsSource === 'this.MebPlausiTypes()') {
								col.options = this.MebPlausiTypes();    // Signal réel
							}

							if (col.optionsSource === 'this.MebExportTypes()') {
								col.options = this.MebExportTypes();    // Signal réel
							}


						});

						// Injection des handlers (Add / Delete)
						config.actions?.forEach(action => {
							if (action.label === 'Ajouter') {
								action.handler = () => this.handleAddRow(key);
							} else if (action.label === 'Supprimer') {
								action.handler = () => this.handleDeleteRow(key);
							}
						});

						// Enregistrer la table
						this.tables[key] = {
							config,
							dataSource: []
						};
					}

					// 🔹 Lancer les chargements pour chaque domaine
					if (this.tables['adminFilter']) {
						console.log('🔄 Chargement des filtres depuis le backend...');
						this.adminFilterService.loadAll();
					}

					if (this.tables['adminExploitation']) {
						console.log('🔄 Chargement des exploitations depuis le backend...');
						this.adminExportService.loadAll();
					}

					if (this.tables['reglePlausi']) {
						console.log('🔄 Chargement des plausis depuis le backend...');
						this.adminPlausiService.loadAll();
					}
				});
	}


	private updateTablesWithDynamicOptions(source: string, options: any[]) {
		Object.values(this.tables).forEach(table => {
			table.config.columns.forEach(col => {
				if (col.optionsSource === source) {
					col.options = [...options];
				}
			});
		});
	}


	// === Méthodes génériques pour toutes les tables ===
	tableKeys(): string[] {
		return Object.keys(this.tables);
	}

	// === Spécifique à adminFilter ===
	newFilterName = '';

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

	private readonly obNotificationService = inject(ObNotificationService);

	private validationMessages: Record<string, { title: string, message: string }> = {
		FILTER_OBJECTTYPE_EMPTY: {
			title: 'Warning',
			message: 'filter.objectempty.message'
		},
		FILTER_AUTHORISATION_EMPTY: {
			title: 'Warning',
			message: 'filter.authorisationempty.message'
		},
		FILTER_SOURCE_EMPTY: {
			title: 'Warning',
			message: 'filter.sourceempty.message'
		},
		PLAUSI_TYPE_EMPTY: {
			title: 'Warning',
			message: 'plausi.typeempty.message'
		},
		PLAUSI_OBJECTLEVEL_EMPTY: {
			title: 'Warning',
			message: 'plausi.objectempty.message'
		}
	};


	handleTableEvent(event: any, tableKey: string) {
		const { action, data } = event;

		// danger: censurer les alertes pour les masquer
		this.clearAlert();

		const fullDataActions = new Set(['save', 'add', 'delete', 'dataChanged']);

		if (data && fullDataActions.has(action)) {
			this.tables[tableKey].dataSource = data;
			console.log(`🔄 Données mises à jour pour ${tableKey}`, data);
		}


		switch (action) {
			case 'validation-error':
				const msg = this.validationMessages[event.errorType]
					|| this.validationMessages["VALIDATION_ERROR"];

				this.obNotificationService.warning({
					title: msg.title,
					message: msg.message
				});
				break;

			case 'save':
				this.handleSave(tableKey);
				this.showAlert('success', 'Données enregistrées avec succès.');
				break;
			case 'add':
				console.log(`➕ Ligne ajoutée pour ${tableKey}`);
				this.showAlert('info', 'Nouvelle ligne ajoutée.');
				break;
			case 'delete':
				console.log(`🗑️ Ligne supprimée pour ${tableKey}`,event);
				this.showAlert('warning', 'Ligne(s) marquées comme supprimées.');
				break;
			case 'block':
				console.log(`⛔ Refresh demandé pour ${tableKey}`);
				this.showAlert('info', 'Table réinitialisée.');
				this.refreshTable(tableKey);  // 🔥 AJOUT
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

			case 'paramAdminFilter':
				if (this.currentFilterParamObject?.filterId) {
					this.adminFilterParamService.loadAll(this.currentFilterParamObject.filterId);
				}
				break;
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
				data.forEach(item => {
					console.log(item);
					if (item.isNew) this.adminFilterService.create(item);
					else if (item.isDeleted) this.adminFilterService.delete(item.filterId);
					else this.adminFilterService.update(item.filterId, item);

					// Nettoyage local
					item.isNew = false;
					item.isDeleted = false;
				});
this.refreshTable(tableKey)
				break;

			case 'paramAdminFilter':

				const filterId = this.currentFilterParamObject?.filterId;

				if (!filterId) {
					console.warn("⚠️ Impossible de sauvegarder paramAdminFilter : pas de filterId");
					return;
				}

				console.log(`💾 Sauvegarde des paramètres du filtre ${filterId}`, data);

				// Assurer l'existence de la liste locale
				if (!this.currentFilterParamObject.parameters) {
					this.currentFilterParamObject.parameters = [];
				}

				data.forEach(item => {

					// Trouver dans l'objet courant
					const existingIndex = this.currentFilterParamObject.parameters
						.findIndex((p: any) => p.parameterId === item.parameterId);

					// 🔥 SUPPRESSION
					if (item.isDeleted) {

						if (item.parameterId) {
							this.adminFilterParamService.delete(filterId, item.parameterId);
						}

						if (existingIndex !== -1) {
							this.currentFilterParamObject.parameters.splice(existingIndex, 1);
						}

					}
					// 🔥 CREATION
					else if (item.isNew) {

						// NE PAS passer parameterId → backend le génère
						const { parameterId, ...paramSansId } = item;

						this.adminFilterParamService.create(filterId, paramSansId);

						// Ajout local
						this.currentFilterParamObject.parameters.push(item);

					}
					// 🔥 MISE À JOUR
					else {

						if (item.parameterId) {
							this.adminFilterParamService.update(filterId, item.parameterId, item);
						}

						if (existingIndex !== -1) {
							this.currentFilterParamObject.parameters[existingIndex] = item;
						} else {
							this.currentFilterParamObject.parameters.push(item);
						}
					}

					// Nettoyage local des flags
					item.isNew = false;
					item.isDeleted = false;
				});

				// 🔄 Recharger depuis backend pour être sûr de la cohérence finale
				this.adminFilterParamService.loadAll(filterId);

				console.log("📌 currentFilterParamObject mis à jour :", this.currentFilterParamObject);

				break;




			case 'reglePlausi':
				data.forEach(item => {
					if (item.isNew) this.adminPlausiService.create(item);
					else if (item.isDeleted) this.adminPlausiService.delete(item.plausiId);
					else this.adminPlausiService.update(item.plausiId, item);

					// Nettoyage local
					item.isNew = false;
					item.isDeleted = false;
				});
				break;

			case 'paramReglePlausi':

				const plausiId = this.currentPlausiParamObject?.plausiId;

				if (!plausiId) {
					console.warn("⚠️ Impossible de sauvegarder paramReglePlausi : pas de plausiId");
					return;
				}

				console.log(`💾 Sauvegarde paramètres Plausi ${plausiId}`, data);

				if (!this.currentPlausiParamObject.parameters) {
					this.currentPlausiParamObject.parameters = [];
				}

				data.forEach(item => {

					const idx = this.currentPlausiParamObject.parameters
						.findIndex((p: any) => p.parameterId === item.parameterId);

					// 🔥 SUPPRESSION
					if (item.isDeleted) {

						if (item.parameterId) {
							this.adminPlausiParamService.delete(plausiId, item.parameterId);
						}

						if (idx !== -1) {
							this.currentPlausiParamObject.parameters.splice(idx, 1);
						}
					}
					// 🔥 CREATION
					else if (item.isNew) {

						const { parameterId, ...paramSansId } = item;

						this.adminPlausiParamService.create(plausiId, paramSansId);

						this.currentPlausiParamObject.parameters.push(item);

					}
					// 🔥 MISE À JOUR
					else {

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

				// 🔄 reload depuis backend
				this.adminPlausiParamService.loadAll(plausiId);

				console.log("📌 currentPlausiParamObject mis à jour", this.currentPlausiParamObject);

				break;



			case 'adminExploitation':
				data.forEach(item => {
					if (item.isNew) this.adminExportService.create(item);
					else if (item.isDeleted) this.adminExportService.delete(item.exportId);
					else this.adminExportService.update(item.exportId, item);

					// Nettoyage local
					item.isNew = false;
					item.isDeleted = false;
				});
				break;

			case 'paramExploitation':

				const exportId = this.currentExploitationParamObject?.exportId;

				if (!exportId) {
					console.warn("⚠️ Impossible de sauvegarder paramExploitation : pas de exportId");
					return;
				}

				console.log(`💾 Sauvegarde paramètres Export ${exportId}`, data);

				if (!this.currentExploitationParamObject.parameters) {
					this.currentExploitationParamObject.parameters = [];
				}

				data.forEach(item => {

					const idx = this.currentExploitationParamObject.parameters
						.findIndex((p: any) => p.parameterId === item.parameterId);

					// 🔥 SUPPRESSION
					if (item.isDeleted) {

						if (item.parameterId) {
							this.adminExportParamService.delete(exportId, item.parameterId);
						}

						if (idx !== -1) {
							this.currentExploitationParamObject.parameters.splice(idx, 1);
						}
					}
					// 🔥 CREATION
					else if (item.isNew) {

						const { parameterId, ...paramSansId } = item;

						this.adminExportParamService.create(exportId, paramSansId);

						this.currentExploitationParamObject.parameters.push(item);

					}
					// 🔥 MISE À JOUR
					else {

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

				// 🔄 reload
				this.adminExportParamService.loadAll(exportId);

				console.log("📌 currentExportParamObject mis à jour", this.currentExploitationParamObject);

				break;


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

		// recharge depuis le backend via le service
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
		}

		console.log(`↩️ Annulation des modifications sur ${tableKey}`);
	}


	currentFilterParamObject : any =null;
	currentPlausiParamObject : any =null;
	currentExploitationParamObject : any =null;

	handleRowSelected(selectedRow: any, tableKey: string) {
		/* ===========================
			   ▶️ 1) Paramètres des FILTRES
			   =========================== */
		if (tableKey === 'adminFilter') {

			this.currentFilterParamObject = selectedRow;
			console.log('🔎 Filtre sélectionné :', selectedRow);

			const filterId = selectedRow.filterId;
			if (!filterId) {
				console.warn('⚠️ Pas de filterId -> impossible de charger les paramètres filtre.');
				this.tables['paramAdminFilter'].dataSource = [];
				return;
			}

			console.log(`🔄 Chargement paramètres filtre ${filterId}...`);
			this.adminFilterParamService.loadAll(filterId);

			return;
		}


		/* ===========================
		   ▶️ 2) Paramètres des PLAUSIS
		   =========================== */
		if (tableKey === 'reglePlausi') {

			this.currentPlausiParamObject = selectedRow;
			console.log('🔎 Plausi sélectionné :', selectedRow);

			const plausiId = selectedRow.plausiId;
			if (!plausiId) {
				console.warn('⚠️ Pas de plausiId -> impossible de charger les paramètres plausi.');
				this.tables['paramReglePlausi'].dataSource = [];
				return;
			}

			console.log(`🔄 Chargement paramètres plausi ${plausiId}...`);
			this.adminPlausiParamService.loadAll(plausiId);

			return;
		}


		/* ===============================
		   ▶️ 3) Paramètres des EXPORTATIONS
		   =============================== */
		if (tableKey === 'adminExploitation') {

			this.currentExploitationParamObject = selectedRow;
			console.log('🔎 Exploitation sélectionnée :', selectedRow);

			const exportId = selectedRow.exportId;
			if (!exportId) {
				console.warn('⚠️ Pas d\'exportId -> impossible de charger les paramètres export.');
				this.tables['paramExploitation'].dataSource = [];
				return;
			}

			console.log(`🔄 Chargement paramètres exploitation ${exportId}...`);
			this.adminExportParamService.loadAll(exportId);

			return;
		}
	}
}
