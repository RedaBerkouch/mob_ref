import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ObUnknownRouteModule } from '@oblique/oblique';
import { Home } from './home/home';
import { Administration } from './administration/administration';

import { DataDelivery } from './data-delivery/data-delivery';


import {
	adminGuard,
	dataDeliveryGuard,
	dataProcessingGuard,
	dataTrackingGuard,
} from './core/guards/auth.guards';
import { DataMaintain } from './data-maintain/data-maintain';
import {Unauthorized} from "./unauthorized/unauthorized";
import {DataTracking} from "./data-tracking/data-tracking";

const routes: Routes = [
	{ path: '', redirectTo: 'data-delivery', pathMatch: 'full' },
	{ path: 'unauthorized', component: Unauthorized},
	{ path: 'home', component: Home },
	{ path: 'data-delivery', component: DataDelivery, canActivate: [dataDeliveryGuard] },
	{ path: 'data-tracking', component: DataTracking, canActivate: [dataTrackingGuard] },
	{ path: 'data-processing', component: DataMaintain, canActivate: [dataProcessingGuard] },
	{ path: 'administration', component: Administration, canActivate: [adminGuard] },

	{ path: '**', redirectTo: 'home' },
];

@NgModule({
	imports: [RouterModule.forRoot(routes, { anchorScrolling: 'enabled' }), ObUnknownRouteModule],
	exports: [RouterModule],
})
export class AppRoutingModule {}
