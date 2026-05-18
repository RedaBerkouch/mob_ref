import {Component, inject, Input, model} from '@angular/core';
import {parseParameters} from "../../core/utils/filters.util";
import {DatePipe} from "@angular/common";

export interface WhereFilter {
	id?: number;
	attribute: string;
	operator: string;
	relation: string;
	value: string;
}

export interface PredefinedFilter {
	id: number;
	label: string;
	description: string;
	parameters?: string;
	sql: string;
	default?: boolean;
}

interface ParsedParameter {
	id: string;
	key: string;
	value: string;
}

export type ContextFilterOption = {
	value: string;
	label: string;
};

export type ContextFilterConfig = {
	key: string;
	label: string;
	type: 'text' | 'select' | 'date' | 'checkbox' | 'plausi-error' | 'parametre';
	options?: ContextFilterOption[];
	optionsSource?: string;
	filterableOnContext?: boolean;
};

@Component({
	selector: 'app-pre-advance-filter',
	standalone: false,
	templateUrl: './pre-advance-filter.html',
	styleUrl: './pre-advance-filter.scss'
})
export class PreAdvanceFilter {
	private datePipe: DatePipe = inject(DatePipe);

	contextFilters = model<WhereFilter[]>([{attribute: '', operator: '=', value: '', relation: 'AND'}]);
	selectedPredefinedFilters = model<PredefinedFilter[]>([]);
	private dateValueCache = new Map<WhereFilter, { input: string; date: Date }>();

	@Input()
	predefinedFilters: PredefinedFilter[] = [];
	@Input()
	contextFiltersConfig: ContextFilterConfig[] = [];

	// Cache pour les paramètres parsés
	private parametersCache = new Map<number, ParsedParameter[]>();

	contextFiltersOperators: string[] = ['=', '>', '<', '<=', '>=', '<>', 'LIKE'];
	contextFiltersRelations: string[] = ['AND', 'OR'];

	/**
	 * Récupère le type d'un champ à partir de sa clé
	 */
	getFieldType(fieldKey: string): 'text' | 'select' | 'date' | 'plausi-error' | 'checkbox' | 'parametre' | null {
		const field = this.contextFiltersConfig.find(f => f.key === fieldKey);
		return field ? field.type : null;
	}

	/**
	 * Récupère les options d'un champ select
	 */
	getFieldOptions(fieldKey: string): ContextFilterOption[] {
		const field = this.contextFiltersConfig.find(f => f.key === fieldKey);
		return field?.options || [];
	}

	/**
	 * Appelé quand le champ change - réinitialise la valeur si nécessaire
	 */
	onFieldChange(ctx: WhereFilter): void {
		// Réinitialise la valeur quand on change de champ
		ctx.value = '';
		this.notifyContextFiltersChange();
	}

	addContextFilter() {
		this.contextFilters.update(filters => [
			...filters,
			{attribute: '', operator: '=', value: '', relation: 'AND'}
		]);
	}

	/**
	 * Notifie le changement des filtres contextuels
	 */
	notifyContextFiltersChange(): void {
		// Force la mise à jour du signal pour déclencher la propagation
		this.contextFilters.update(filters => [...filters]);
	}

	getDateValue(ctx: WhereFilter): Date | null {
		if (!ctx.value) return null;

		const cached = this.dateValueCache.get(ctx);
		if (cached && cached.input === ctx.value) return cached.date;

		const parts = ctx.value.split('.');
		if (parts.length !== 3) return null;

		const date = new Date(+parts[2], +parts[1] - 1, +parts[0]);
		this.dateValueCache.set(ctx, { input: ctx.value, date });
		return date;
	}

	onDateChange(ctx: WhereFilter, value: any): void {
		ctx.value = value ? (this.datePipe.transform(value, 'dd.MM.yyyy') ?? '') : '';
		this.notifyContextFiltersChange();
	}

	/**
	 * Vérifie si un filtre est sélectionné
	 */
	isFilterSelected(id: number): boolean {
		return this.selectedPredefinedFilters().some(f => f.id === id);
	}

	/**
	 * Toggle un filtre prédéfini
	 */
	toggleFilter(filter: PredefinedFilter, checked: boolean): void {
		if (checked) {
			this.selectedPredefinedFilters.update(filters => [...filters, filter]);
		} else {
			this.selectedPredefinedFilters.update(filters =>
				filters.filter(f => f.id !== filter.id)
			);
		}
	}

	/**
	 * Met à jour un paramètre spécifique
	 */
	updateParameter(filter: PredefinedFilter, id: string, event: Event): void {
		const input = event.target as HTMLInputElement;
		const newValue = input.value;

		if (!filter.parameters) {
			return;
		}

		// Parse les paramètres actuels
		const params = parseParameters(filter.parameters);

		// Trouve et met à jour le paramètre
		const paramIndex = params.findIndex(p => p.id === id);
		if (paramIndex !== -1) {
			params[paramIndex].value = newValue;
		}

		// Reconstruit la chaîne de paramètres
		filter.parameters = params
			.map(p => `${p.id}:${p.key}=${p.value}`)
			.join(';');

		// Invalide le cache
		this.parametersCache.delete(filter.id);

		// Notifie le changement
		this.selectedPredefinedFilters.update(filters => [...filters]);
	}

	/**
	 * Supprime un filtre contextuel à l'index donné
	 */
	removeContextFilter(index: number): void {
		const currentFilters = this.contextFilters();

		// Si 1 ligne, réinit
		if (currentFilters.length === 1) {
			this.contextFilters.set([{attribute: '', operator: '=', value: '', relation: 'AND'}]);
		} else if (currentFilters.length > 1) {
			this.contextFilters.update(filters =>
				filters.filter((_, i) => i !== index)
			);
		}
	}

	protected readonly parseParameters = parseParameters;

	isCollapsed = true;

	toggleCollapse(): void {
		this.isCollapsed = !this.isCollapsed;
	}
}
