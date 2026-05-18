import {
	Component,
	effect,
	EventEmitter,
	Input,
	OnChanges,
	OnInit,
	Output,
	signal,
	SimpleChanges,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import {
	ContextFilterConfig,
	PredefinedFilter,
	WhereFilter,
} from '../pre-advance-filter/pre-advance-filter';
import { AdminFilterService } from '../../services/admin-filter';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export type VersionCantonFilterType = {
	versionFilter: number;
	cantonFilter: number;
	initYear: boolean;
	syncSchool: boolean;
	contextFilters: WhereFilter[];
	selectedPredefinedFilters: PredefinedFilter[];
};

@Component({
	selector: 'app-version-canton-filter',
	standalone: false,
	templateUrl: './version-canton-filter.html',
	styleUrl: './version-canton-filter.scss',
})
export class VersionCantonFilter implements OnInit, OnChanges {
	private readonly SESSION_STORAGE_KEY = 'OFS.MEB.versionCantonFilterState.';

	@Input() tabStorage: string = '';
	@Input() yearInit: boolean = false;
	@Input() yearInitLabel: string = '';
	@Input() versionCantonFilterTitle = '';
	@Input() schoolSynchroLabel: string = '';
	@Input() initialVersion?: string | number;
	@Input() initialCanton?: number;
	@Input() cantons: { value: number; label: string }[] = [];

	@Input()
	set refreshData(
		data:
			| { version?: string | number | undefined; canton?: string | number | undefined }
			| undefined,
	) {
		if (!data) return;

		const canton =
			data.canton !== undefined && data.canton !== null && data.canton !== ''
				? Number(data.canton)
				: undefined;

		// Update silencieux + emission contrôlée (dédoublonnée)
		this.updateFilters(data.version, canton);
	}

	@Input() predefinedFilters: PredefinedFilter[] = [];
	@Input() contextFiltersConfig: ContextFilterConfig[] = [];

	contextFilters = signal<WhereFilter[]>([]);
	selectedPredefinedFilters = signal<PredefinedFilter[]>([]);

	@Output() initialiseVersion = new EventEmitter<VersionCantonFilterType>();
	@Output() filterChanged = new EventEmitter<VersionCantonFilterType>();
	@Output() cantonChanged = new EventEmitter<number | null>();

	filterForm: FormGroup;

	// Anti data-leak : empêche d’émettre pendant les patch internes
	private isPatching = false;

	// Anti data-leak : dernier payload émis pour déduplication
	private lastEmittedHash: string | null = null;

	constructor(
		private fb: FormBuilder,
		private adminFilterService: AdminFilterService,
	) {
		this.filterForm = this.fb.group({
			versionFilter: [''],
			cantonFilter: [''],
			initYear: false,
			syncSchool: true,
		});
		// Émet la valeur du canton sélectionné pour la transmission de fichier (sans appliquer le filtre)
		this.filterForm
			.get('cantonFilter')!
			.valueChanges.pipe(takeUntilDestroyed())
			.subscribe((val: number | null) => this.cantonChanged.emit(val ?? null));

		// Quand les filtres admin sont chargés, on met la valeur par défaut si besoin
		effect(() => {
			if (!this.adminFilterService.loaded()) return;

			const current = this.filterForm.get('versionFilter')?.value;
			const hasValue =
				current !== null && current !== undefined && String(current).trim().length > 0;

			//  si initialVersion / lastFilters a déjà rempli la version, on ne touche pas
			if (hasValue) return;

			const year = this.adminFilterService.getActVersionYear() ?? new Date().getFullYear();

			this.isPatching = true;
			this.filterForm.patchValue({ versionFilter: year }, { emitEvent: false });
			this.isPatching = false;

			// pour que le parent recharge avec ce default
			this.emitCombinedFilters();
		});
	}

	ngOnInit() {
		this.initializeFilters();
		this.adminFilterService.loadAll();
	}

	ngOnChanges(changes: SimpleChanges) {
		if (changes['initialVersion'] || changes['initialCanton']) {
			if (!changes['initialVersion']?.firstChange || !changes['initialCanton']?.firstChange) {
				this.updateFilters(this.initialVersion, this.initialCanton);
			}
		}
	}

	private initializeFilters() {
		const savedState = this.loadFromSessionStorage();

		const version = savedState?.version ?? this.initialVersion;
		const canton = savedState?.canton ?? this.initialCanton;

		if (version != null || (canton != null && canton > -1)) {
			this.updateFilters(version, canton);
		}
	}

	/**
	 * Met à jour le form sans spam d'événements, puis émet une seule fois (dédoublonnée)
	 * + Sauvegarde en session storage
	 */
	private updateFilters(version?: string | number, canton?: number) {
		const updates: any = {};

		if (version !== undefined) {
			updates.versionFilter = version;
		}
		if (canton !== undefined) {
			updates.cantonFilter = canton;
		}

		if (Object.keys(updates).length > 0) {
			// Patch silencieux (ne déclenche pas de valueChanges)
			this.isPatching = true;
			this.filterForm.patchValue(updates, { emitEvent: false });
			this.isPatching = false;

			// Sauvegarder en session storage
			this.saveToSessionStorage();

			this.emitCombinedFilters();
		}
	}

	/**
	 * Appelé par l'utilisateur (bouton appliquer)
	 */
	applyFilter() {
		this.saveToSessionStorage();
		this.emitCombinedFilters();
	}

	/**
	 * Émet tous les filtres combinés (form + context + predefined)
	 * dédoublonné pour éviter spam réseau / re-render
	 * ne fait rien si patch interne en cours
	 */
	private emitCombinedFilters(): void {
		if (this.isPatching) return;

		const combinedFilters = this.getCombinedFilters();

		// hash stable -> si identique au dernier emit, on n’émet pas
		const hash = this.hashFilters(combinedFilters);
		if (hash === this.lastEmittedHash) return;
		this.lastEmittedHash = hash;

		this.filterChanged.emit(combinedFilters);
	}

	hasActiveFilters(): boolean {
		const values = this.filterForm.getRawValue();
		return !!(values.versionFilter || values.cantonFilter);
	}

	resetFilters(): void {
		this.isPatching = true;
		this.filterForm.reset(
			{
				versionFilter: '',
				cantonFilter: '',
				syncSchool: true,
			},
			{ emitEvent: false },
		);
		this.isPatching = false;

		this.contextFilters.set([]);
		this.selectedPredefinedFilters.set([]);

		this.clearSessionStorage();

		this.applyFilter();
	}

	/**
	 * Même signature, comportement gardé, mais sans spam
	 * - patch silencieux
	 * - emission 1 seule fois
	 */
	initVersion(): void {
		this.saveToSessionStorage();

		this.isPatching = true;
		this.filterForm.patchValue({ initYear: true }, { emitEvent: false });
		this.isPatching = false;

		this.initialiseVersion.emit(this.getCombinedFilters());

		this.isPatching = true;
		this.filterForm.patchValue({ initYear: false }, { emitEvent: false });
		this.isPatching = false;
	}

	private getCombinedFilters(): VersionCantonFilterType {
		const formValue = this.filterForm.getRawValue();

		const contextFiltersNotEmpty = (this.contextFilters() ?? []).filter(
			(f) =>
				!!f &&
				typeof f.attribute === 'string' &&
				f.attribute.trim().length > 0 &&
				f.value !== undefined &&
				f.value !== null &&
				String(f.value).trim().length > 0,
		);

		return {
			...formValue,
			contextFilters: contextFiltersNotEmpty,
			selectedPredefinedFilters: this.selectedPredefinedFilters() ?? [],
		};
	}

	// -------------------- Session Storage Helpers --------------------

	getSessionStorageKey() {
		return this.SESSION_STORAGE_KEY + this.tabStorage;
	}

	/**
	 * Sauvegarde version et canton en session storage
	 */
	private saveToSessionStorage(): void {
		try {
			const formValue = this.filterForm.getRawValue();
			const state = {
				version: formValue.versionFilter || null,
				canton:
					formValue.cantonFilter !== '' &&
					formValue.cantonFilter !== undefined &&
					formValue.cantonFilter !== null
						? formValue.cantonFilter
						: null,
			};
			sessionStorage.setItem(this.getSessionStorageKey(), JSON.stringify(state));
		} catch (error) {
			console.warn('Failed to save to session storage', error);
		}
	}

	/**
	 * Charge version et canton depuis session storage
	 */
	private loadFromSessionStorage(): { version?: string | number; canton?: number } | null {
		try {
			const stored = sessionStorage.getItem(this.getSessionStorageKey());
			if (!stored) return null;

			const state = JSON.parse(stored);
			return {
				version: state.version ?? undefined,
				canton: state.canton ?? undefined,
			};
		} catch (error) {
			console.warn('Failed to load from session storage', error);
			return null;
		}
	}

	/**
	 * Efface le session storage
	 */
	private clearSessionStorage(): void {
		try {
			sessionStorage.removeItem(this.getSessionStorageKey());
		} catch (error) {
			console.warn('Failed to clear session storage', error);
		}
	}

	// -------------------- Anti data-leak helpers --------------------

	/**
	 * Hash stable : on trie les clés + stabilise arrays (filters) => même contenu => même hash
	 */
	private hashFilters(payload: VersionCantonFilterType): string {
		const stable = this.stableClone(payload);
		return JSON.stringify(stable);
	}

	private stableClone<T>(obj: T): T {
		// primitives
		if (obj === null || obj === undefined) return obj;
		if (typeof obj !== 'object') return obj;

		// arrays
		if (Array.isArray(obj)) {
			return obj.map((x) => this.stableClone(x)) as any;
		}

		// objects: sort keys
		const anyObj: any = obj;
		const keys = Object.keys(anyObj).sort();
		const res: any = {};
		for (const k of keys) {
			// stabilise aussi WhereFilter/PredefinedFilter
			res[k] = this.stableClone(anyObj[k]);
		}

		// Optionnel : stabiliser l'ordre des contextFilters (si l’ordre n’a pas d’importance)
		// Ici je ne le force pas pour ne pas changer la sémantique.
		return res;
	}
}
