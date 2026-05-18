import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {PlausiError} from "../shared/plausi-error-editor/plausi-error.model";

export interface Delivery {
	canton: number;
	configDeliveryCode: string;
	confirmRules: string;
	creatingReport: boolean;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryId: number;
	deliveryStatus: number;
	isLocked: number;
	modificationDate: number;
	modificationUser: string;
	nrPlausiPerson: string;
	nrPlausiActivity: string;
	plausiStatus: number;
	prevalidationDate: number;
	prevalidationUser: string;
	userText: string;
	validationDate: number;
	validationUser: string;
	version: number;
	classCondition?: string;
	plausiErrors?: PlausiError[];
}

export interface Intervention {
	canton?: number;
	deliveryId?: number;
	interventionId?: number;
	interventionDate: string;
	interventionUser?: string;
	reportDe?: string;
	reportFr?: string;
	reportIt?: string;
	report?: string;
	text?: string;
	type?: number;
	version?: number;
}

export interface Export {
	id?: string;
	authorisationLevel?: number;
	exportId?: number;
	exportOrder?: number;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
	description?: string;
	isActive?: boolean;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	name?: string;
	parameters?: Parameter[];
	parameter?: string;
	source?: string;
	type?: number;
}

export interface Parameter {
	defaultValue?: string;
	exportId?: number;
	filterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	parameterId?: number;
	parameterOrder?: number;
	plausiId?: number;
	uniqueName?: string;
}

export interface WebFilter {
	filterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
	refObject?: number;
	source?: string;
	authorisationLevel?: number;
	isActive?: boolean;
	isDefault?: boolean;
	filterOrder?: number;
	parameters?: Parameter[];
}

export interface LastFilters {
	version?: number;
	canton?: number;
}

export interface DeliveryParams {
	version?: number;
	canton?: number;
	webFilters?: WebFilter[];
	whereFilters?: WhereFilter[];
}

export const MEB_DELIVERYSTATUS = {
	FINALIZED: 7,
	VALIDATED: 6,
	PREVALIDATED: 5,
	DELIVERED: 4,
	CONFIRMATION: 3,
	AMENDREPLACE: 2,
	IMPORTED: 1,
	INITIALIZED: 0
} as const;

/**
 * Types d'interventions possibles
 */
export const MEB_INTERVENTIONTYPE = {
	IGNORED_SCHOOLS: 17,
	PLAUSIREPORT_IN_CREATION: 16,
	MANUAL: 15,
	DELETE_LAST: 14,
	EMPTY: 13,
	UNDO_FINALIZE: 12,
	FINALIZE: 11,
	UNDO_VALIDATE: 10,
	VALIDATE: 9,
	UNDO_PREVALIDATE: 8,
	PREVALIDATE: 7,
	CREATE_PLAUSIREPORT: 6,
	CONFIRM_DELIVERY: 5,
	CANCEL_DELIVERY: 4,
	REPLACE_DELIVERY: 3,
	AMEND_DELIVERY: 2,
	DELIVERY_WITH_ERRORS: 1,
	DELIVER_FILE: 0,
} as const;

export const MEB_PLAUSISTATUS = {
	UNDEFINED: 0,
	NOT_VALID: 1,
	VALID: 2,
} as const;

export const DATAHANDLER_STYLE = {
	NOT_VALID: 'black',
	FINALIZED: 'gray',
	VALIDATED: 'dark-green',
	PREVALIDATED: 'green',
	VALID: 'blue',
	IMPORTED: 'gray',
	INITIALIZED: 'black',
	ARC: 'red'
} as const;
