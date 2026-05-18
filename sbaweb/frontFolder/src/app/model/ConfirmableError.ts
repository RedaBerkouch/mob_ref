export interface ConfirmableError {

	// ===== IDENTIFIANTS TECHNIQUES =====
	cantonId: number | null;
	deliveryId: number;
	errorId: number;
	qualificationId: number | null;
	confirmId: number | null;

	// ===== FLAGS MÉTIER =====
	confirmable: boolean;
	isConfirmed: boolean;
	isToDelete: boolean;

	// ===== PERSONNE =====
	personId: number;
	personLabel: string;           // ex: "CT.OW: 1"

	// ===== QUALIFICATION =====
	qualificationLabel: string;    // ex: "3"

	// ===== ÉCOLE =====
	schoolLabel: string;           // ex: "CH.BUR: 65111067 - Schulhaus Stuckli, Sachseln"

	// ===== ERREURS / PLAUSI =====
	errorMsgDe: string;
	errorMsgFr: string;
	errorMsgIt: string;

	plausiNameDe: string;
	plausiNameFr: string;
	plausiNameIt: string;

	// ===== AFFICHAGE SIMPLIFIÉ (UI) =====
	reportData: any | null;

	// ===== AUDIT =====
	modificationDate: number;      // timestamp
	modificationUser: string;
}
