import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {WebFilter} from "./Delivery";
import {PlausiError} from "../shared/plausi-error-editor/plausi-error.model";

export interface Person {
	birthdate: number;
	canton: number;
	configDeliveryCode: string;
	confirmRules: string;
	country: number;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryId: number;
	deliveryStatus: number;
	historicResidence: number;
	id: string;
	idType: string;
	isToDelete: boolean;
	modificationDate: number;
	modificationUser: string;
	origDeliveryData: string;
	personId: number;
	plausiStatus: number;
	prevalidationDate: number;
	prevalidationUser: string;
	residence: number;
	sex: number;
	userText: string;
	validationDate: number;
	validationUser: string;
	version: number;
	classCondition?: string;
	plausiErrors?: PlausiError[];
}

export interface Qualification {
	canton: number;
	charPrivNoSubFlg: number;
	charPrivSubFlg: number;
	charPublFlg: number;
	configDeliveryCode: string;
	confirmRules: string;
	creationDate: number;
	creationUser: string;
	deliveryCode: string;
	deliveryStatus: number;
	educationType: number;
	examDate: number;
	examNr: number;
	examType: number;
	isSpecialSchool: boolean;
	maturityLanguages: number;
	modificationDate: number;
	modificationUser: string;
	nameBurSchool: string;
	personId: number;
	plausiStatus: number;
	prevalidationDate: number;
	prevalidationUser: string;
	qualificationId: number;
	result: number;
	schoolId: string;
	schoolIdType: string;
	userText: string;
	validationDate: number;
	validationUser: string;
	version: number;
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

