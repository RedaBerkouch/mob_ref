import {DestroyRef, inject, Injectable, Signal} from '@angular/core';
import {takeUntilDestroyed, toObservable, toSignal} from '@angular/core/rxjs-interop';
import {interval, Observable} from 'rxjs';
import {distinctUntilChanged, map, startWith} from 'rxjs/operators';

/**
 * Clé utilisée pour stocker la langue dans le localStorage
 */
const OBLIQUE_LANG = 'oblique_lang';

/**
 * Service de gestion de la langue de l'application
 *
 * Ce service surveille les changements de langue stockés dans le localStorage
 * et expose la langue courante via un signal réactif.
 *
 */
@Injectable({
	providedIn: 'root'
})
export class LanguageService {
	private destroyRef = inject(DestroyRef);

	/**
	 * Signal contenant la langue courante de l'application
	 *
	 * Ce signal se met à jour automatiquement lorsque la valeur de 'oblique_lang'
	 * change dans le localStorage. La vérification est effectuée toutes les 100ms.
	 *
	 * Langues supportées : 'fr' (français), 'de' (allemand), 'it' (italien)
	 *
	 * @default 'fr' - Langue par défaut si aucune langue n'est définie dans le localStorage
	 */
	readonly currentLanguage: Signal<string> = toSignal(
		interval(100).pipe(
			map(() => localStorage.getItem(OBLIQUE_LANG) || 'fr'),
			distinctUntilChanged(),
			startWith(localStorage.getItem(OBLIQUE_LANG) || 'fr'),
			takeUntilDestroyed(this.destroyRef)
		),
		{initialValue: localStorage.getItem(OBLIQUE_LANG) || 'fr'}
	);

	/**
	 * Observable qui émet la langue courante de l'application
	 *
	 * Cet observable émet une nouvelle valeur à chaque fois que la langue change dans le localStorage.
	 * Il est dérivé du signal `currentLanguage` et permet l'utilisation avec les opérateurs RxJS
	 * classiques (switchMap, mergeMap, etc.) pour déclencher des actions réactives.
	 *
	 * Utile pour :
	 * - Recharger des données depuis le backend lors d'un changement de langue
	 * - Déclencher des effets secondaires asynchrones
	 * - Intégrer avec des flux RxJS existants
	 *
	 * @see currentLanguage - Le signal source
	 */
	readonly currentLanguage$: Observable<string> = toObservable(this.currentLanguage)

	/**
	 * Récupère la langue courante depuis le localStorage de manière synchrone
	 *
	 * @returns La langue courante ('fr', 'de', 'it' ou 'en'), ou 'fr' par défaut
	 */
	public getCurrentLanguage(): string {
		return localStorage.getItem(OBLIQUE_LANG) || 'fr';
	}
}
