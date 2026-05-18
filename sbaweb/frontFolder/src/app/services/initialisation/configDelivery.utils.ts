import {ConfigDelivery} from "../../model/ConfigDelivery";
import {DatahandlerColumn} from "../../shared/datahandler/datahandler";
import {convertAsIncrementedParameters} from "../../utils/parameter.util";
import {WhereFilter} from "../../shared/pre-advance-filter/pre-advance-filter";

export type ActionTypeForConfigDelivery =
	| 'export_csv'    // BUTTON_EXPORT_CSV - 6
	| 'switch_master' // BUTTON_SWITCH_MASTER - 5
	| 'save'          // BUTTON_SAVE - 4
	| 'undo'          // BUTTON_UNDO - 3
	| 'new'           // BUTTON_NEW - 2
	| 'delete'

	| 'insert';       // BUTTON_INSERT ?

export function isActionDisabledForConfigDelivery(
	actionType: ActionTypeForConfigDelivery,
	configDelivery: ConfigDelivery | null,
	isEA: boolean,
	isMaster: boolean,
	inSync: boolean
): boolean {
	const hasSelected: boolean = configDelivery != null;

	switch (actionType) {
		case "delete":
			return !hasSelected || isEA;

		case "insert":
			return false;

		case "new":
			return !isMaster || !isEA

		case "undo":
		case "save":
			return !inSync;

		case "switch_master":
			return isMaster;

		case "export_csv":
			return false;
	}

	return true;
}

/** Enrichit les colonnes avec les valeurs par d�faut pour les nouveaux enregistrements */
export function enrichDefaultValuesForConfigDelivery(columns: DatahandlerColumn[]): DatahandlerColumn[] {
	const defaultValues: Record<string, any> = {
		dueDateDate: new Date(),
		referenceDateDate: new Date(),
		creationDateString: new Date().toLocaleDateString('fr-CH'),
		modificationDateString: new Date().toLocaleDateString('fr-CH'),
		roUsersParameters: convertAsIncrementedParameters(";;;", "RO", 4),
		dlUsersParameters: convertAsIncrementedParameters(";;;", "DL", 4)
	};

	return columns.map(col => {
		const clonedCol = {...col};
		if (col.key in defaultValues) {
			clonedCol.default = defaultValues[col.key];
		}
		return clonedCol;
	});
}

/** pour les champs 'dlUsersParameters' et 'roUsersParameters', lors des requêtes avec 'WhereFilter', il faut le faire sans le suffixe 'Parameters'*/
const USERS_PARAMETERS_SUFFIX = 'Parameters' as const;
const USERS_PARAMETERS_ATTRIBUTES = ['dlUsersParameters', 'roUsersParameters'] as const;

type UsersParametersAttribute = typeof USERS_PARAMETERS_ATTRIBUTES[number];

export function normalizeWhereFilterAttribute(filter: WhereFilter): WhereFilter {
	if ((USERS_PARAMETERS_ATTRIBUTES as readonly string[]).includes(filter.attribute)) {
		return {
			...filter,
			attribute: (filter.attribute as UsersParametersAttribute).replace(USERS_PARAMETERS_SUFFIX, ''),
		};
	}
	return filter;
}
