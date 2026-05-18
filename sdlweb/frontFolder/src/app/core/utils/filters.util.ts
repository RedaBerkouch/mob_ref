import {PredefinedFilter} from "../../shared/pre-advance-filter/pre-advance-filter";

export interface Parameter {
	defaultValue?: string;
	exportId?: number;
	filterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	parameterId?: number;
	parameterOrder?: number;
	plausiId?: number;
	uniqueName?: string;
}

export interface WebFilter {
	filterId?: number;
	nameDe?: string;
	nameFr?: string;
	nameIt?: string;
	descriptionDe?: string;
	descriptionFr?: string;
	descriptionIt?: string;
	refObject?: number;
	source?: string;
	authorisationLevel?: number;
	isActive?: boolean;
	isDefault?: boolean;
	filterOrder?: number;
	parameters?: Parameter[];
}

export interface LastFilters {
	version: number;
	canton: number;
}

/**
 * Parse les paramètres depuis la chaîne "id:key=value;id2:key2=value2"
 * id -> parameterId (non utilisé ici mais pourrait servir pour l'ordre)
 * key -> uniqueName
 * value -> defaultValue
 */
export function parseParameters(parametersString: string): Array<{ id: string, key: string, value: string }> {
	if (!parametersString?.trim()) {
		return [];
	}

	return parametersString
		.split(';')
		.filter(param => param.includes(':') && param.includes('='))
		.map(param => {
			const [idPart, ...rest] = param.split(':');
			const keyValuePart = rest.join(':');
			const [key, ...valueParts] = keyValuePart.split('=');

			return {
				id: idPart.trim(),
				key: key.trim(),        // uniqueName
				value: valueParts.join('=').trim()  // defaultValue
			};
		});
}


export function updateWebFilterWithPredefinedFilter(loadedFilter: WebFilter | undefined, predefinedFilter: PredefinedFilter) {
	if (!loadedFilter) return null;

	// Parser les paramètres du PredefinedFilter
	const parsedParams = parseParameters(predefinedFilter.parameters || '');

	// Créer une map des valeurs à surcharger (uniqueName -> defaultValue)
	const overrideMap = new Map<string, string>();
	parsedParams.forEach(parsed => {
		overrideMap.set(parsed.id, parsed.value);
	});

	// Filtrer et surcharger les paramètres
	const filteredParameters = loadedFilter.parameters
		?.filter(param => overrideMap.has(param.uniqueName!))
		.map(param => ({
			...param,
			defaultValue: overrideMap.get(param.uniqueName!)
		}));

	// Retourner le WebFilter avec les paramètres filtrés et surchargés
	return {
		...loadedFilter,
		parameters: filteredParameters
	} as WebFilter;
}
