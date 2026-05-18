export interface ConfigDelivery {
	canton: number;
	creationDate: number;
	creationDateString?: string;
	creationUser: string;
	deliveryCode: string;
	deliveryId: number;
	dlUsers: string;
	dlUsersParameters: string;
	dueDate: number;
	dueDateDate?: Date;
	isDefault: boolean;
	modificationDate: number;
	modificationDateString: string;
	modificationUser: string;
	referenceDate: number;
	referenceDateDate?: Date;
	roUsers: string;
	roUsersParameters: string;
	userText: string;
	version: number;
}
