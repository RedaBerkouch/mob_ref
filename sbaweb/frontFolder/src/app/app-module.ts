import {NgModule, provideBrowserGlobalErrorListeners, LOCALE_ID} from '@angular/core';
import {AppRoutingModule} from './app-routing-module';
import {App} from './app';
import {
	ObMasterLayoutModule,
	ObButtonModule,
	provideObliqueConfiguration,
	ObHttpApiInterceptor,
	ObExternalLinkModule,
	ObFileUploadComponent, ObFileInfoComponent, ObAlertComponent, ObNotificationModule
} from '@oblique/oblique';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DatePipe, registerLocaleData} from '@angular/common';
import localeDECH from '@angular/common/locales/de-CH';
import localeFRCH from '@angular/common/locales/fr-CH';
import localeITCH from '@angular/common/locales/it-CH';
import {TranslateModule} from '@ngx-translate/core';
import {provideHttpClient, HTTP_INTERCEPTORS, withInterceptorsFromDi, withInterceptors} from '@angular/common/http';
import {Home} from './home/home';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {Administration} from './administration/administration';
import {DataDelivery} from './data-delivery/data-delivery';
import {DataWizard} from './data-wizard/data-wizard';
import {Initialisation} from './initialisation/initialisation';
import {Datahandler} from './shared/datahandler/datahandler';
import {PreAdvanceFilter} from './shared/pre-advance-filter/pre-advance-filter';
import {VersionCantonFilter} from './shared/version-canton-filter/version-canton-filter';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatTableModule} from "@angular/material/table";
import {MatInputModule} from '@angular/material/input';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatSelectModule} from "@angular/material/select";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatOptionModule} from "@angular/material/core";
import {MatSortModule} from "@angular/material/sort";
import {MatSliderModule} from "@angular/material/slider";
import {MatTooltipModule} from "@angular/material/tooltip";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatRadioModule} from "@angular/material/radio";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {UploadFile} from './shared/upload-file/upload-file';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
import {AideDocumentation} from "./shared/aide-documentation/aide-documentation";
import {languageInterceptor} from "./core/interceptors/language-interceptor";
import {Panel} from "./shared/panel/panel";
import {MatDividerModule} from "@angular/material/divider";
import {MatDialogModule} from "@angular/material/dialog";
import {SharedDialog} from "./shared/shared-dialog/shared-dialog";
import {DataMaintain} from "./data-maintain/data-maintain";
import {ToggleAdministration} from "./shared/toggle-administration/toggle-administration";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {MatListOption, MatSelectionList} from "@angular/material/list";
import {MatAutocomplete, MatAutocompleteTrigger} from "@angular/material/autocomplete";
import {PlausiErrorEditor} from "./shared/plausi-error-editor/plausi-error-editor";
import {MatChipsModule} from "@angular/material/chips";
import { PortalModule } from '@angular/cdk/portal';
import {tunnelRestResponseInterceptor} from "./core/interceptors/tunnel-rest-response.interceptor";
import {Unauthorized} from "./unauthorized/unauthorized";

registerLocaleData(localeDECH);
registerLocaleData(localeFRCH);
registerLocaleData(localeITCH);

@NgModule({
	declarations: [
		App,
		Home,
		Administration,
		DataDelivery,
		DataWizard,
		Initialisation,
		DataMaintain,
		Datahandler,
		PreAdvanceFilter,
		VersionCantonFilter,
		UploadFile,
		AideDocumentation,
		Panel,
		SharedDialog,
		ToggleAdministration,
		PlausiErrorEditor,
		Unauthorized
	],
	imports: [
		BrowserAnimationsModule,
		AppRoutingModule,
		FormsModule,
		ReactiveFormsModule,
		ObMasterLayoutModule,
		ObButtonModule,
		TranslateModule,
		ObExternalLinkModule,
		// Angular Material
		MatTableModule,
		MatDatepickerModule,
		MatNativeDateModule,
		MatInputModule,
		MatCheckboxModule,
		MatSelectModule,
		MatFormFieldModule,
		MatButtonModule,//
		MatOptionModule,
		MatSortModule,
		MatSliderModule,
		MatTooltipModule,
		MatIconModule,//
		MatExpansionModule,
		MatRadioModule,
		MatCardModule,//
		DragDropModule,
		ObFileUploadComponent,
		ObFileInfoComponent,
		MatDividerModule,
		ObAlertComponent,
		ObNotificationModule,
		MatDialogModule,
		MatSlideToggleModule,
		MatProgressBarModule,
		MatProgressSpinner,
		MatListOption,
		MatSelectionList,
		MatAutocomplete,
		MatAutocompleteTrigger,
		MatChipsModule,
		PortalModule,
	],
	providers: [
		DatePipe,
		provideBrowserGlobalErrorListeners(),
		{provide: LOCALE_ID, useValue: 'de-CH'},
		provideObliqueConfiguration({
			accessibilityStatement: {
				applicationName: 'meb_frontend',
				conformity: 'none',
				createdOn: new Date('2025-10-17'),
				applicationOperator: 'a',
				contact: [{phone: '+41'}]
			}, hasLanguageInUrl: false
		}),
		{provide: HTTP_INTERCEPTORS, useClass: ObHttpApiInterceptor, multi: true},
		provideHttpClient(
			withInterceptorsFromDi(),
			withInterceptors([languageInterceptor, tunnelRestResponseInterceptor])
		)
	],
	bootstrap: [App]
})
export class AppModule {
}
