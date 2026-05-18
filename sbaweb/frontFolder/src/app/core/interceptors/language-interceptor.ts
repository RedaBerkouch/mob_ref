import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {LanguageService} from "../../services/language";

export const languageInterceptor: HttpInterceptorFn = (req, next) => {
	const reloadService = inject(LanguageService);

	const lang = reloadService.getCurrentLanguage();

	if (lang && ['fr', 'de', 'it'].includes(lang)) {
		const modifiedReq = req.clone({
			setHeaders: {'Accept-Language': lang}
		});
		return next(modifiedReq);
	}

	return next(req);
};
