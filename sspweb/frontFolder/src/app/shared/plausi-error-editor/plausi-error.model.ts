/**
 * Interface pour une erreur de plausibilité
 */
export interface PlausiError {
	cantonId: number | null;
	confirmId: string | null;
	confirmable: boolean;
	deliveryId: number;
	errorId: number;
	errorMsgDe: string;
	errorMsgFr: string;
	errorMsgIt: string;
	isConfirmed: boolean;
	isToDelete: boolean;
	modificationDate: number;
	modificationDateString: string;
	modificationUser: string;
	plausiNameDe: string;
	plausiNameFr: string;
	plausiNameIt: string;
	reportData: string | null;
}

/**
 * Configuration pour le champ plausi-error
 */
export interface PlausiConfig {
	errorArrayKey: string;      // Clé du champ PlausiError[] dans le FormGroup
	statusKey: string;           // Clé du champ status dans le FormGroup
	editableStatusValue: number[]; // Valeurs permettant l'édition
	tooltip?: string;
}

/**
 * Option pour le select de statut
 */
export interface PlausiStatusOption {
	value: number;
	label: string;
	name?: string;
}
