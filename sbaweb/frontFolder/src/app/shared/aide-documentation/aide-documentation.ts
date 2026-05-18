import {Component, Input} from '@angular/core';

export interface HelpLink {
	label: string;
	url: string;
	target?: '_blank' | '_self';
}

@Component({
	selector: 'app-aide-documentation',
	standalone: false,
	templateUrl: './aide-documentation.html',
	styleUrl: './aide-documentation.scss'
})
export class AideDocumentation {
	@Input() links: HelpLink[] = [];
}
