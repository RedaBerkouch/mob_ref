import {Component, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

export interface SharedDialogData {
	message: string;
	showCancel?: boolean;
	confirmLabel?: string;
	cancelLabel?: string;
}

@Component({
	selector: 'app-shared-dialog',
	standalone: false,
	templateUrl: './shared-dialog.html',
	styleUrl: './shared-dialog.scss'
})
export class SharedDialog {
	data = inject<SharedDialogData>(MAT_DIALOG_DATA);
	private dialogRef = inject(MatDialogRef<SharedDialog>);

	onConfirm(): void {
		this.dialogRef.close(true);
	}

	onCancel(): void {
		this.dialogRef.close(false);
	}
}
