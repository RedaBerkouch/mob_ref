import {MEB_DATASTATUS, Person, Qualification} from "../../model/Maintain";
import {DATAHANDLER_STYLE, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
import {Role} from "../../model/user";

export function calculateQualificationCondition(qualification: Qualification): string {
	if (qualification.isValidated) {
		return DATAHANDLER_STYLE.FINALIZED;
	} else if (qualification.plausiStatus == MEB_DATASTATUS.IMPORTED) {
		return DATAHANDLER_STYLE.IMPORTED;
	} else if ((qualification.plausiStatus == MEB_PLAUSISTATUS.VALID)
		|| (qualification.plausiStatus == MEB_PLAUSISTATUS.CONFIRMED)) {
		return DATAHANDLER_STYLE.VALID;
	}
	return DATAHANDLER_STYLE.NOT_VALID;
}

/** Enrichit les colonnes éditables en fonction des rôles et du statut de livraison */
export function enrichEditableColumnsForQualifications(columns: DatahandlerColumn[], roles: string[]): DatahandlerColumn[] {
	// Colonnes éditables lorsque l'état est PREVALIDATED
	const editableOnPrevalidated = new Set([
		'schoolIdType', 'schoolId', 'educationType', 'examType', 'examDateString', 'examNr',
		'result', 'maturityLanguages', 'userText'
	]);

	return columns.map(col => {
		const clonedCol = {...col};

		// Ne définir editableOnCondition que pour les colonnes concernées
		const isEditableColumn = editableOnPrevalidated.has(col.key);

		if (isEditableColumn) {
			clonedCol.editableOnCondition = (row: Person, rowIndex: number) => hasEditRightsforColumns(row.status, roles);
		}

		return clonedCol;
	});
}

/** Vérifie si l'utilisateur a les droits d'édition selon le statut de livraison */
function hasEditRightsforColumns(deliveryStatus: number | null, roles: string[]): boolean {
	// Si pas de statut, seul SBG_DV peut éditer
	if (deliveryStatus == null) {
		return roles.includes(Role.SBG_DV);
	}

	switch (deliveryStatus) {
		case MEB_DATASTATUS.VALIDATED:
			return roles.includes(Role.SBG_EV);
		case MEB_DATASTATUS.DELIVERED:
			return roles.includes(Role.SBG_DL);
		default:
			return false;
	}
}

/**
 * Détermine si le bouton Delete doit être désactivé
 * Règles : peut supprimer selon le rôle et l'état des données
 */
export function disableDeleteButtonForQualifications(selected: Qualification[], containsNew: boolean, roles: string[]): boolean {
	if (selected.length === 0) {
		return true;
	}

	if (containsNew) {
		return false;
	}

	// Trouver minState
	const states = selected.map(p => p.plausiStatus);
	const minState = Math.min(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBG_DL) && roles.includes(Role.SBG_DV)) {
			return minState >= MEB_DATASTATUS.VALIDATED;
	}

	// Read-only : toujours désactivé
	return true;
}
