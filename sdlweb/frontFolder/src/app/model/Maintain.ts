import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {WebFilter} from "./Delivery";
import {PlausiError} from "../shared/plausi-error-editor/plausi-error.model";

export interface Person {
	canton: number;
	configDeliveryCode: string;
	confirmRules: string;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryId: number;
	deliveryStatus: number;
	id: string;
	idType: string;
	modificationDate: number;
	modificationUser: string;
	plausiStatus: number;
	prevalidationDate: number;
	prevalidationUser: string;
	userText: string;
	validationDate: number;
	validationUser: string;
	classCondition?: string;
	plausiErrors?: PlausiError[];
	burSchoolLabel: string;
	charPrivNoSubFlg: number;
	charPrivSubFlg: number;
	charPublFlg: number;
	classes: Qualification[];
	isSpecialSchool: boolean;
	schoolId: number;
	toDelete: boolean;
}

export interface Qualification {
	canton: number;
	configDeliveryCode: string;
	confirmRules: string;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryStatus: number;
	modificationDate: number;
	modificationUser: string;
	plausiStatus: number;
	prevalidationDate: number;
	prevalidationUser: string;
	schoolId: number;
	userText: string;
	validationDate: number;
	validationUser: string;
	classCondition?: string;
	plausiErrors?: PlausiError[];
	classId: number;
	id: string;
	schoolType: string;
}

export interface Learner {
	addition1: string;
	addition2: string;
	addition3: string;
	addition4: string;
	addition5: string;
	birthdate: number;
	canton: number;
	cantonalYear: number;
	classId: number;
	configDeliveryCode: string;
	confirmRules: string;
	country: number;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryStatus: number;
	educationType: number;
	historicResidence: number;
	id: string;
	idType: string;
	language: number;
	learnerId: number;
	modificationDate: number;
	modificationUser: string;
	nationality: number;
	origDeliveryData: string;
	planStatus: number;
	plausiStatus: number;
	prevCantonalYear: number;
	prevSchoolType: number;
	prevalidationDate: number;
	prevalidationUser: string;
	profMatura: number;
	residence: number;
	schoolType: number;
	sex: number;
	userText: string;
	validationDate: number;
	validationUser: string;
	version: number;
	age: number;
	classCondition?: string;
	plausiErrors?: PlausiError[];
}

export interface MaintainParams {
	version?: number;
	canton?: string;
	webFilters?: WebFilter[];
	whereFilters?: WhereFilter[];
}


export const MEB_DATASTATUS = {
	FINALIZED: 7,
	VALIDATED: 6,
	PREVALIDATED: 5,
	DELIVERED: 4,
	CONFIRMATION: 3,
	AMENDREPLACE: 2,
	IMPORTED: 1,
	INITIALIZED: 0
} as const;

