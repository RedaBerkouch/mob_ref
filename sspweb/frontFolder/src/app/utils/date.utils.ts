export function convertDateNumberToDate(dateNumber: number): Date {
	return new Date(dateNumber);
}

export function convertDateToDateString(date: number): string {
	return convertDateNumberToString(date, false);
}

export function convertDateToDateTimeString(date: number): string {
	return convertDateNumberToString(date, true);
}

function convertDateNumberToString(dateNumber: number, withTime: boolean) {
	let dateString: string = "";

	const date: Date = convertDateNumberToDate(dateNumber);

	if (dateNumber) {
		dateString = date.toLocaleDateString('fr-CH');

		if (withTime) {
			dateString += " " + date.toLocaleTimeString('fr-CH');
		}
	}

	return dateString;
}
