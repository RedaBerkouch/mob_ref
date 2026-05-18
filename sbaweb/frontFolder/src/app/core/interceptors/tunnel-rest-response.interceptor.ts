import {HttpErrorResponse, HttpInterceptorFn, HttpResponse} from "@angular/common/http";
import {catchError, switchMap, throwError} from "rxjs";

export interface TunnelApiResponse<T> {
	status: number;
	message: string;
	data: T | null;
	success: boolean;
}

async function resolveBodyAsString(body: unknown): Promise<string> {
	if (typeof body === 'string') return body;
	if (body instanceof Blob) return body.text();
	return JSON.stringify(body);
}

async function tryParseAsTunnelError(body: unknown): Promise<TunnelApiResponse<any> | null> {
	try {
		const raw = await resolveBodyAsString(body);
		const parsed = JSON.parse(raw) as TunnelApiResponse<any>;

		if (!parsed || parsed.success) return null;

		return parsed.status >= 400 ? parsed : null;
	} catch {
		return null;
	}
}

export const tunnelRestResponseInterceptor: HttpInterceptorFn = (req, next) => {
	return next(req).pipe(
		switchMap(async event => {
			if (!(event instanceof HttpResponse)) return event;

			const body = event.body;

			// string (responseType: 'text'), Blob (responseType: 'blob'), ou objet JSON parsé
			const errorBody = await tryParseAsTunnelError(body);

			if (errorBody) {
				throw new HttpErrorResponse({
					error: errorBody.message,
					status: errorBody.status,
					statusText: errorBody.message,
					url: req.url,
				});
			}

			return event;
		}),
		catchError(err => throwError(() => err))
	);
};
