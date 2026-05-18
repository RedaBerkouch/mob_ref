import {AfterViewInit, Component, effect, ElementRef, EventEmitter, inject, Input, Output, Renderer2, ViewChild} from '@angular/core';
import {ObEUploadEventType, ObIFileDescription, ObIUploadEvent} from "@oblique/oblique";
import {MatDialog} from "@angular/material/dialog";
import {TranslateService} from "@ngx-translate/core";
import {SharedDialog} from "../shared-dialog/shared-dialog";
import {toSignal} from "@angular/core/rxjs-interop";

@Component({
	selector: 'app-upload-file',
	standalone: false,
	templateUrl: './upload-file.html',
	styleUrl: './upload-file.scss'
})
export class UploadFile implements AfterViewInit {
	selectedFile!: File | null;
	uploadedFiles: ObIFileDescription[] = [];

	@Output() uploaded = new EventEmitter<File>();

	@Input() title: string = '';
	@Input() label: string = '';
	@Input() dropAreaLabel: string = 'upload.type.message'; // TODO ne fonctionne pas avec le signal
	@Input() labelButton: string = '';
	@Input() notificationOnUpload: string = '';
	@Input() allTypes: boolean = false;
	@ViewChild('fileUploadContainer') container!: ElementRef;

	// Injection avec inject()
	private dialog = inject(MatDialog);
	private translateService = inject(TranslateService);
	private renderer = inject(Renderer2);

	private uploadMessage = toSignal(
		this.translateService.stream(this.dropAreaLabel),
		{ initialValue: '' }
	);

	constructor() {
		// Effect qui se déclenche quand le message change (de language)
		effect(() => {
			const message = this.uploadMessage();
			if (message && this.container) {
				this.customizeFileUploadLabels(message);
			}
		});
	}

	ngAfterViewInit(): void {
		// Forcer la mise à jour initiale
		const message = this.uploadMessage();
		if (message) {
			this.customizeFileUploadLabels(message);
		}
	}

	private customizeFileUploadLabels(message: string): void {
		const caption = this.container.nativeElement.querySelector('#ob-file-upload-caption');
		if (caption) {
			this.renderer.setProperty(caption, 'textContent', message);
		}

		const hints = this.container.nativeElement.querySelector('.ob-drop-zone-hints');
		if (hints) {
			this.renderer.setProperty(hints, 'innerHTML', '');
		}
	}

	acceptedTypes(): string[] {
		return this.allTypes ? [] : ['.xml', '.csv', '.zip'];
	}

	onFileUploadEvent(event: ObIUploadEvent) {
		if (event.type == ObEUploadEventType.SELECTED || event.type == ObEUploadEventType.CHOSEN) {
			if (event.files && event.files.length > 0) {
				this.selectedFile = event.files[0] as File;
				this.uploadedFiles = [{
					name: this.selectedFile.name,
					size: this.selectedFile.size,
					type: this.selectedFile.type
				}];
			}
		}

	}

	removeFile() {
		this.selectedFile = null;
	}

	formatFileSize(bytes: number): string {
		if (bytes === 0) return '0 Bytes';
		const k = 1024;
		const sizes = ['Bytes', 'KB', 'MB', 'GB'];
		const i = Math.floor(Math.log(bytes) / Math.log(k));
		return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
	}

	onSubmit() {
		if (!this.selectedFile) {
			return;
		}

		if (this.notificationOnUpload) {
			const dialogRef = this.dialog.open(SharedDialog, {
				disableClose: true,
				data: {
					message: this.translateService.instant('upload.confirm.delivery.message'),
					showCancel: true
				}
			});

			dialogRef.afterClosed().subscribe(result => {
				if (result){
					// après cliqué sur OK/confirmé
					this.uploaded.emit(this.selectedFile!);
				}
			});
		} else {
			// Si pas de notification, émettre directement
			this.uploaded.emit(this.selectedFile);
		}
	}
}
