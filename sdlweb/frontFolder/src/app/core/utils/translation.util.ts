/**
 * Récupère la traduction d'une propriété selon la langue
 * @param obj - Objet contenant les propriétés traduites (nameFr, nameDe, nameIt)
 * @param property - Nom de base de la propriété (ex: 'name', 'description')
 * @param lang - Code langue ('fr', 'de', 'it')
 * @returns La valeur traduite
 */
export function getTranslation<T>(obj: T, property: string, lang: string): string {
	const suffix = lang === 'de' ? 'De' : lang === 'it' ? 'It' : 'Fr';
	const key = `${property}${suffix}` as keyof T;
	return (obj[key] as string) || '';
}
