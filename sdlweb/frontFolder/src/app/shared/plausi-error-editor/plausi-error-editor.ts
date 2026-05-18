import {ChangeDetectorRef, Component, effect, ElementRef, EventEmitter, HostListener, inject, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild, ViewContainerRef} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {PlausiConfig, PlausiError, PlausiStatusOption} from './plausi-error.model';
import {LanguageService} from "../../services/language";
import {Overlay, OverlayRef} from '@angular/cdk/overlay';
import {TemplatePortal} from '@angular/cdk/portal';

@Component({
	selector: 'app-plausi-error-editor',
	standalone: false,
	templateUrl: './plausi-error-editor.html',
	styleUrl: './plausi-error-editor.scss'
})
export class PlausiErrorEditor implements OnInit, OnDestroy {

	protected readonly languageService = inject(LanguageService);
	private overlay = inject(Overlay);
	private viewContainerRef = inject(ViewContainerRef);

	@ViewChild('summaryElement') summaryElement!: ElementRef;
	@ViewChild('modalTemplate') modalTemplate!: TemplateRef<any>;

	@Input({required: true}) formGroup!: FormGroup;
	@Input({required: true}) plausiConfig!: PlausiConfig;
	@Input() statusOptions: PlausiStatusOption[] = [];
	@Input() rowIndex!: number;

	@Output() errorsChanged = new EventEmitter<{
		rowIndex: number;
		errors: PlausiError[];
	}>();

	@Output() rowModified = new EventEmitter<number>();

	isEditorOpen: boolean = false;
	editableErrors: PlausiError[] = [];
	hoverResume: boolean = false;
	hoverError: number | null = null;
	currentLanguage = 'fr';

	modalTop = 0;
	modalLeft = 0;
	isDragging = false;
	dragStartX = 0;
	dragStartY = 0;
	modalStartX = 0;
	modalStartY = 0;

	private overlayRef?: OverlayRef;

	constructor(private cdRef: ChangeDetectorRef) {
		effect(() => {
			this.currentLanguage = this.languageService.currentLanguage();
		});
	}

	ngOnInit() {
		if (!this.formGroup) {
			console.error('PlausiErrorEditorComponent: formGroup is required');
		}
		if (!this.plausiConfig) {
			console.error('PlausiErrorEditorComponent: plausiConfig is required');
		}
	}

	ngOnDestroy() {
		if (this.overlayRef) {
			this.overlayRef.dispose();
		}
	}

	@HostListener('document:mousemove', ['$event'])
	onDocumentMouseMove(event: MouseEvent): void {
		if (this.isDragging) {
			event.preventDefault();
			const dx = event.clientX - this.dragStartX;
			const dy = event.clientY - this.dragStartY;
			this.modalLeft = this.modalStartX + dx;
			this.modalTop = this.modalStartY + dy;
		}
	}

	@HostListener('document:mouseup')
	onDocumentMouseUp(): void {
		this.isDragging = false;
	}

	@HostListener('document:keydown.escape')
	onEscapeKey(): void {
		if (this.isEditorOpen) {
			this.closeEditor();
		}
	}

	canEdit(): boolean {
		const statusValue = this.formGroup.get(this.plausiConfig.statusKey)?.value;
		const errorCount = this.getErrorCount();
		return this.plausiConfig.editableStatusValue.includes(statusValue) && errorCount > 0;
	}

	getErrorCount(): number {
		const errors = this.formGroup.get(this.plausiConfig.errorArrayKey)?.value as PlausiError[];
		return errors?.length || 0;
	}

	getStatusLabel(): string {
		const statusValue = this.formGroup.get(this.plausiConfig.statusKey)?.value;

		if (this.statusOptions && this.statusOptions.length > 0) {
			const option = this.statusOptions.find(opt => opt.value === statusValue);
			return option?.label || option?.name || String(statusValue);
		}

		return String(statusValue);
	}

	getPlausiName(error: PlausiError): string {
		switch (this.currentLanguage) {
			case 'de':
				return error.plausiNameDe;
			case 'it':
				return error.plausiNameIt;
			default:
				return error.plausiNameFr;
		}
	}

	getPlausiErrorMessage(error: PlausiError): string {
		switch (this.currentLanguage) {
			case 'de':
				return error.errorMsgDe;
			case 'it':
				return error.errorMsgIt;
			default:
				return error.errorMsgFr;
		}
	}

	openEditor(): void {
		if (!this.canEdit()) return;

		if (this.isEditorOpen) {
			this.closeEditor();
			return;
		}

		const errors = this.formGroup.get(this.plausiConfig.errorArrayKey)?.value as PlausiError[];
		this.editableErrors = errors ? JSON.parse(JSON.stringify(errors)) : [];

		if (this.summaryElement) {
			const rect = this.summaryElement.nativeElement.getBoundingClientRect();
			this.modalTop = rect.bottom + 8;

			const cellCenter = rect.left + (rect.width / 2);
			this.modalLeft = cellCenter;
		}

		if (!this.overlayRef) {
			this.overlayRef = this.overlay.create({
				hasBackdrop: false,
				scrollStrategy: this.overlay.scrollStrategies.noop(),
				positionStrategy: this.overlay.position().global()
			});
		}

		const portal = new TemplatePortal(this.modalTemplate, this.viewContainerRef);
		this.overlayRef.attach(portal);

		this.isEditorOpen = true;
		this.cdRef.detectChanges();
	}

	closeEditor(): void {
		if (this.overlayRef) {
			this.overlayRef.detach();
		}
		this.isEditorOpen = false;
		this.editableErrors = [];
		this.isDragging = false;
	}

	onConfirmationChange(error: PlausiError): void {
		const errorControl = this.formGroup.get(this.plausiConfig.errorArrayKey);
		if (errorControl) {
			const updatedErrors = JSON.parse(JSON.stringify(this.editableErrors));
			errorControl.setValue(updatedErrors);
			errorControl.markAsDirty();
		}

		this.errorsChanged.emit({
			rowIndex: this.rowIndex,
			errors: JSON.parse(JSON.stringify(this.editableErrors))
		});

		this.rowModified.emit(this.rowIndex);
		this.cdRef.detectChanges();
	}

	onDragStart(event: MouseEvent): void {
		this.isDragging = true;
		this.dragStartX = event.clientX;
		this.dragStartY = event.clientY;
		this.modalStartX = this.modalLeft;
		this.modalStartY = this.modalTop;
		event.preventDefault();
	}
}
