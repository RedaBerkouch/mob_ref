import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WizardContext, WizardSchool } from '../model/wizard-delivery';
import {ConfirmableError} from "../model/ConfirmableError";

@Injectable({ providedIn: 'root' })
export class WizardDeliveryService {
	private readonly baseUrl = '/sdlweb/api/wizard/delivery';

	// 🔥 état mémorisé du wizard
	private selectedDlUser: string | null = null;

	constructor(private http: HttpClient) {}

	/* ===============================
     DL USER (state)
     =============================== */

	setSelectedDlUser(dlUser: string): void {
		this.selectedDlUser = dlUser;
	}

	getSelectedDlUser(): string | null {
		return this.selectedDlUser;
	}

	/* ===============================
	   CONTEXT
	   =============================== */

	loadContext(): Observable<WizardContext> {
		return this.http.get<WizardContext>(`${this.baseUrl}/context`);
	}

	/* ===============================
	   SCHOOLS
	   =============================== */

	loadSchools(): Observable<WizardSchool[]> {
		return this.http.get<WizardSchool[]>(`${this.baseUrl}/schools`);
	}

	/* ===============================
	   DL USER
	   =============================== */

	changeDlUser(dlUser: string): Observable<void> {
		const params = new HttpParams().set('dlUser', dlUser);
		return this.http.put<void>(`${this.baseUrl}/dl-user`, null, { params });
	}

	/* ===============================
	   FILE UPLOAD
	   =============================== */

	uploadFile(file: File): Observable<string> {
		const formData = new FormData();
		formData.append('file', file);

		return this.http.post(
			`${this.baseUrl}/upload`,
			formData,
			{ responseType: 'text' }, // ✅ important
		);
	}

	/* ===============================
	   ACTIONS
	   =============================== */

	validateDeliveries(): Observable<string | null> {
		return this.http.post(`${this.baseUrl}/deliveries/validate`, {}, { responseType: 'text' });
	}

	deleteDeliveries(): Observable<void> {
		return this.http.delete<void>(`${this.baseUrl}/deliveries`);
	}

	/* ===============================
	   PLAUSI REPORT
	   =============================== */

	downloadPlausireport(): Observable<Blob> {
		return this.http.get(`${this.baseUrl}/plausi-report`, { responseType: 'blob' });
	}

	loadConfirmableErrors(): Observable<ConfirmableError[]> {
		return this.http.get<ConfirmableError[]>('/sdlweb/api/wizard/delivery/errors/confirmable');
	}

	confirmErrors(errors: ConfirmableError[]): Observable<void> {
		return this.http.post<void>(`${this.baseUrl}/errors/confirm`, errors);
	}

	loadAllPlausiErrors(): Observable<ConfirmableError[]> {
		return this.http.get<ConfirmableError[]>('/sdlweb/api/wizard/delivery/errors');
	}

	saveConfirmableErrors(errors: ConfirmableError[]): Observable<void> {
		return this.http.post<void>(`${this.baseUrl}/errors`, errors);
	}
}
