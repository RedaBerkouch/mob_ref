import {Component, Input, model} from '@angular/core';

@Component({
	selector: 'app-toggle-administration',
	standalone: false,
	templateUrl: './toggle-administration.html',
	styleUrl: './toggle-administration.scss'
})
export class ToggleAdministration {
	@Input() actionLabel = '';
	activated = model<boolean>(false);
}
