import {MEB_DATASTATUS, Person, Qualification} from "../../model/Maintain";
import {DATAHANDLER_STYLE, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
import {Role} from "../../model/user";

export function calculatePersonCondition(person: Person): string {
	const deliveryStatus = person.deliveryStatus;
	const plausiStatus = person.plausiStatus;

	if (deliveryStatus == null) {
		return DATAHANDLER_STYLE.NOT_VALID;
	}
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
export function enrichEditableColumnsForPersons(columns: DatahandlerColumn[], roles: string[]): DatahandlerColumn[] {
	// Colonnes éditables lorsque l'état est PREVALIDATED
	const editableOnPrevalidated = new Set([
		'version', 'canton', 'deliveryCode', 'idType', 'id', 'sex',
		'birthdateString', 'residence', 'historicResidence', 'country',
		'plausiStatus', 'userText'
	]);

	// Colonnes éditables lorsque l'état n'est PAS PREVALIDATED
	const editableOnNotPrevalidated = new Set([
		'version', 'canton', 'deliveryCode', 'idType', 'id', 'sex',
		'birthdateString', 'residence', 'historicResidence', 'country',
		'deliveryStatus', 'userText'
	]);

	return columns.map(col => {
		const clonedCol = {...col};

		// Ne définir editableOnCondition que pour les colonnes concernées
		const isEditableColumn = editableOnPrevalidated.has(col.key) || editableOnNotPrevalidated.has(col.key);

		if (isEditableColumn) {
			clonedCol.editableOnCondition = (row: Person, rowIndex: number) => {
				// 1. Nouvelle ligne
				if ((row as any).isNew === true) {
					return true;
				}

				// 2. Vérifier si l'utilisateur a les droits selon le deliveryStatus
				const hasEditRights = hasEditRightsforColumns(row.deliveryStatus, roles);
				if (!hasEditRights) {
					return false;
				}

				// 3. Vérifier si la colonne est éditable selon le deliveryStatus
				const isPrevalidated = row.deliveryStatus === MEB_DATASTATUS.PREVALIDATED;
				// Si status prévalider
				return (isPrevalidated && editableOnPrevalidated.has(col.key))
					// Si status non prévalider
					|| editableOnNotPrevalidated.has(col.key);
			};
		}

		return clonedCol;
	});
}

/** Vérifie si l'utilisateur a les droits d'édition selon le statut de livraison */
function hasEditRightsforColumns(deliveryStatus: number | null, roles: string[]): boolean {
	// Si pas de statut, seul SBA_DV peut éditer
	if (deliveryStatus == null) {
		return roles.includes(Role.SBA_DV);
	}

	switch (deliveryStatus) {
		case MEB_DATASTATUS.VALIDATED:
			return roles.includes(Role.SBA_EV);
		case MEB_DATASTATUS.PREVALIDATED:
			return roles.includes(Role.SBA_DV);
		case MEB_DATASTATUS.DELIVERED:
			return roles.includes(Role.SBA_DL);
		default:
			return false;
	}
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
	const states = selectedPersons.map(p => p.deliveryStatus);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBA_DL)) {
		if (roles.includes(Role.SBA_DV)) {
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
export function disableUndoValidateButton(isMaster: boolean, containsModification: boolean, selectedPersons: Person[], roles: string[]): boolean {
	if (!isMaster || containsModification || selectedPersons.length === 0) {
		return true;
	}

	if (selectedPersons.length === 0) {
		return true;
	}

	// Trouver minState et maxState
	const states = selectedPersons.map(p => p.deliveryStatus);
	const minState = Math.min(...states);
	const maxState = Math.max(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBA_DL)) {
		if (roles.includes(Role.SBA_DV)) {
			if (roles.includes(Role.SBA_EV)) {
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

/**
 * Détermine si le bouton Delete doit être désactivé
 * Règles : peut supprimer selon le rôle et l'état des données
 */
export function disableDeleteButtonForPersons(selected: Person[] | Qualification[], roles: string[]): boolean {
	if (selected.length === 0) {
		return true;
	}

	// Trouver minState
	const states = selected.map(p => p.deliveryStatus);
	const minState = Math.min(...states);

	// Logique selon les rôles
	if (roles.includes(Role.SBA_DL)) {
		if (roles.includes(Role.SBA_DV)) {
			if (roles.includes(Role.SBA_EV)) {
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
