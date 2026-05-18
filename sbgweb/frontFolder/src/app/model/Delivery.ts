import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {PlausiError} from "../shared/plausi-error-editor/plausi-error.model";

export interface Delivery {
	deliveryid: number;
	canton: number;
	creatingReport: boolean;
	deliverydate: number;
	deliveryuser: string;
	isLocked: number;
	notvalid: string;
	nrplausievent: string;
	nrplausiperson: string;
	plausiErrors?: PlausiError[];
	plausistatus: number;
	status: number;
	version: number;
	classCondition?: string;
}

export interface Intervention {
	canton?: number;
	actionid?: number;
	deliveryid?: number;
	executiondate: string;
	actionuser?: string;
	validationreport?: string;
	validationreportDe?: string;
	validationreportFr?: string;
	validationreportIt?: string;
	plausireportname?: string;
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
	EMPTY: 6,
	FINALIZED: 5,
	VALIDATED: 4,
	DELIVERED: 3,
	CONFIRMATION: 2,
	AMENDREPLACE: 1,
	IMPORTED: 0,
} as const;

/**
 * Types d'interventions possibles
 */
export const MEB_INTERVENTIONTYPE = {
	DELIVER_FILE: 0,
	AMEND_DELIVERY: 1,
	REPLACE_DELIVERY: 2,
	CANCEL_DELIVERY: 3,
	CONFIRM_DELIVERY: 4,
	CREATE_PLAUSIREPORT: 5,
	VALIDATE: 6,
	FINALIZE: 7,
	DELETE: 8,
	DELIVERY_WITH_ERRORS: 9,
	UNDO_VALIDATE: 10,
	UNDO_FINALIZE: 11,
	PLAUSIREPORT_IN_CREATION: 12,
	UPLOAD: 13,
} as const;

export const MEB_PLAUSISTATUS = {
	UNDEFINED: 0,
	NOT_VALID: 1,
	VALID: 2,
	CONFIRMED: 3,
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
