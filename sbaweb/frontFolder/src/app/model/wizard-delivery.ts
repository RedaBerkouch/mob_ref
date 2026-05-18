export interface WizardContext {
	dlUser: string;
	version: number;

	allSchoolsDelivered: boolean;
	deliveriesValidated: boolean;
	deliveriesValidatedConflict: boolean;

	// Errors (legacy WizardDeliveryTableManager)
	nrOfConfirmableErrors: number;      // ⛔ confirmables non confirmés
	nrOfConfirmedErrors: number;        // ✅ confirmés
	nrOfNonConfirmableErrors: number;   // ❌ non confirmables

	// Totaux legacy (WizardNrOfColumn + WS)
	totalPersons: number;
	totalQualifications: number;

	// DL user combo (ROLE_SBA_DV)
	availableDlUsers?: string[];
}



export interface WizardSchool {
	schoolId: string;
	schoolName: string;
	nrOfQualifications: number;
}
