export interface School {
	activityStatus: number;
	burNr: number;
	burActivityStatus: number;
	burCanton: number;
	burCantonalCodeSba: string;
	burCantonalCodeSdl: string;
	burCantonalCodeSsp: string;
	burCharPrivNoSubFlg: number;
	burCharPrivSubFlg: number;
	burCharPublFlg: number;
	burIsSba: boolean;
	burIsSdl: boolean;
	burIsSsp: boolean;
	burLabel: string;
	burMunicipality: number;
	burValidFromSba: number;
	burValidFromSdlSsp: number;
	burValidFromSsp: number;
	burValidToSba: number;
	burValidToSdlSsp: number;
	burValidToSsp: number;
	canton: number;
	cantonBur: number;
	cantonalCodeSba: string;
	cantonalCodeSdl: string;
	cantonalCodeSsp: string;
	charPrivNoSubFlg: number;
	charPrivSubFlg: number;
	charPublFlg: number;
	deliveryCode: string;
	deliveryId: number;
	isSpecialSchool?: boolean;
	isSpecialSchoolBur?: boolean;
	label: string;
	municipality: number;
	municipalityBur: number;
	nameBur: string;
	schoolId: number;
	synchStatusBur: number;
	synchStatusSba: number;
	synchStatusSdl: number;
	synchStatusSsp: number;
	userText: string;
	validFromBur: number;
	validFromSba: number;
	validFromSdlSsp: number;
	validFromSsp: number;
	validToBur: number;
	validToSba: number;
	validToSdlSsp: number;
	validToSsp: number;
	version: number;
	sba: boolean;
	sdl: boolean;
	ssp: boolean;
	syncParameter: boolean;
	isToDelete: boolean;
	isDeleted?: boolean;
	isModified?: boolean;
	isNew?: boolean;
}

export const MEB_SYNCHSTATUS = {
	UNCHANGED: 0,
	CHANGED: 1,
	NEW: 2,
	INACTIVATED: 3,
};

export const MEB_BUR_ACTIVITY_STATUS = {
	ACTIVE: 1,
	INACTIVE: 2,
	DELETED: 3,
	UNKNOWN: 4,
	VIRTUAL: 5,
	TRANSFERRED: 6,
};
