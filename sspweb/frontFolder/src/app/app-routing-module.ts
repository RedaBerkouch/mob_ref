import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ObUnknownRouteModule } from '@oblique/oblique';
import { Home } from './home/home';
import { Administration } from './administration/administration';

import { DataDelivery } from './data-delivery/data-delivery';


import {
	adminGuard,
	cantonOrganisationGuard,
	dataDeliveryGuard,
	dataProcessingGuard,
	schoolDeliveryGuard,
} from './core/guards/auth.guards';
import { DataWizard } from './data-wizard/data-wizard';
import { DataMaintain } from './data-maintain/data-maintain';
import { Initialisation } from './initialisation/initialisation';
import {Unauthorized} from "./unauthorized/unauthorized";

const routes: Routes = [
	{ path: '', redirectTo: 'data-delivery', pathMatch: 'full' },
	{ path: 'unauthorized', component: Unauthorized},
	{ path: 'home', component: Home },
	{ path: 'school-delivery', component: DataWizard, canActivate: [schoolDeliveryGuard] },
	{ path: 'data-delivery', component: DataDelivery, canActivate: [dataDeliveryGuard] },
	{ path: 'data-processing', component: DataMaintain, canActivate: [dataProcessingGuard] },
	{
		path: 'canton-organisation',
		component: Initialisation,
		canActivate: [cantonOrganisationGuard],
	},
	{ path: 'administration', component: Administration, canActivate: [adminGuard] },

	{ path: '**', redirectTo: 'home' },
];

@NgModule({
	imports: [RouterModule.forRoot(routes, { anchorScrolling: 'enabled' }), ObUnknownRouteModule],
	exports: [RouterModule],
})
export class AppRoutingModule {}
