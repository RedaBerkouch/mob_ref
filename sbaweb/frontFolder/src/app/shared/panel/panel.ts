import { Component, Input } from '@angular/core';

@Component({
	selector: 'app-panel',
	standalone: false,
	templateUrl: './panel.html',
	styleUrl: './panel.scss'
})
export class Panel {
	@Input() title: string = '';
	@Input() collapsable = true;
	@Input() isCollapsed = false;

	toggleCollapse(): void {
		this.isCollapsed = !this.isCollapsed;
	}
}
