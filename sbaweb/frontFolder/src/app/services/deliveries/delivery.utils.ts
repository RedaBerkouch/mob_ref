import {DATAHANDLER_STYLE, Delivery, MEB_DELIVERYSTATUS, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {Role} from "../../model/user";


export function calculateDeliveryCondition(delivery: Delivery): string {
	const deliveryStatus = delivery.deliveryStatus;
	const plausiStatus = delivery.plausiStatus;

	if (deliveryStatus == null) {
		return DATAHANDLER_STYLE.NOT_VALID;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.FINALIZED) {
		return DATAHANDLER_STYLE.FINALIZED;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.VALIDATED) {
		return DATAHANDLER_STYLE.VALIDATED;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return DATAHANDLER_STYLE.PREVALIDATED;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.DELIVERED) {
		if (plausiStatus != null && plausiStatus >= MEB_PLAUSISTATUS.VALID) {
			return DATAHANDLER_STYLE.VALID;
		}
		return DATAHANDLER_STYLE.NOT_VALID;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.IMPORTED) {
		return DATAHANDLER_STYLE.IMPORTED;
	}
	if (deliveryStatus === MEB_DELIVERYSTATUS.INITIALIZED) {
		return DATAHANDLER_STYLE.INITIALIZED;
	}
	return DATAHANDLER_STYLE.ARC;
}

export type ActionTypeForDelivery =
	| 'export'           // BUTTON_EXPORT_CSV (11)
	| 'amend'            // BUTTON_AMEND (10)
	| 'replace'          // BUTTON_REPLACE (9)
	| 'confirm'          // BUTTON_CONFIRM (8)
	| 'cancel'           // BUTTON_CANCEL (7)
	| 'prevalidate'      // BUTTON_VALIDATE (6)
	| 'undo_validate'    // BUTTON_UNDO_VALIDATE (5)
	| 'create_plausi_report' // BUTTON_CREATE_PLAUSIREPORT (4)
	| 'show_plausi_report'   // BUTTON_SHOW_PLAUSIREPORT (3)
	| 'save'             // BUTTON_SAVE (2)
	| 'delete';          // BUTTON_DELETE (1)

/**
 * Détermine si une action doit être désactivée pour une livraison donnée.
 * Basé sur l'état de la livraison, les rôles utilisateur, et l'état de synchronisation.
 *
 * @param actionType - Type d'action à évaluer
 * @param delivery - Livraison sélectionnée (null si aucune)
 * @param roles - Liste des rôles de l'utilisateur
 * @param inSync - État de synchronisation (true = sauvegardé, false = modifié)
 * @returns true si l'action doit être désactivée, false si active
 */
export function isActionDisabledForDelivery(
	actionType: ActionTypeForDelivery,
	delivery: Delivery | null,
	roles: string[],
	inSync: boolean
): boolean {
	// Sans sélection : seul export est disponible
	if (!delivery) {
		return actionType !== 'export';
	}

	const state = delivery.deliveryStatus;
	const plausiUndef = !delivery.plausiStatus;

	// Extraction des rôles
	const {hasDL, hasDV, hasEV, hasEA} = extractRoles(roles);

	// Sans rôle DL : accès limité
	if (!hasDL) {
		return evaluateWithoutDLRole(actionType, state);
	}

	// Avec rôle DL : évaluation complète
	return evaluateActionWithDLRole(actionType, state, plausiUndef, inSync, hasDV, hasEV, hasEA);
}

/**
 * Extrait les rôles de l'utilisateur
 */
function extractRoles(roles: string[]) {
	return {
		hasDL: roles.includes(Role.SBA_DL),
		hasDV: roles.includes(Role.SBA_DV),
		hasEV: roles.includes(Role.SBA_EV),
		hasEA: roles.includes(Role.SBA_EA)
	};
}

/**
 * Évalue les actions disponibles sans le rôle DL
 */
function evaluateWithoutDLRole(actionType: ActionTypeForDelivery, state: number): boolean {
	if (actionType === 'export') {
		return false; // Toujours actif
	}

	if (actionType === 'show_plausi_report') {
		return state < MEB_DELIVERYSTATUS.CONFIRMATION; // Actif si >= CONFIRMATION
	}

	return true; // Tous les autres désactivés
}

/**
 * Évalue les actions avec le rôle DL
 */
function evaluateActionWithDLRole(
	actionType: ActionTypeForDelivery,
	state: number,
	plausiUndef: boolean,
	inSync: boolean,
	hasDV: boolean,
	hasEV: boolean,
	hasEA: boolean
): boolean {
	switch (actionType) {
		case 'export':
			return false; // Toujours actif

		case 'delete':
			return isDeleteDisabled(state, hasDV, hasEV, hasEA);

		case 'save':
			return isSaveDisabled(state, inSync, hasDV, hasEV);

		case 'prevalidate':
			return isPrevalidateDisabled(state, plausiUndef, hasDV);

		case 'undo_validate':
			return isUndoValidateDisabled(state, hasDV, hasEV);

		case 'create_plausi_report':
			return isCreatePlausiReportDisabled(state, hasDV, hasEV);

		case 'show_plausi_report':
			return state < MEB_DELIVERYSTATUS.CONFIRMATION;

		case 'confirm':
			return state !== MEB_DELIVERYSTATUS.CONFIRMATION;

		case 'cancel':
			return !isStateAmendReplaceOrConfirmation(state);

		case 'amend':
		case 'replace':
			return state !== MEB_DELIVERYSTATUS.AMENDREPLACE;

		default:
			return true;
	}
}

/**
 * Détermine si DELETE est désactivé
 */
function isDeleteDisabled(state: number, hasDV: boolean, hasEV: boolean, hasEA: boolean): boolean {
	// DL : IMPORTED ou DELIVERED
	if (state === MEB_DELIVERYSTATUS.IMPORTED || state === MEB_DELIVERYSTATUS.DELIVERED) {
		return false;
	}

	// DV : PREVALIDATED
	if (hasDV && state === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return false;
	}

	// EV : IMPORTED à VALIDATED
	if (hasEV && isStateInRange(state, MEB_DELIVERYSTATUS.IMPORTED, MEB_DELIVERYSTATUS.VALIDATED)) {
		return false;
	}

	// EA : INITIALIZED
	if (hasEA && state === MEB_DELIVERYSTATUS.INITIALIZED) {
		return false;
	}

	return true;
}

/**
 * Détermine si SAVE est désactivé
 */
function isSaveDisabled(state: number, inSync: boolean, hasDV: boolean, hasEV: boolean): boolean {
	// DL : DELIVERED
	if (state === MEB_DELIVERYSTATUS.DELIVERED) {
		return inSync; // Actif si pas en sync
	}

	// DV : PREVALIDATED
	if (hasDV && state === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return inSync;
	}

	// EV : VALIDATED
	if (hasEV && state === MEB_DELIVERYSTATUS.VALIDATED) {
		return inSync;
	}

	return true;
}

/**
 * Détermine si PREVALIDATE est désactivé
 */
function isPrevalidateDisabled(state: number, plausiUndef: boolean, hasDV: boolean): boolean {
	// DL : DELIVERED (si plausi défini)
	if (state === MEB_DELIVERYSTATUS.DELIVERED) {
		return plausiUndef; // Actif si plausi défini
	}

	// DV : PREVALIDATED
	if (hasDV && state === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return false;
	}

	return true;
}

/**
 * Détermine si UNDO_VALIDATE est désactivé
 */
function isUndoValidateDisabled(state: number, hasDV: boolean, hasEV: boolean): boolean {
	// DV : PREVALIDATED
	if (hasDV && state === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return false;
	}

	// EV : VALIDATED
	if (hasEV && state === MEB_DELIVERYSTATUS.VALIDATED) {
		return false;
	}

	return true;
}

/**
 * Détermine si CREATE_PLAUSI_REPORT est désactivé
 */
function isCreatePlausiReportDisabled(state: number, hasDV: boolean, hasEV: boolean): boolean {
	// DL : CONFIRMATION ou DELIVERED
	if (state === MEB_DELIVERYSTATUS.CONFIRMATION || state === MEB_DELIVERYSTATUS.DELIVERED) {
		return false;
	}

	// DV : PREVALIDATED
	if (hasDV && state === MEB_DELIVERYSTATUS.PREVALIDATED) {
		return false;
	}

	// EV : VALIDATED
	if (hasEV && state === MEB_DELIVERYSTATUS.VALIDATED) {
		return false;
	}

	return true;
}

/**
 * Vérifie si l'état est AMENDREPLACE ou CONFIRMATION
 */
function isStateAmendReplaceOrConfirmation(state: number): boolean {
	return state === MEB_DELIVERYSTATUS.AMENDREPLACE || state === MEB_DELIVERYSTATUS.CONFIRMATION;
}

/**
 * Vérifie si l'état est dans une plage donnée (inclus)
 */
function isStateInRange(state: number, min: number, max: number): boolean {
	return state >= min && state <= max;
}
