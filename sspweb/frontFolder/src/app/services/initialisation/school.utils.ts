import { MEB_BUR_ACTIVITY_STATUS, MEB_SYNCHSTATUS, School } from '../../model/School';
import { DatahandlerColumn } from '../../shared/datahandler/datahandler';

export type ActionTypeForSchool =
	| 'export_csv' // BUTTON_EXPORT_CSV - 7
	| 'switch_master' // BUTTON_SWITCH_MASTER - 6
	| 'autorenew' // BUTTON_SYNC_BUR - 5
	| 'done_all' // BUTTON_GET_ALL_BUR - 4
	| 'check' // BUTTON_GET_BUR - 3
	| 'save' // BUTTON_SAVE - 2
	| 'undo'; // BUTTON_UNDO - 1

export function isActionDisabledForSchool(
	actionType: ActionTypeForSchool,
	school: School | null,
	isEV: boolean,
	isMaster: boolean,
	inSync: boolean,
	sync_bur: boolean,
): boolean {
	const hasSelected: boolean = school != null;

	switch (actionType) {
		case 'undo':
		case 'save':
			return !inSync;

		case 'check':
			return !isEV || !isMaster || !hasSelected || !sync_bur;

		case 'done_all':
			return !isEV || !isMaster || !sync_bur;

		case 'autorenew':
			return !isEV || !isMaster;

		case 'switch_master':
			return isMaster;

		case 'export_csv':
			return false;
	}

	return true;
}

/** Enrichit les colonnes éditables en fonction des rôles et du statut de livraison */
export function enrichEditableColumnsForSchool(
	columns: DatahandlerColumn[],
	isConfigDeliveryMaster: boolean = false,
): DatahandlerColumn[] {
	// Colonnes éditables lorsque l'état du synch status Bur est null ou NEW
	const editableOnSyncStatuts = new Set(['deliveryCode']);

	return columns.map((col) => {
		const clonedCol = { ...col };

		// Ne définir editableOnCondition que pour les colonnes concernées
		const isEditableColumn = editableOnSyncStatuts.has(col.key);

		if (isEditableColumn) {
			clonedCol.editableOnCondition = (row: School, rowIndex: number) => {
				// Non-éditable si la configuration des livraisons est master
				if (isConfigDeliveryMaster) return false;
				// Vérifier si la colonne est éditable selon le synchStatus
				return !row.synchStatusBur || row.synchStatusBur === MEB_SYNCHSTATUS.NEW;
			};
		}

		return clonedCol;
	});
}

/**
 * Détermine si une école doit être supprimée lors d'un get_bur.
 *
 * Règle métier du legacy (BurSchoolTableManager#getRowUserData) :
 * une école doit être supprimée si :
 *  - elle n'est pas référencée dans SSP (burIsSsp = false), OU
 *  - son statut d'activité BUR est INACTIVE, DELETED ou TRANSFERRED, OU
 *  - sa date de début de validité BUR est postérieure à l'année de référence, OU
 *  - sa date de fin de validité BUR est antérieure à l'année de référence.
 */
export function shouldDeleteOnGetBur(school: School, version: number): boolean {
	return (
		!school.burIsSsp ||
		school.burActivityStatus === MEB_BUR_ACTIVITY_STATUS.INACTIVE ||
		school.burActivityStatus === MEB_BUR_ACTIVITY_STATUS.DELETED ||
		school.burActivityStatus === MEB_BUR_ACTIVITY_STATUS.TRANSFERRED ||
		(school.burValidFromSsp != null && school.burValidFromSsp > version) ||
		(school.burValidToSsp != null && school.burValidToSsp < version)
	);
}
