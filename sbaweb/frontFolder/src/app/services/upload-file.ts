import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable, of, throwError } from 'rxjs';
import { VersionCantonFilterType } from '../shared/version-canton-filter/version-canton-filter';
import {ObHttpApiInterceptorEvents} from "@oblique/oblique";

@Injectable({
	providedIn: 'root',
})
export class UploadFileService {
	private readonly baseUrl = '/sbaweb/api';
	private http = inject(HttpClient);
	private obHttpApiInterceptorEvents = inject(ObHttpApiInterceptorEvents);

	private uploadState = signal<{
		loading: boolean;
		progress: number;
		error: string | null;
		success: boolean;
	}>({
		loading: false,
		progress: 0,
		error: null,
		success: false,
	});

	uploadFile(
		file: File,
		target: string,
		goal: string,
		filters?: VersionCantonFilterType,
	): Observable<string> {
		const formData = new FormData();
		formData.append('file', file);

		if (goal === 'upload_file') {
			formData.append(
				'cantonId',
				filters?.cantonFilter != null ? filters.cantonFilter.toString() : '-1',
			);
			formData.append(
				'version',
				filters?.versionFilter != null ? filters.versionFilter.toString() : '-1',
			);
		} else {
			formData.append('goal', goal || 'default');
		}

		this.obHttpApiInterceptorEvents.deactivateOnNextAPICalls(1);
		return this.http
			.post(`${this.baseUrl}/${target}`, formData, {
				responseType: 'text',
			})
			.pipe(
				map((response) => {
					const parser = new DOMParser();
					const doc = parser.parseFromString(response, 'text/html');
					const textarea = doc.getElementById('error');

					if (textarea) {
						return textarea.textContent || '';
					}

					return response;
				}),
				catchError((error) => {
					console.error('Upload failed', error);

					if (error.error?.text) {
						const parser = new DOMParser();
						const doc = parser.parseFromString(error.error.text, 'text/html');
						const textarea = doc.getElementById('error');

						if (textarea) {
							const message = textarea.textContent || '';
							return of(message);
						}
					}

					return throwError(() => new Error(error.error || error.message || "Erreur lors de l'upload"));
				}),
			);
	}

	resetUploadState(): void {
		this.uploadState.set({
			loading: false,
			progress: 0,
			error: null,
			success: false,
		});
	}
}
