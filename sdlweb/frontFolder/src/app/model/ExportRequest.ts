export interface ExportRequest {
	type: string;
	startDate?: string;
	endDate?: string;
	filters?: Record<string, string>;
}
