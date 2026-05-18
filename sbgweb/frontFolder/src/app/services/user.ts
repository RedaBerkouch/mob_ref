import { computed, inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { Role, UserInfo } from '../model/user';

@Injectable({
	providedIn: 'root',
})
export class UserService {
	private readonly baseUrl = '/sbgweb/api/user';
	private http = inject(HttpClient);

	 readonly userInfo$: Observable<UserInfo> = this.http
		.get<UserInfo>(`${this.baseUrl}`)
		.pipe(shareReplay(1));

	readonly user = toSignal(this.userInfo$, {
		initialValue: {
			authenticated: false,
			principalClass: '',
			roles: [],
			username: '',
		},
	});

	// Compatibilité avec ton ancien code
	readonly roles = computed(() => this.user().roles.map((r) => r.authority));

	readonly isAuthenticated = computed(() => this.user().authenticated);
	readonly username = computed(() => this.user().username);

	readonly hasRole = (role: Role | string) => computed(() => this.roles().includes(role));

	readonly hasAnyRole = (...roles: (Role | string)[]) =>
		computed(() => roles.some((role) => this.roles().includes(role)));

	readonly hasAllRoles = (...roles: (Role | string)[]) =>
		computed(() => roles.every((role) => this.roles().includes(role)));

	readonly isDL = this.hasAnyRole(Role.SDL_DL, Role.SSP_DL, Role.SBA_DL, Role.SBG_DL);
	readonly isDV = this.hasAnyRole(Role.SDL_DV, Role.SSP_DV, Role.SBA_DV, Role.SBG_DV);
	readonly isEV = this.hasAnyRole(Role.SDL_EV, Role.SSP_EV, Role.SBA_EV, Role.SBG_EV);
	readonly isEA = this.hasAnyRole(Role.SDL_EA, Role.SSP_EA, Role.SBA_EA, Role.SBG_EA);
	readonly isRO = this.hasAnyRole(Role.SDL_RO, Role.SSP_RO, Role.SBA_RO, Role.SBG_RO, Role.MEB_RO);

	readonly cantonRole = computed(
		() => this.roles().find((role) => role.startsWith('BFS-MEB.KANTON-')) ?? null,
	);

	readonly cantonCode = computed(() => {
		const role = this.cantonRole();
		return role ? role.replace('BFS-MEB.KANTON-', '') : null;
	});

	readonly hasCantonRole = computed(() => !!this.cantonRole());

	readonly canAccessDataDelivery = computed(
		() => this.isDL() || this.isDV() || this.isEV() || this.isEA(),
	);
	readonly canAccessDataTracking = computed(
		() => this.isEV(),
	);
	readonly canAccessDataProcessing = computed(
		() => this.isDL() || this.isDV() || this.isEV() || this.isEA(),
	);
	readonly canAccessAdministration = computed(() => this.isEA());

	readonly canUseLD = computed(() => this.isDL() || this.isDV() || this.isEV() || this.isEA());
	readonly canUseRO = computed(() => this.isDL() || this.isDV() || this.isEV() || this.isEA());
	readonly canUseRD = computed(() => this.isDV() || this.isEV() || this.isEA());
	readonly canUseRR = computed(() => this.isEV() || this.isEA());

	readonly mustForceCantonFilter = computed(() => this.isDL() || this.isDV());
	readonly canSeeOnlyOwnCanton = computed(() => this.isDL() || this.isDV());
	readonly mustRestrictToEmailSchools = computed(() => this.isDL());
}
