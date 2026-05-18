import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {WebFilter} from "./Delivery";

export interface Canton {
	canton: number;
	cantonId: number;
	confirmRules: string;
	creationDate: number;
	creationDateString?: string;
	creationUser: string;
	deliveryStatus: number;
	finalisationDate: number;
	finalisationDateString?: string;
	finalisationUser: string;
	modificationDate: number;
	modificationDateString?: string;
	modificationUser: string;
	plausiStatus: number;
	plausiDate: number;
	plausiDateString?: string;
	plausiUser: string;
	userText: string;
	validationDate: number;
	validationDateString?: string;
	validationUser: string;
	version: number;
	classCondition?: string;
}

export const MEB_CANTONSTATUS = {
	INITIALIZED: 0,
	DELIVERED: 4,
	VALIDATED: 6,
	FINALIZED: 7
}

export interface CantonParams {
	version: number;
	canton?: number;
	webFilters?: WebFilter[];
	whereFilters?: WhereFilter[];
}
