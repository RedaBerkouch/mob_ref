export function convertDateNumberToDate(dateNumber: number): Date {
	return new Date(dateNumber);
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
