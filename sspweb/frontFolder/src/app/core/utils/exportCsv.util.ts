import {HttpResponse} from "@angular/common/http";

export function extractFilename(response: HttpResponse<Blob>): string | null {
	const contentDisposition = response.headers.get('Content-Disposition');
	if (!contentDisposition) return null;

	const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(contentDisposition);
	if (matches?.[1]) {
		let filename = matches[1].replace(/['"]/g, '');
		if (filename.includes("UTF-8''")) {
			filename = decodeURIComponent(filename.split("UTF-8''")[1]);
		}
		return filename;
	}

	return null;
}

export function openCsvFile(response: HttpResponse<Blob>, defaultName: string) {
	const blob = response.body;
	if (!blob) return;

	const filename = extractFilename(response) || defaultName;
	const url = window.URL.createObjectURL(blob);

	// Télécharger avec le nom du fichier
	const link = document.createElement('a');
	link.href = url;
	link.download = filename;
	document.body.appendChild(link);
	link.click();
	document.body.removeChild(link);

	setTimeout(() => window.URL.revokeObjectURL(url), 100);
}
