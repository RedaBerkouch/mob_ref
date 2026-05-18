import {CantonIntervention, SSP_CANTONINTERVENTIONTYPE} from "../../model/CantonIntervention";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";

export type ActionTypeForCantonIntervention =
	| 'export_csv'         // BUTTON_EXPORT_CSV - 6
	| 'show_plausi_report' // BUTTON_SHOW_PLAUSIREPORT - 5
	| 'save'               // BUTTON_SAVE - 4
	| 'undo'               // BUTTON_UNDO - 3
	| 'insert'             // BUTTON_INSERT - 2
	| 'delete';            // BUTTON_DELETE - 1

export function isActionDisabledForCantonIntervention(
	actionType: ActionTypeForCantonIntervention,
	cantonIntervention: CantonIntervention | null,
	isDL: boolean,
	inSync: boolean
): boolean {
	const hasSelected: boolean = cantonIntervention != null;
	const type: number = hasSelected && cantonIntervention?.type ? cantonIntervention.type : Number.MAX_VALUE;

	switch (actionType) {
		case "delete":
			return !hasSelected || isDL && type < SSP_CANTONINTERVENTIONTYPE.MANUAL;

		case "insert":
			return !hasSelected || !isDL

		case "undo":
		case "save":
			return !hasSelected || isDL && !inSync;

		case "show_plausi_report":
			return !hasSelected || type != SSP_CANTONINTERVENTIONTYPE.CREATE_PLAUSIREPORT;

		case "export_csv":
			return false;
	}

	return true;
}

export function enrichEditableOnConditionForCantonIntervention(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const editableOnNewRecord = new Set([
		'type', 'text'
	]);

	return columns.map(col => {
		const clonedCol = {...col};

		// Ne d�finir editableOnCondition que pour les colonnes concern�es
		const isEditableColumn = editableOnNewRecord.has(col.key);

		if (isEditableColumn) {
			clonedCol.editableOnCondition = (row: CantonIntervention, rowIndex: number) => {
				// 1. Nouvelle ligne
				return (row as any).isNew === true;
			};
		}

		return clonedCol;
	});
}

/** Enrichit les colonnes avec les valeurs par d�faut pour les nouveaux enregistrements */
export function enrichDefaultValuesForCantonIntervention(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const defaultValues: Record<string, any> = {
		creationDateString: new Date().toLocaleDateString('fr-CH'),
		modificationDateString: new Date().toLocaleDateString('fr-CH'),
		type: SSP_CANTONINTERVENTIONTYPE.MANUAL
	};

	return columns.map(col => {
		const clonedCol = {...col};
		if (col.key in defaultValues) {
			clonedCol.default = defaultValues[col.key];
		}
		return clonedCol;
	});
}
