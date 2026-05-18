import {Intervention, MEB_INTERVENTIONTYPE} from "../../model/Delivery";
import {Role} from "../../model/user";

/**
 * Types d'actions disponibles sur une intervention
 */
export type ActionTypeForLivraisons =
	| 'export'
	| 'show_plausi_report'   // Afficher rapport de plausibilité (BUTTON_SHOW_PLAUSIREPORT - 6)
	| 'show_delivery'        // Afficher la livraison (BUTTON_SHOW_DELIVERY - 5)
	| 'save'                 // Enregistrer (BUTTON_SAVE - 4)
	| 'block'                 // Annuler (BUTTON_UNDO - 3)
	| 'add'               		// Insérer (BUTTON_INSERT - 2)
	| 'delete';              // Supprimer (BUTTON_DELETE - 1)

/**
 * Détermine si une action doit être désactivée pour une intervention donnée.
 *
 * @param actionType - Type d'action à évaluer
 * @param intervention - Intervention sélectionnée (null si aucune sélection)
 * @param roles - Liste des rôles de l'utilisateur connecté
 * @param inSync - État de synchronisation (true = sauvegardé, false = modifié)
 * @returns true si l'action doit être désactivée (bouton grisé), false si active
 *
 * @example
 * ```typescript
 * const intervention = { type: MEB_INTERVENTIONTYPE.CREATE_PLAUSIREPORT };
 * const isDisabled = isActionDisabledForHistory(
 *     'show_plausi_report',
 *     intervention,
 *     ['BFS-MEB.SBG_DL'],
 *     true
 * );
 * // → false (bouton actif car type = CREATE_PLAUSIREPORT)
 * ```
 */
export function isActionDisabledForIntervention(
	actionType: ActionTypeForLivraisons,
	intervention: Intervention | null,
	roles: string[],
	inSync: boolean
): boolean {
	// Sans sélection : toutes les actions désactivées sauf insert (si DL)
	if (!intervention) {
		return evaluateWithoutSelection(actionType, roles, inSync);
	}

	const type = intervention.type;
	const hasDL = hasDeliveryRole(roles);

	// Évaluation selon le type d'action
	return evaluateActionForHistory(actionType, type, hasDL, inSync);
}

/**
 * Évalue les actions disponibles sans sélection d'intervention
 */
function evaluateWithoutSelection(
	actionType: ActionTypeForLivraisons,
	roles: string[],
	inSync: boolean
): boolean {
	const hasDL = hasDeliveryRole(roles);

	if (!hasDL) {
		return true; // Tout désactivé sans rôle DL
	}

	// Avec rôle DL : save, insert, undo disponibles selon inSync
	switch (actionType) {
		case 'save':
		case 'block':
			return inSync; // Actif si pas en sync

		case 'export':
		case 'add':
			return false; // Toujours actif avec DL

		default:
			return true; // Autres désactivés
	}
}

/**
 * Évalue les actions pour une intervention sélectionnée
 */
function evaluateActionForHistory(
	actionType: ActionTypeForLivraisons,
	type: number | undefined,
	hasDL: boolean,
	inSync: boolean
): boolean {
	switch (actionType) {
		case 'show_plausi_report':
			return isShowPlausiReportDisabled(type);

		case 'show_delivery':
			return isShowDeliveryDisabled(type);

		case 'save':
		case 'block':
			return isSaveOrUndoDisabled(hasDL, inSync);

		case 'export':
			return isInsertDisabled(hasDL);

		default:
			return true;
	}
}

/**
 * Règles pour l'action SHOW_PLAUSI_REPORT
 * Actif uniquement si type = CREATE_PLAUSIREPORT
 */
function isShowPlausiReportDisabled(type: number | undefined): boolean {
	return type !== MEB_INTERVENTIONTYPE.CREATE_PLAUSIREPORT;
}

/**
 * Règles pour l'action SHOW_DELIVERY
 * Actif si type = DELIVER_FILE ou DELIVERY_WITH_ERRORS
 */
function isShowDeliveryDisabled(type: number | undefined): boolean {
	return !(type === MEB_INTERVENTIONTYPE.DELIVER_FILE ||
		type === MEB_INTERVENTIONTYPE.DELIVERY_WITH_ERRORS);
}

/**
 * Règles pour l'action DELETE
 * Actif uniquement pour les interventions UPLOAD avec rôle DL
 */
function isDeleteDisabled(type: number | undefined, hasDL: boolean): boolean {
	if (!hasDL) {
		return true; // Désactivé sans rôle DL
	}

	return type !== MEB_INTERVENTIONTYPE.UPLOAD; // Actif uniquement si MANUAL
}

/**
 * Règles pour les actions SAVE et UNDO
 * Actives uniquement avec rôle DL et si pas en sync
 */
function isSaveOrUndoDisabled(hasDL: boolean, inSync: boolean): boolean {
	if (!hasDL) {
		return true; // Désactivé sans rôle DL
	}

	return inSync; // Actif si pas en sync (inSync = false)
}

/**
 * Règles pour l'action INSERT
 * Active uniquement avec rôle DL
 */
function isInsertDisabled(hasDL: boolean): boolean {
	return !hasDL; // Actif si DL
}

/**
 * Vérifie si l'utilisateur a un rôle de délivreur (DL)
 */
function hasDeliveryRole(roles: string[]): boolean {
	return roles.includes(Role.SDL_DL) ||
		roles.includes(Role.SSP_DL) ||
		roles.includes(Role.SBG_DL) ||
		roles.includes(Role.SBA_DL);
}
