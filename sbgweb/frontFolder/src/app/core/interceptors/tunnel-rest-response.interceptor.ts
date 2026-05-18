import {HttpErrorResponse, HttpInterceptorFn, HttpResponse} from "@angular/common/http";
import {catchError, switchMap, throwError} from "rxjs";

export interface TunnelApiResponse<T> {
	status: number;
	message: string;
	data: T | null;
	success: boolean;
}

async function tryParseErrorFromBlob(blob: Blob): Promise<TunnelApiResponse<any> | null> {
	try {
		const text = await blob.text();
		const body = JSON.parse(text) as TunnelApiResponse<any>;
		return body && !body.success && body.status >= 400 ? body : null;
	} catch {
		return null;
	}
}

export const tunnelRestResponseInterceptor: HttpInterceptorFn = (req, next) => {
	return next(req).pipe(
		switchMap(async event => {
			if (!(event instanceof HttpResponse)) return event;

			if (event.body instanceof Blob) {
				const errorBody = await tryParseErrorFromBlob(event.body);
				if (errorBody) {
					throw new HttpErrorResponse({
						error: errorBody.message,
						status: errorBody.status,
						statusText: errorBody.message,
						url: req.url,
					});
				}
				return event;
			}

			const body = event.body as TunnelApiResponse<any>;
			if (body && !body.success && body.status >= 400) {
				throw new HttpErrorResponse({
					error: body.message,
					status: body.status,
					statusText: body.message,
					url: req.url,
				});
			}

			return event;
		}),
		catchError(err => throwError(() => err))
	);
};
