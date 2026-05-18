import {MEB_DATASTATUS, Person, Qualification} from "../../model/Maintain";
import {DATAHANDLER_STYLE, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
import {Role} from "../../model/user";

export function calculatePersonCondition(person: Person): string {
	const plausiStatus = person.plausiStatus;
	if (person.status >= MEB_DATASTATUS.VALIDATED) {
		return DATAHANDLER_STYLE.VALIDATED;
	} else if (person.status == MEB_DATASTATUS.IMPORTED) {
		return DATAHANDLER_STYLE.IMPORTED;
	} else if ((plausiStatus == MEB_PLAUSISTATUS.VALID)
		|| (plausiStatus == MEB_PLAUSISTATUS.CONFIRMED)) {
		return DATAHANDLER_STYLE.VALID;
	}
	return DATAHANDLER_STYLE.NOT_VALID;
}

/**
 * Détermine si le bouton Validate doit être désactivé
 * Règles : peut valider si toutes les sélections sont entre DELIVERED et PREVALIDATED
 */
export function disableValidateButton(isMaster: boolean, containsModification: boolean, selectedPersons: Person[], roles: string[]): boolean {
	if (!isMaster || containsModification || selectedPersons.length === 0) {
		return true;
	}

	// Trouver minState et maxState
	const states = selectedPersons.map(p => p.status);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBG_DL)) {
		if (roles.includes(Role.SBG_DV)) {
			// DV ou supérieur (EV)
			return minState < MEB_DATASTATUS.DELIVERED;
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
export function disableUndoValidateButton(isMaster: boolean, containsModification: boolean, selectedPersons: Person[], roles: string[]): boolean {
	if (!isMaster || containsModification || selectedPersons.length === 0) {
		return true;
	}

	if (selectedPersons.length === 0) {
		return true;
	}

	// Trouver minState et maxState
	const states = selectedPersons.map(p => p.status);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBG_DL)) {
		if (roles.includes(Role.SBG_DV)) {
			if (roles.includes(Role.SBG_EV)) {
				// EV (Expert Validator) : peut annuler PREVALIDATED ou VALIDATED
				return minState !== maxState || minState !== MEB_DATASTATUS.VALIDATED;
			}
		}
		// DL n'a pas le droit d'annuler validation
		return true;
	}

	// Read-only : toujours désactivé
	return true;
}

/**
 * Détermine si le bouton Delete doit être désactivé
 * Règles : peut supprimer selon le rôle et l'état des données
 */
export function disableDeleteButtonForPersons(selected: Person[], roles: string[]): boolean {
	if (selected.length === 0) return true;

	const states = selected.map(p => p.status);
	const maxState = Math.max(...states);

	if (roles.includes(Role.SBG_DL)) {
		if (roles.includes(Role.SBG_EV)) {
			return maxState > MEB_DATASTATUS.VALIDATED;   // EV/EA
		} else {
			return maxState > MEB_DATASTATUS.DELIVERED;   // DL only
		}
	}
	return true;
}

/** Enrichit les colonnes avec les valeurs par défaut pour les nouveaux enregistrements */
export function enrichDefaultValuesForPersons(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const defaultValues: Record<string, any> = {
		plausiStatus: 0,
		creationDateString: new Date().toLocaleDateString('fr-CH'),
		modificationDateString: new Date().toLocaleDateString('fr-CH')
	};

	return columns.map(col => {
		const clonedCol = {...col};
		if (col.key in defaultValues) {
			clonedCol.default = defaultValues[col.key];
		}
		return clonedCol;
	});
}
