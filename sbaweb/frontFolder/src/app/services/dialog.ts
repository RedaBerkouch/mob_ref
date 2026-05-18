import { Injectable, inject } from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import {SharedDialog} from "../shared/shared-dialog/shared-dialog";

/**
 * Service centralisé pour gérer les dialogues de confirmation et d'information
 *
 */
@Injectable({
	providedIn: 'root'
})
export class DialogService {
	private dialog = inject(MatDialog);
	private translateService = inject(TranslateService);

	/**
	 * Affiche un dialogue de confirmation avec boutons OK et Annuler
	 * Exécute l'action uniquement si l'utilisateur clique sur OK
	 *
	 * @param messageKey - Clé de traduction du message à afficher
	 * @param doAction - Fonction à exécuter si l'utilisateur confirme
	 * @param disableClose - Empêche la fermeture du dialogue en cliquant à l'extérieur (défaut: true)
	 *
	 */
	confirmAndDo(messageKey: string, doAction: () => void, disableClose = true): void {
		const dialogRef = this.dialog.open(SharedDialog, {
			disableClose,
			data: {
				message: this.translateService.instant(messageKey),
				showCancel: true
			}
		});

		dialogRef.afterClosed().subscribe(result => {
			if (result) {
				doAction();
			}
		});
	}

	/**
	 * Affiche un message d'information simple avec uniquement un bouton OK
	 * Aucune action n'est exécutée après la fermeture
	 *
	 * @param messageKey - Clé de traduction du message à afficher
	 * @param disableClose - Empêche la fermeture du dialogue en cliquant à l'extérieur (défaut: false)
	 *
	 */
	showMessage(messageKey: string, disableClose = false): void {
		this.dialog.open(SharedDialog, {
			disableClose,
			data: {
				message: this.translateService.instant(messageKey),
				showCancel: false
			}
		});
	}
}
