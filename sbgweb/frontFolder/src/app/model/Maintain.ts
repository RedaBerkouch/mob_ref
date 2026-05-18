import {WhereFilter} from "../shared/pre-advance-filter/pre-advance-filter";
import {WebFilter} from "./Delivery";
import {PlausiError} from "../shared/plausi-error-editor/plausi-error.model";

export interface Person {
	id: number;
	pid: number;
	canton: number;
	version: number;
	deliveryId: number;
	idType: number;
	sex: number;
	birthDate: number;
	newBirthDate: number;
	userComment: string;
	deliveryText: string;
	modUser: string;
	modDate: number;
	validationUser: string;
	validationDate: number;
	isToDelete: boolean;
	plausiStatus: number;
	status: number;
	classCondition?: string;
	plausiErrors?: PlausiError[];
}

export interface Qualification {
	eventid: number;
	pid: number;
	canton: number;
	version: number;
	type: number;
	sbfiCode: number;
	contractNr: number;
	professionCode: number;
	contractType: number;
	contractDate: number;
	educationYear: number;
	examType: number;
	examNr: number;
	examRepetition: number;
	examResult: number;
	cancelReason: number;
	cancelDate: number;
	burnr: number;
	kantLbCode: string;
	keyAspect: number;
	userComment: string;
	firmName: string;
	firmStreet: string;
	firmStreetNr: string;
	firmPlz: number;
	firmMunicipality: string;
	flagLbv: boolean;
	plausiStatus: number;
	isValidated: boolean;
	modUser: string;
	modDate: number;
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
	VALIDATED: 2,
	DELIVERED: 1,
	IMPORTED: 0
} as const;

