import {Component, signal, computed, inject} from '@angular/core';
import { WizardContext, WizardSchool } from '../model/wizard-delivery';
import { WizardDeliveryService } from '../services/wizard-delivery';
import { ConfirmableError } from '../model/ConfirmableError';
import {ObNotificationService,ObEUploadEventType,
	ObIUploadEvent,
	ObIFileDescription} from "@oblique/oblique";
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { TemplateRef, ViewChild,ElementRef,Renderer2 } from '@angular/core';
import {TranslateService} from "@ngx-translate/core";



@Component({
	selector: 'app-data-wizard',
	standalone: false,
	templateUrl: './data-wizard.html',
	styleUrl: './data-wizard.scss'
})
export class DataWizard {

	@ViewChild('fileUploadContainer') fileUploadContainer!: ElementRef;

	uploadedFiles: ObIFileDescription[] = [];


	private readonly obNotificationService = inject(ObNotificationService);

	private initialized = false;

	/* ===============================
	   STATE (données brutes)
	   =============================== */

	readonly context = signal<WizardContext | null>(null);
	readonly schools = signal<WizardSchool[]>([]);
	readonly confirmableErrors = signal<ConfirmableError[]>([]);

	readonly selectedDlUser = signal<string | null>(null);
	readonly selectedFile = signal<File | null>(null);

	readonly showConfirmableView = signal(false);

	readonly headerLoading = signal(true);   // init uniquement
	readonly contentLoading = signal(false);
	readonly error = signal<string | null>(null);

	@ViewChild('confirmableDialog') confirmableDialog!: TemplateRef<any>;

	confirmableColumns = [
		'accept',
		'person',
		'exam',
		'description',
		'rule'
	];

	private dialogRef?: MatDialogRef<any>;

	/* ===============================
	   COMPUTED (logique dérivée)
	   =============================== */

	readonly hasNonConfirmableErrors = computed(
		() => (this.context()?.nrOfNonConfirmableErrors ?? 0) > 0
	);

	readonly hasConfirmableErrors = computed(
		() => (this.context()?.nrOfConfirmableErrors ?? 0) > 0
	);

	readonly hasConfirmedErrors = computed(
		() => (this.context()?.nrOfConfirmedErrors ?? 0) > 0
	);

	readonly hasEverConfirmableErrors = computed(
		() => this.hasConfirmableErrors() || this.hasConfirmedErrors()
	);

	readonly hasErrors = computed(
		() => this.hasNonConfirmableErrors() || this.hasConfirmableErrors()
	);

	readonly canValidate = computed(() => {
		const ctx = this.context();
		if (!ctx) return false;

		return !this.validationBlocked() && !ctx.deliveriesValidated;
	});


	readonly validationBlocked = computed(() => {
		const ctx = this.context();
		if (!ctx) return true;

		// ⛔ aucune école chargée
		if (this.schools().length === 0) {
			return true;
		}

		return (
			!ctx.allSchoolsDelivered ||
			ctx.nrOfNonConfirmableErrors > 0 ||
			ctx.nrOfConfirmableErrors > 0
		);
	});


	readonly validationBlockReason = computed<
		null | 'SCHOOLS' | 'NON_CONFIRMABLE' | 'CONFIRMABLE'
	>(() => {
		const ctx = this.context();
		if (!ctx) return null;

		// ⛔ aucune école
		if (this.schools().length === 0) {
			return 'SCHOOLS';
		}

		if (!ctx.allSchoolsDelivered) return 'SCHOOLS';
		if (ctx.nrOfNonConfirmableErrors > 0) return 'NON_CONFIRMABLE';
		if (ctx.nrOfConfirmableErrors > 0) return 'CONFIRMABLE';

		return null;
	});


	private extractHttpErrorMessage(err: any): string {
		// HttpErrorResponse (le plus courant)
		const raw =
			(typeof err === 'string' && err) ||
			(typeof err?.error === 'string' && err.error) ||
			(typeof err?.message === 'string' && err.message) ||
			null;

		if (!raw) return 'Erreur inconnue.';

		return this.extractTextareaError(raw) || this.stripHtml(raw) || raw;
	}

