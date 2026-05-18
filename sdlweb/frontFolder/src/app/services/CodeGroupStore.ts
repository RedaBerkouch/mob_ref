import { Injectable, Signal, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, catchError, of } from 'rxjs';
import { LocalizedCode } from '../model/LocalizedCode';

export type SelectOption = { label: string; value: any };

@Injectable({ providedIn: 'root' })
export class CodeGroupStore {
	private readonly baseUrl = '/sdlweb/api/codegroups';

	constructor(private http: HttpClient) {}

	// --- Signals ---
	// (Je garde _values pour compat, mais on ne l’utilise plus comme source principale.)
	private _values = signal<LocalizedCode[]>([]);
	private _initialized = signal<boolean>(false);

	/**
	 * Loading global: true si au moins un load est en cours.
	 * On le gère avec un compteur pour éviter les incohérences.
	 */
	private _loading = signal<boolean>(false);
	private _loadingCount = 0;

	/**
	 * Cache principal des valeurs par codeGroup.
	 * IMPORTANT: on garde une Map stable mais on crée une NOUVELLE Map à chaque update
	 * (sinon le signal ne notifie pas).
	 */
	private _valuesMap = signal<Map<string, LocalizedCode[]>>(new Map());
	readonly valuesMap = this._valuesMap.asReadonly();

	// --- Exposed read-only signals ---
	values = computed(() => this._values());
	initialized = computed(() => this._initialized());
	loading = computed(() => this._loading());

	// -------------------------
	// ✅ CACHES ANTI “MEMORY LEAK”
	// -------------------------

	/**
	 * getValuesSignal() créait un computed différent à chaque appel.
	 * Ici: 1 computed par codeGroup, réutilisé partout.
	 */
	private valuesSignalCache = new Map<string, Signal<LocalizedCode[]>>();

	/**
	 * Cache d’options prêtes (SelectOption[]) par “clé” = codeGroup + formatterId.
	 * Ça t’évite de remapper dans chaque composant / computed.
	 */
	private optionsSignalCache = new Map<string, Signal<SelectOption[]>>();

	/**
	 * Dédup des requêtes en cours / déjà chargées (par codeGroup).
	 * Si tu veux gérer canton/lang différemment, on peut étendre la clé.
	 */
	private inFlightLoads = new Set<string>();
	private loadedGroups = new Set<string>();

	// -------------------------
	// Helpers
	// -------------------------

	private beginLoading(): void {
		this._loadingCount++;
		if (this._loadingCount === 1) {
			this._loading.set(true);
		}
	}

	private endLoading(): void {
		this._loadingCount = Math.max(0, this._loadingCount - 1);
		if (this._loadingCount === 0) {
			this._loading.set(false);
		}
	}

	// -------------------------
	// Actions (signatures inchangées)
	// -------------------------

	loadValues(codeGroup: string, canton?: number, lang?: string, allCantons?: boolean) {
		// ✅ Dédup simple: si déjà chargé et pas de param spécial => ne refetch pas
		// (si tu veux forcer le reload, passe par refreshCache() côté backend)
		const key = this.buildLoadKey(codeGroup, canton, lang);

		if (this.inFlightLoads.has(key)) return;

		// si tu charges toujours sans (canton/lang) et que tu re-appelles sans, on coupe
		// NB: si tu relies la langue via interceptor header, c’est parfait.
		if (this.loadedGroups.has(key)) return;

		this.inFlightLoads.add(key);
		this.beginLoading();

		const params: any = {};
		if (lang !== undefined) params.lang = lang;
		if (canton !== undefined) params.canton = canton;
		if (allCantons !== undefined) params.allCantons = allCantons;

		this.http
			.get<LocalizedCode[]>(`${this.baseUrl}/${codeGroup}`, { params })
			.pipe(
				catchError(() => of([] as LocalizedCode[])),
				finalize(() => {
					this.inFlightLoads.delete(key);
					this.loadedGroups.add(key);
					this.endLoading();
				})
			)
			.subscribe((data) => {
				// compat
				this._values.set(data);

				const current = this._valuesMap();
				const next = new Map(current);
				next.set(codeGroup, data);
				this._valuesMap.set(next);
			});
	}

	getValueById(codeGroup: string, id: number, canton?: number, lang: string = 'fr') {
		const params: any = { lang };
		if (canton !== undefined) params.canton = canton;
		return this.http.get(`${this.baseUrl}/${codeGroup}/${id}`, { params, responseType: 'text' });
	}

	searchValueInAllCantons(codeGroup: string, id: number, lang: string = 'fr') {
		return this.http.get(`${this.baseUrl}/${codeGroup}/${id}/search-all-cantons`, {
			responseType: 'text',
			params: { lang }
		});
	}

	refreshCache() {
		this.beginLoading();
		this.http
			.post<void>(`${this.baseUrl}/refresh`, {})
			.pipe(
				finalize(() => this.endLoading())
			)
			.subscribe({
				next: () => {
					// si ton backend refresh les caches, côté front on garde les signals stables
					// mais on peut marquer loadedGroups comme "à recharger" si tu veux refetch.
					// Ici, on se contente de re-check l’état.
					this.checkInitialized();
				},
				error: () => {
					// noop
				}
			});
	}

	checkInitialized(lang: string = 'fr') {
		// lang param conservé (signature inchangée) même si non utilisé côté backend
		this.beginLoading();
		this.http
			.get<boolean>(`${this.baseUrl}/initialized`)
			.pipe(finalize(() => this.endLoading()))
			.subscribe({
				next: (value) => this._initialized.set(value),
				error: () => this._initialized.set(false)
			});
	}

	getValuesSignal(codeGroup: string): Signal<LocalizedCode[]> {
		// ✅ retourne TOUJOURS le même Signal pour un codeGroup donné
		const cached = this.valuesSignalCache.get(codeGroup);
		if (cached) return cached;

		const sig = computed(() => this._valuesMap().get(codeGroup) || []);
		this.valuesSignalCache.set(codeGroup, sig);
		return sig;
	}

	// -------------------------
	// ✅ NOUVEAU (additif) : optionsFor()
	// -------------------------
	/**
	 * Donne un Signal d’options {label,value} prêt pour mat-select.
	 * - Stable: même instance Signal réutilisée
	 * - Centralise le mapping label/value (évite mapping dans chaque composant)
	 */
	optionsFor(
		codeGroup: string,
		labelFormatter?: (item: LocalizedCode) => string,
		skipDefaultValue = false,
	): Signal<SelectOption[]> {
		const formatterId = labelFormatter ? 'custom' : 'default';
		const cacheKey = `${codeGroup}::${formatterId}::${skipDefaultValue}`;

		const cached = this.optionsSignalCache.get(cacheKey);
		if (cached) return cached;

		const sig = computed(() => {
			const values = this.getValuesSignal(codeGroup)();
			const formatter = labelFormatter ?? ((item: LocalizedCode) => item.value);
			const options = values.map((item) => ({
				label: formatter(item),
				value: item.key
			}));
			return skipDefaultValue && options[0]?.value == null ? options.slice(1) : options;
		});

		this.optionsSignalCache.set(cacheKey, sig);
		return sig;
	}

	// -------------------------
	// Private
	// -------------------------

	private buildLoadKey(codeGroup: string, canton?: number, lang?: string): string {
		// clé simple; si tu veux qu’un changement de langue refetch automatiquement
		// sans surcharger, tu peux inclure lang/canton ici.
		const c = canton ?? '';
		const l = lang ?? '';
		return `${codeGroup}|${c}|${l}`;
	}
}
