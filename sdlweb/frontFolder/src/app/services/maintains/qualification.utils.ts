import {Learner, MEB_DATASTATUS, Person, Qualification} from "../../model/Maintain";
import {DATAHANDLER_STYLE, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
import {Role} from "../../model/user";

export function calculateQualificationCondition(qualification: Qualification): string {
	const deliveryStatus = qualification.deliveryStatus;
	const plausiStatus = qualification.plausiStatus;

	if (deliveryStatus === MEB_DATASTATUS.FINALIZED) {
		return DATAHANDLER_STYLE.FINALIZED;
	}
	if (deliveryStatus === MEB_DATASTATUS.VALIDATED) {
		return DATAHANDLER_STYLE.VALIDATED;
	}
	if (deliveryStatus === MEB_DATASTATUS.PREVALIDATED) {
		return DATAHANDLER_STYLE.PREVALIDATED;
	}
	if (deliveryStatus === MEB_DATASTATUS.IMPORTED) {
		return DATAHANDLER_STYLE.IMPORTED;
	}
	if (plausiStatus === MEB_PLAUSISTATUS.VALID) {
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
			clonedCol.editableOnCondition = (row: Person, rowIndex: number) => hasEditRightsforColumns(row.deliveryStatus, roles);
		}

		return clonedCol;
	});
}

/** Vérifie si l'utilisateur a les droits d'édition selon le statut de livraison */
function hasEditRightsforColumns(deliveryStatus: number | null, roles: string[]): boolean {
	// Si pas de statut, seul SDL_DV peut éditer
	if (deliveryStatus == null) {
		return roles.includes(Role.SDL_DV);
	}

	switch (deliveryStatus) {
		case MEB_DATASTATUS.VALIDATED:
			return roles.includes(Role.SDL_EV);
		case MEB_DATASTATUS.PREVALIDATED:
			return roles.includes(Role.SDL_DV);
		case MEB_DATASTATUS.DELIVERED:
			return roles.includes(Role.SDL_DL);
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
	const states = selected.map(p => p.deliveryStatus);
	const minState = Math.min(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SDL_DL)) {
		if (roles.includes(Role.SDL_DV)) {
			if (roles.includes(Role.SDL_EV)) {
				// EV : peut supprimer si pas encore FINALIZED
				return minState >= MEB_DATASTATUS.FINALIZED;
			} else {
				// DV : peut supprimer si pas encore VALIDATED
				return minState >= MEB_DATASTATUS.VALIDATED;
			}
		} else {
			// DL : peut supprimer si pas encore PREVALIDATED
			return minState >= MEB_DATASTATUS.PREVALIDATED;
		}
	}

	// Read-only : toujours désactivé
	return true;
}



/**
 * Détermine si le bouton Validate doit être désactivé
 * Règles : peut valider si toutes les sélections sont entre DELIVERED et PREVALIDATED
 */
export function disableValidateButtonForQualifications(isMaster: boolean, containsModification: boolean, selectedQualifications: Qualification[], roles: string[]): boolean {
	if (!isMaster || containsModification || selectedQualifications.length === 0) {
		return true;
	}

	// Trouver minState et maxState
	const states = selectedQualifications.map(p => p.deliveryStatus);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SDL_DL)) {
		if (roles.includes(Role.SDL_DV)) {
			// DV ou supérieur (EV)
			return minState < MEB_DATASTATUS.DELIVERED || maxState > MEB_DATASTATUS.PREVALIDATED;
		} else {
			// DL uniquement
			return minState < MEB_DATASTATUS.DELIVERED || maxState > MEB_DATASTATUS.DELIVERED;
		}
	}
	// Read-only : toujours désactivé
	return true;
}

/**
 * Détermine si le bouton Undo Validate doit être désactivé
 * Règles : peut annuler validation selon le rôle et l'état des données
 */
export function disableUndoValidateButtonForQualifications(isMaster: boolean, containsModification: boolean, selectedQualifications: Qualification[], roles: string[]): boolean {
	if (!isMaster || containsModification || selectedQualifications.length === 0) {
		return true;
	}

	if (selectedQualifications.length === 0) {
		return true;
	}

	// Trouver minState et maxState
	const states = selectedQualifications.map(p => p.deliveryStatus);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SDL_DL)) {
		if (roles.includes(Role.SDL_DV)) {
			if (roles.includes(Role.SDL_EV)) {
				// EV (Expert Validator) : peut annuler PREVALIDATED ou VALIDATED
				return minState !== maxState ||
					(minState !== MEB_DATASTATUS.PREVALIDATED && minState !== MEB_DATASTATUS.VALIDATED);
			} else {
				// DV (Data Validator) : peut annuler uniquement PREVALIDATED
				return minState < MEB_DATASTATUS.PREVALIDATED || maxState > MEB_DATASTATUS.PREVALIDATED;
			}
		}
		// DL n'a pas le droit d'annuler validation
		return true;
	}

	// Read-only : toujours désactivé
	return true;
}
