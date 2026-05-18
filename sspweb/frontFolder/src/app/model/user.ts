export interface UserRole {
	authority: string;
}

export interface UserInfo {
	authenticated: boolean;
	principalClass: string;
	roles: UserRole[];
	username: string;
}

export enum Role {
	// General MEB role
	MEB_RO = 'BFS-MEB.MEB_RO',

	// SDL roles
	SDL_RO = 'BFS-MEB.SDL_DL',
	SDL_DL = 'BFS-MEB.SDL_DL',
	SDL_DV = 'BFS-MEB.SDL_DV',
	SDL_EV = 'BFS-MEB.SDL_EV',
	SDL_EA = 'BFS-MEB.SDL_EA',

	// SSP roles
	SSP_RO = 'BFS-MEB.SSP_RO',
	SSP_DL = 'BFS-MEB.SSP_DL',
	SSP_DV = 'BFS-MEB.SSP_DV',
	SSP_EV = 'BFS-MEB.SSP_EV',
	SSP_EA = 'BFS-MEB.SSP_EA',

	// SBA roles
	SBA_RO = 'BFS-MEB.SBA_RO',
	SBA_DL = 'BFS-MEB.SBA_DL',
	SBA_DV = 'BFS-MEB.SBA_DV',
	SBA_EV = 'BFS-MEB.SBA_EV',
	SBA_EA = 'BFS-MEB.SBA_EA',

	// SBG roles
	SBG_RO = 'BFS-MEB.SBG_RO',
	SBG_DL = 'BFS-MEB.SBG_DL',
	SBG_DV = 'BFS-MEB.SBG_DV',
	SBG_EV = 'BFS-MEB.SBG_EV',
	SBG_EA = 'BFS-MEB.SBG_EA',
}
