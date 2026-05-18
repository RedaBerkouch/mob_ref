import {Canton, MEB_CANTONSTATUS} from "../../model/Canton";
import {DATAHANDLER_STYLE, MEB_DELIVERYSTATUS, MEB_PLAUSISTATUS} from "../../model/Delivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
export type ActionTypeForCanton =
	| 'export_csv'              // BUTTON_EXPORT_CSV - 11
	| 'finalize'                // BUTTON_FINALIZE - 10
	| 'undo_finalize'           // BUTTON_UNDO_FINALIZE - 9
	| 'validate'                // BUTTON_VALIDATE - 8
	| 'undo_validate'           // BUTTON_UNDO_VALIDATE - 7
	| 'create_plausi_report'    // BUTTON_CREATE_PLAUSIREPORT - 6
	| 'show_last_plausi_report' // BUTTON_SHOW_LAST_PLAUSIREPORT - 5
	| 'save'                    // BUTTON_SAVE - 4
	| 'undo'                    // BUTTON_UNDO - 3
	| 'insert'             	    // BUTTON_INSERT - 2
	| 'delete';                 // BUTTON_DELETE - 1

export function calculateCantonRowStyle(canton: Canton): string {
	const deliveryStatus = canton.deliveryStatus;
	const plausiStatus = canton.plausiStatus;

	if (deliveryStatus == MEB_CANTONSTATUS.FINALIZED) {
		return DATAHANDLER_STYLE.FINALIZED;
	}
	else if (deliveryStatus === MEB_DELIVERYSTATUS.VALIDATED) {
		return DATAHANDLER_STYLE.VALIDATED;
	}
	else if (deliveryStatus === MEB_DELIVERYSTATUS.DELIVERED &&
		plausiStatus === MEB_PLAUSISTATUS.VALID) {
		return DATAHANDLER_STYLE.VALID;
	}

	return DATAHANDLER_STYLE.NOT_VALID;
}

export function enrichEditableOnConditionForCanton(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const editableOnNewRecord = new Set([
		'canton', 'plausiStatus'
	]);

	return columns.map(col => {
		const clonedCol = {...col};

		// Ne d�finir editableOnCondition que pour les colonnes concern�es
		const isEditableColumn = editableOnNewRecord.has(col.key);

		if (isEditableColumn) {
			clonedCol.editableOnCondition = (row: Canton, rowIndex: number) => {
				// 1. Nouvelle ligne
				return (row as any).isNew === true;
			};
		}

		return clonedCol;
	});
}

/** Enrichit les colonnes avec les valeurs par d?faut pour les nouveaux enregistrements */
export function enrichDefaultValuesForCanton(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const defaultValues: Record<string, any> = {
		creationDateString: new Date().toLocaleDateString('fr-CH'),
		modificationDateString: new Date().toLocaleDateString('fr-CH'),
		plausiStatus: MEB_PLAUSISTATUS.UNDEFINED
	};

	return columns.map(col => {
		const clonedCol = {...col};
		if (col.key in defaultValues) {
			clonedCol.default = defaultValues[col.key];
		}
		return clonedCol;
	});
}

export function hasMissingCantons(misscantons: number[], currentCantonFiltered: number | null): boolean {
	return actualMissingCantons(misscantons, currentCantonFiltered).length > 0;
}

export function actualMissingCantons(missingCantons: number[], currentCantonFiltered: number | null): number[] {
	if (currentCantonFiltered) {
		return missingCantons.includes(currentCantonFiltered) ? [currentCantonFiltered] : [];
	}
	else {
		return missingCantons;
	}
}
