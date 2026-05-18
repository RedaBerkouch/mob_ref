export interface CantonIntervention {
	canton: number;
	cantonId: number;
	interventionId: number;
	interventionDate: number;
	interventionDateString?: string;
	interventionUser: string;
	text: string;
	type: number;
	version: number;
	fileId: number;
}

export const SBA_CANTONINTERVENTIONTYPE = {
	INITIALIZE: 1,
	CREATE_PLAUSIREPORT: 2,
	VALIDATE: 3,
	UNDO_VALIDATE: 4,
	FINALIZE: 5,
	UNDO_FINALIZE: 6,
	MANUAL: 8,
	UPLOAD: 2125
}
