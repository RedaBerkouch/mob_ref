import { Component, effect, inject, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { VersionService } from './services/AppVersion';
import { UserService } from './services/user';

@Component({
	selector: 'app-root',
	templateUrl: './app.html',
	standalone: false,
	styleUrl: './app.scss',
})
export class App implements OnInit {
	versionService = inject(VersionService);
	private userService = inject(UserService);

	navigation: { url: string; label: string }[] = [];

	constructor(private translate: TranslateService) {
		effect(() => {
			this.userService.user();
			this.buildNavigation();
		});

		this.translate.onLangChange.subscribe(() => this.buildNavigation());
	}

	ngOnInit(): void {
		this.versionService.load();
	}

	private buildNavigation() {
		const allTabs = [
			{
				url: 'school-delivery',
				key: 'main.tab.datawizard.name',
				check: () => this.userService.canAccessSchoolDelivery(),
			},
			{
				url: 'data-delivery',
				key: 'main.tab.datadelivery.name',
				check: () => this.userService.canAccessDataDelivery(),
			},
			{
				url: 'data-processing',
				key: 'main.tab.dataadministration.name',
				check: () => this.userService.canAccessDataProcessing(),
			},
			{
				url: 'canton-organisation',
				key: 'main.tab.initialisation.name',
				check: () => this.userService.canAccessCantonOrganisation(),
			},
			{
				url: 'administration',
				key: 'main.tab.administration.name',
				check: () => this.userService.canAccessAdministration(),
			},
		];

		const visibleTabs = allTabs.filter((tab) => tab.check());
		const translationKeys = visibleTabs.map((tab) => tab.key);

		if (!translationKeys.length) {
			this.navigation = [];
			return;
		}

		this.translate.get(translationKeys).subscribe((translations) => {
			this.navigation = visibleTabs.map((tab) => ({
				url: tab.url,
				label: translations[tab.key],
			}));
		});
	}
}