	private extractTextareaError(html: string): string | null {
		const match = /<textarea[^>]*id=["']error["'][^>]*>([\s\S]*?)<\/textarea>/i.exec(html);
		if (!match?.[1]) return null;

		// Nettoyage léger (trim + espaces)
		return match[1].replace(/\s+/g, ' ').trim();
	}

	private stripHtml(html: string): string {
		return html.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim();
	}

	/* ===============================
	   INIT
	   =============================== */

	constructor(private service: WizardDeliveryService,
				private dialog: MatDialog,
				private translate: TranslateService) {
		this.loadAll();
	}

	get currentLang(): 'fr' | 'de' | 'it' {
		const lang = this.translate.currentLang || this.translate.getDefaultLang();
		return (lang?.startsWith('de') ? 'de'
			: lang?.startsWith('it') ? 'it'
				: 'fr');
	}
	getErrorMessage(e: ConfirmableError): string {
		switch (this.currentLang) {
			case 'de': return e.errorMsgDe || e.errorMsgFr;
			case 'it': return e.errorMsgIt || e.errorMsgFr;
			default:   return e.errorMsgFr || e.errorMsgDe;
		}
	}

	getPlausiname(e: ConfirmableError): string {
		switch (this.currentLang) {
			case 'de': return e.plausiNameDe || e.plausiNameFr;
			case 'it': return e.plausiNameIt || e.plausiNameFr;
			default:   return e.plausiNameFr || e.plausiNameDe;
		}
	}


	/* ===============================
	   LOAD
	   =============================== */

	loadAll(): void {
		this.error.set(null);
		this.contentLoading.set(true);
		this.service.loadContext().subscribe({
			next: ctx => {
				this.context.set(ctx);

				// INIT une seule fois
				if (!this.initialized && ctx.availableDlUsers?.length) {

					const rememberedUser = this.service.getSelectedDlUser();

					const userToUse =
						rememberedUser && ctx.availableDlUsers.includes(rememberedUser)
							? rememberedUser
							: ctx.availableDlUsers[0];

					this.selectedDlUser.set(userToUse);
					this.initialized = true;

					this.service.changeDlUser(userToUse).subscribe(() => {
						this.loadSchools();
						this.headerLoading.set(false);
					});

					return;
				}


				// cas normal
				this.selectedDlUser.set(ctx.dlUser);
				this.initialized = true;
				this.headerLoading.set(false);
				this.loadSchools();
			},
			error: err => {
				this.notifyError(err);
				this.headerLoading.set(false);
				this.contentLoading.set(false);
			}
		});
	}



	private loadSchools(): void {
		this.service.loadSchools().subscribe({
			next: schools => this.schools.set(schools),
			error: () => this.error.set('Erreur chargement écoles'),
			complete: () => this.contentLoading.set(false)
		});
	}

	/* ===============================
	   DL USER
	   =============================== */

	onDlUserChange(dlUser: string): void {
		this.selectedDlUser.set(dlUser);

		// 🔥 persistance wizard
		this.service.setSelectedDlUser(dlUser);

		this.service.changeDlUser(dlUser).subscribe(() => this.loadAll());
	}


	/* ===============================
	   FILE UPLOAD
	   =============================== */

	onFileSelected(event: Event): void {
		const input = event.target as HTMLInputElement;
		if (input.files?.length) {
			this.selectedFile.set(input.files[0]);
		}
	}

upload(): void {
  const file = this.selectedFile();
  if (!file) {
    this.notifyWarning('Veuillez sélectionner un fichier.');
    return;
  }

  this.service.uploadFile(file).subscribe({
    next: (message) => {
      const msg = (message ?? '').trim();

      if (msg.length > 0) {
        // ✅ backend a renvoyé l’erreur en 200
        this.notifyError(msg);
        return;
      }

      this.notifySuccess('Fichier chargé avec succès.');
      this.loadAll();
    },
    error: (err) => {
      // ici: erreurs réseau / 500 / etc.
      this.notifyError(this.extractHttpErrorMessage(err));
    }
  });
}


	deleteAll(): void {
		this.service.deleteDeliveries().subscribe({
			next: () => {
				this.notifySuccess('Toutes les données ont été supprimées.');
				this.loadAll();
			},
			error: err => this.notifyError(err)
		});
	}

	/* ===============================
	   VALIDATION
	   =============================== */

	validate(): void {
		this.service.validateDeliveries().subscribe({
			next: () => {
				this.notifySuccess('Livraisons validées avec succès.');
				this.loadAll(); // 🔥 le contexte décidera
			},
			error: err => this.notifyError(err)
		});
	}





	/* ===============================
	   PLAUSI
	   =============================== */

	downloadPlausireport(): void {
		this.service.downloadPlausireport().subscribe(blob => {
			const url = URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = 'PlausiReport.xlsx';
			a.click();
			URL.revokeObjectURL(url);
		});
	}

	openConfirmableView(): void {
		this.service.loadAllPlausiErrors().subscribe(errors => {
			this.confirmableErrors.set(errors.filter(e => e.confirmable));

			this.dialogRef = this.dialog.open(this.confirmableDialog, {
				width: '90vw',        // 🔥 presque plein écran
				maxWidth: '1200px',   // 🔥 limite desktop
				minWidth: '900px',    // 🔥 jamais trop étroit
				height: '80vh',      // 🔥 grande hauteur
				disableClose: false,
				panelClass: 'confirmable-dialog'
			});
		});
	}



	closeConfirmableDialog(): void {
		this.dialogRef?.close();
		this.loadAll();
	}


	saveAndCloseConfirmable(): void {
		this.service
			.confirmErrors(this.confirmableErrors())
			.subscribe(() => {
				this.dialogRef?.close();
				this.loadAll();
			});
	}


	private readonly channel = 'wizard';

	private notifyError(message: string): void {

		this.obNotificationService.error({
			title: 'Erreur',
			message
		});
	}

	private notifySuccess(message: string): void {
		this.obNotificationService.success({
			title: 'Succès',
			message
		});
	}

	private notifyWarning(message: string): void {
		this.obNotificationService.warning({
			title: 'Attention',
			message
		});
	}

	onObFileUploadEvent(event: ObIUploadEvent): void {

		if (
			event.type === ObEUploadEventType.SELECTED ||
			event.type === ObEUploadEventType.CHOSEN
		) {
			if (event.files && event.files.length > 0) {
				const file = event.files[0] as File;

				this.selectedFile.set(file);

				this.uploadedFiles = [{
					name: file.name,
					size: file.size,
					type: file.type
				}];
			}
		}

		if (event.type === ObEUploadEventType.DELETED) {
			this.removeSelectedFile();
		}
	}

	removeSelectedFile(): void {
		this.selectedFile.set(null);
		this.uploadedFiles = [];
	}

	formatFileSize(bytes: number): string {
		if (bytes === 0) return '0 Bytes';
		const k = 1024;
		const sizes = ['Bytes', 'KB', 'MB', 'GB'];
		const i = Math.floor(Math.log(bytes) / Math.log(k));
		return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
	}



}
