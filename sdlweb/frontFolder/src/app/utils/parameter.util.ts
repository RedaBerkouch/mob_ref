export function convertAsIncrementedParameters(params: string, name: string, number: number) {
	let asParam = "";

	if (!params) {
		params = "";
		for (let i=1; i<=number-1; i++) {
			params += ";"
		}
	}

	const split = params.split(";");

	for (let i=1; i<=number; i++) {
		const value = split.at(i - 1) ?? "";
		const separator = i < number ? ";" : "";

		asParam += `${name} ${i}=${value}${separator}`;
	}

	return asParam;
}

export function convertAsString(params: string, name: string, number: number) {
	let asString = ";;;";

	if (params) {
		asString = params;

		for (let i=1; i<=number; i++) {
			asString = asString.replace(`${name} ${i}=`, "");
		}
	}

	return asString;
}
