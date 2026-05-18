import {
	AfterViewInit,
	ChangeDetectorRef,
	Component,
	ElementRef,
	EventEmitter,
	HostBinding,
	Input,
	OnChanges,
	OnDestroy,
	OnInit,
	Output,
	QueryList,
	TemplateRef,
	ViewChild,
	ViewChildren,
	effect,
	signal,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { MatSort, Sort } from '@angular/material/sort';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { PlausiConfig } from '../plausi-error-editor/plausi-error.model';

export type DatahandlerSelectionConfig<T = any> = {
	mode: 'none' | 'single' | 'multiple';
	onSelect?: (row: T) => void;
};

export type DatahandlerAction = {
	icon: string;
	src?: string;
	tooltip: string;
	label: string;
	disabled: boolean;
	disabledFn?: () => boolean;
	handler: () => void;
};

export type DatahandlerColumn = {
	key: string;
	label: string;
	width: string;
	default?: string;
	type: 'text' | 'checkbox' | 'select' | 'date' | 'parametre' | 'plausi-error';
	editable: boolean;
	editableOnCondition?: (row: any, rowIndex: number) => boolean;
	visible?: boolean;
	nonSelectable?: boolean;
	options?: any;
	filterOptions?: { label: string; value: any }[];
	required?: boolean;
	optionsSource?: string;
	filterOptionsSource?: string;
	filterableOnContext?: boolean;
	unsortable?: boolean;
	plausiConfig?: PlausiConfig;
};

export type DatahandlerConfig = {
	title: string;
	entityName?: string;
	isByDefaultCollapsed: boolean;
	minViewHeader?: boolean;
	selection?: DatahandlerSelectionConfig;
	columns: DatahandlerColumn[];
	actions: DatahandlerAction[];
};

export type TableEvent = {
	action: string;
	data?: any[];
	itemsToUpdate?: any[];
	itemsToCreate?: any[];
	itemsToDelete?: any[];
	errorType?: string;
	invalidRows?: any[];
};

export type DoubleClickEvent = {
	columnKey: string;
	value: any;
	rowIndex: number;
	rowData: any;
};

type DisplayRow = any & { __globalIndex: number };

@Component({
	selector: 'app-datahandler',
	standalone: false,
	templateUrl: './datahandler.html',
	styleUrl: './datahandler.scss',
})
export class Datahandler implements OnInit, OnChanges, OnDestroy, AfterViewInit {
	@ViewChild('tableContainer', { static: false }) tableContainer!: ElementRef<HTMLElement>;
	private containerWidth = 0;
	private ro?: ResizeObserver;

	private readonly MIN_COL_PX = 170;
	private readonly HEADER_EXTRA_PX = 56;

	@Input() isLoading = false;

	@ViewChild(MatSort) sort!: MatSort;
	@ViewChildren('headerCell') headerCells!: QueryList<ElementRef<HTMLElement>>;

	@ViewChildren('selectEditorInput')
	selectEditorInputs!: QueryList<ElementRef<HTMLInputElement>>;

	@ViewChildren(MatAutocompleteTrigger)
	autoTriggers!: QueryList<MatAutocompleteTrigger>;

	private readonly minWidthByKey = new Map<string, number>();
	private readonly colByKey = new Map<string, DatahandlerColumn>();

	@Output() rowSelected = new EventEmitter<any>();
	@Output() tableEvent = new EventEmitter<TableEvent>();

	@ViewChild('columnDialog') columnDialog!: TemplateRef<any>;
	private columnDialogRef!: MatDialogRef<any>;

	@Input() clearSelection = signal(0);
	@Input() selectFirstRowOnInit = false;

	@Input() config!: DatahandlerConfig & { actions: DatahandlerAction[] };

	@Input()
	set dataSource(value: any[]) {
		this.updateDataSource(value);
		this.selectFirstRowSilentlyIfNeeded();
	}
	get dataSource() {
		return this._dataSource;
	}

	@Input() isMaster: boolean = false;

	@HostBinding('class.master-mode')
	get isMasterMode() {
		return this.isMaster;
	}

	@Input() pageSize = 100;
	private renderedCount = 100;

	private filteredIndexCache: number[] | null = null;
	private readonly filteredPosMap = new Map<number, number>();

	displayedRows: DisplayRow[] = [];
	displayedFormGroups: FormGroup[] = [];

	private applyFiltersTimer: any = null;
	private autoFitTimer: any = null;

	_dataSource: any[] = [];
	originalDataSnapshot: any[] = [];

	filtersForm!: FormGroup;

	columnFilters: Record<string, string> = {};
	visibleColumns: string[] = [];

	selectedRows: Set<number> = new Set();
	lastSelectedGlobalIndex: number | null = null;

	newRows = new Set<number>();
	deletedRows = new Set<number>();
	modifiedRows = new Set<number>();
	editedRows = new Set<number>();

	sortState = { active: '', direction: '' as 'asc' | 'desc' | '' };

	isResizing = false;
	private resizingCol: any = null;
	private startX = 0;
	private startWidth = 0;

	private readonly editingParams = new Set<string>();
	editingParam: { row: number; key: string } | null = null;
	editableParams: { key: string; value: string }[] = [];

	editingSelect: { row: number; key: string } | null = null;

	dialogColumns: { key: string; label: string; visible: boolean; nonSelectable?: boolean }[] = [];

	private readonly optionsCache = new Map<string, { label: string; value: any }[]>();
	private readonly optionLabelCache = new Map<string, Map<any, string>>();
	private readonly autoCtrlCache = new Map<string, FormControl>();

	isCollapsed = false;

	private readonly destroy$ = new Subject<void>();
	private readonly cellStartValue = new Map<string, any>();
	private readonly formGroupCache = new Map<number, FormGroup>();

	constructor(
		private readonly fb: FormBuilder,
		private readonly cdRef: ChangeDetectorRef,
		private readonly dialog: MatDialog,
		private readonly translate: TranslateService,
	) {
		effect(() => {
			const trigger = this.clearSelection();
			if (trigger > 0) this.clearSelectedRows();
		});

		this.translate.onLangChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
			this.invalidateOptionsCache();
			this.cdRef.detectChanges();
			this.refreshHeaderMinWidths();
		});
	}

	ngOnInit() {
		this.visibleColumns = [
			'select',
			...(this.config.columns?.filter((c) => c.visible ?? true).map((c) => c.key) ?? []),
		];

		this.columnFilters = {};
		this.config.columns.forEach((col) => (this.columnFilters[col.key] = ''));

		this.rebuildColumnIndexMaps();
		this.initFiltersForm();

		this.isCollapsed = this.config.isByDefaultCollapsed;
		this.renderedCount = this.pageSize;

		this.rebuildWindow();
	}

	ngAfterViewInit() {
		this.autoFitTimer = setTimeout(() => {
			this.autoFitColumns();
			this.refreshHeaderMinWidths();
			this.observeContainerResize();

			this.headerCells?.changes
				.pipe(takeUntil(this.destroy$))
				.subscribe(() => this.refreshHeaderMinWidths());
		});
	}

	ngOnChanges() {}

	ngOnDestroy() {
		this.destroy$.next();
		this.destroy$.complete();

		document.removeEventListener('mousemove', this.onResizeMouseMove);
		document.removeEventListener('mouseup', this.onResizeMouseUp);

		if (this.applyFiltersTimer) clearTimeout(this.applyFiltersTimer);
		if (this.autoFitTimer) clearTimeout(this.autoFitTimer);

		if (this.columnDialogRef) this.columnDialogRef.close();

		this.ro?.disconnect();
		this.ro = undefined;

		this.displayedRows = [];
		this.displayedFormGroups = [];
		this._dataSource = [];
		this.originalDataSnapshot = [];
		this.filteredIndexCache = null;
		this.filteredPosMap.clear();

		this.optionsCache.clear();
		this.optionLabelCache.clear();
		this.autoCtrlCache.clear();

		this.selectedRows.clear();
		this.newRows.clear();
		this.deletedRows.clear();
		this.modifiedRows.clear();
		this.editedRows.clear();

		this.cellStartValue.clear();
		this.formGroupCache.clear();

		this.editingParam = null;
		this.editableParams = [];
		this.editingSelect = null;
		this.autoCtrlCache.clear();

		this.minWidthByKey.clear();
		this.colByKey.clear();
	}

	get tableWidthPx(): number {
		const min = this.totalTableWidth;
		const cont = this.containerWidth || 0;
		return Math.max(min, cont);
	}

	private observeContainerResize(): void {
		const el = this.tableContainer?.nativeElement;
		if (!el) return;

		this.containerWidth = el.clientWidth;

		this.ro = new ResizeObserver(() => {
			const w = el.clientWidth;
			if (w !== this.containerWidth) {
				this.containerWidth = w;
				this.cdRef.detectChanges();
			}
		});

		this.ro.observe(el);
	}

	getMinWidthPx(col: DatahandlerColumn): number {
		const cached = this.minWidthByKey.get(col.key);
		if (cached && cached > 0) return cached;

		const current = parseInt(col.width, 10);
		if (Number.isFinite(current) && current > 0) return Math.max(this.MIN_COL_PX, current);

		return this.MIN_COL_PX;
	}

	private refreshHeaderMinWidths(): void {
		queueMicrotask(() => this.enforceHeaderMinWidths());
	}

	private enforceHeaderMinWidths(): void {
		if (!this.config?.columns?.length) return;
		if (this.headerCells?.length === 0) return;

		const visibleDataCols = this.config.columns.filter((c) => this.visibleColumns.includes(c.key));
		const headers = this.headerCells.toArray();
		let changed = false;

		for (let i = 0; i < headers.length; i++) {
			const headerEl = headers[i]?.nativeElement;
			const col = visibleDataCols[i];
			if (!headerEl || !col) continue;

			const labelW = Math.ceil(headerEl.scrollWidth);
			const needed = Math.max(this.MIN_COL_PX, labelW + this.HEADER_EXTRA_PX);

			const prevMin = this.minWidthByKey.get(col.key) ?? 0;
			if (needed > prevMin) {
				this.minWidthByKey.set(col.key, needed);
			}

			const current = parseInt(col.width, 10) || 0;
			if (current < needed) {
				col.width = `${needed}px`;
				changed = true;
			}
		}

		if (changed) {
			this.config.columns = [...this.config.columns];
			this.cdRef.detectChanges();
		}
	}

	private rebuildColumnIndexMaps(): void {
		this.colByKey.clear();
		for (const c of this.config.columns) this.colByKey.set(c.key, c);

		for (const c of this.config.columns) {
			const w = parseInt(c.width, 10);
			const base = Number.isFinite(w) && w > 0 ? w : this.MIN_COL_PX;
			if (!this.minWidthByKey.has(c.key)) {
				this.minWidthByKey.set(c.key, Math.max(this.MIN_COL_PX, base));
			}
		}
	}

	private initFiltersForm(): void {
		const group: Record<string, FormControl> = {};
		for (const col of this.config.columns) {
			group[col.key] = new FormControl('');
		}
		this.filtersForm = this.fb.group(group);
	}

	applyFiltersFromForm(): void {
		if (!this.filtersForm) return;

		const values = this.filtersForm.getRawValue() as Record<string, string>;
		this.columnFilters = { ...values };
		this.applyFilters();
	}

	private rebuildFilteredIndexCacheFromSource(): void {
		const indexes = Array.from({ length: this._dataSource.length }, (_, i) => i);
		this.filteredIndexCache = indexes;
		this.rebuildPosMap();
	}

	private rebuildPosMap(): void {
		this.filteredPosMap.clear();
		const arr = this.filteredIndexCache ?? [];
		for (let i = 0; i < arr.length; i++) {
			this.filteredPosMap.set(arr[i], i);
		}
	}

	private getOrCreateFormGroup(globalIndex: number): FormGroup {
		let fg = this.formGroupCache.get(globalIndex);
		if (!fg) {
			fg = this.buildFormGroup(this._dataSource[globalIndex] ?? {});
			this.formGroupCache.set(globalIndex, fg);
		}
		this.syncFormGroupWithRow(fg, this._dataSource[globalIndex] ?? {});
		return fg;
	}

	private syncFormGroupWithRow(fg: FormGroup, row: any): void {
		for (const col of this.config.columns) {
			const ctrl = fg.controls[col.key] as FormControl;
			const newVal =
				col.type === 'date' && row?.[col.key] ? new Date(row[col.key]) : row?.[col.key];
			if (ctrl?.value !== newVal) {
				ctrl.setValue(newVal, { emitEvent: false });
			}
		}
		if (fg.controls['isNew']) {
			(fg.controls['isNew'] as FormControl).setValue(!!row?.isNew, { emitEvent: false });
		}
		if (fg.controls['isDeleted']) {
			(fg.controls['isDeleted'] as FormControl).setValue(!!row?.isDeleted, {
				emitEvent: false,
			});
		}
		if (fg.controls['isModified']) {
			(fg.controls['isModified'] as FormControl).setValue(!!row?.isModified, {
				emitEvent: false,
			});
		}
	}

	private rebuildWindow(): void {
		if (!this.filteredIndexCache) this.rebuildFilteredIndexCacheFromSource();

		const idx = this.filteredIndexCache!;
		const slice = idx.slice(0, this.renderedCount);

		this.displayedRows = slice.map((globalIndex) => ({
			...this._dataSource[globalIndex],
			__globalIndex: globalIndex,
		}));

		this.displayedFormGroups = slice.map((globalIndex) => this.getOrCreateFormGroup(globalIndex));
	}

	private syncWindowRow(globalIndex: number): void {
		const winIdx = this.displayedRows.findIndex((r) => r.__globalIndex === globalIndex);
		if (winIdx === -1) return;

		this.displayedRows[winIdx] = {
			...this._dataSource[globalIndex],
			__globalIndex: globalIndex,
		};

		const fg = this.formGroupCache.get(globalIndex);
		if (fg) this.syncFormGroupWithRow(fg, this._dataSource[globalIndex]);
	}

	loadMore(): void {
		const max = (this.filteredIndexCache ?? []).length;
		if (this.renderedCount >= max) return;

		this.renderedCount = Math.min(this.renderedCount + this.pageSize, max);
		this.rebuildWindow();
		this.cdRef.detectChanges();
	}

	onScroll(event: Event): void {
		const el = event.target as HTMLElement;
		const atBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 80;
		if (atBottom) this.loadMore();
	}

	updateDataSource(value: any[]) {
		this._dataSource = value ?? [];

		this.originalDataSnapshot = JSON.parse(
			JSON.stringify(
				this._dataSource.map((row) => ({
					...row,
					isNew: false,
					isDeleted: false,
					isModified: false,
					_selected: false,
				})),
			),
		);

		this.newRows.clear();
		this.deletedRows.clear();
		this.modifiedRows.clear();
		this.editedRows.clear();

		this._dataSource.forEach((row, index) => {
			if (row?.isNew) this.newRows.add(index);
			if (row?.isDeleted) this.deletedRows.add(index);
			if (row?.isModified) {
				this.modifiedRows.add(index);
				this.editedRows.add(index);
			}
		});

		this.filteredIndexCache = null;
		this.filteredPosMap.clear();
		this.renderedCount = this.pageSize;

		this.config.columns.forEach((col) => (this.columnFilters[col.key] = ''));
		if (this.filtersForm) {
			this.filtersForm.reset({}, { emitEvent: false });
		}

		this.formGroupCache.clear();

		this.rebuildWindow();
		this.autoCtrlCache.clear();
		this.cdRef.detectChanges();
		this.refreshHeaderMinWidths();
	}

	cancelChanges() {
		this._dataSource = JSON.parse(JSON.stringify(this.originalDataSnapshot));

		this.newRows.clear();
		this.deletedRows.clear();
		this.editedRows.clear();
		this.modifiedRows.clear();
		this.selectedRows.clear();
		this.lastSelectedGlobalIndex = null;

		this.filteredIndexCache = null;
		this.filteredPosMap.clear();
		this.renderedCount = this.pageSize;

		this.config.columns.forEach((col) => (this.columnFilters[col.key] = ''));
		if (this.filtersForm) this.filtersForm.reset({}, { emitEvent: false });

		this.formGroupCache.clear();

		this.rebuildWindow();
		this.autoCtrlCache.clear();
		this.cdRef.detectChanges();
	}

	autoFitColumns() {
		const visibleDataCols = this.config.columns.filter((c) => this.visibleColumns.includes(c.key));
		const headers = this.headerCells.toArray();

		for (let i = 0; i < headers.length; i++) {
			const col = visibleDataCols[i];
			const header = headers[i];
			if (!col || !header) continue;

			const measured = header.nativeElement.offsetWidth;
			const w = Math.max(measured + 150, this.MIN_COL_PX);

			col.width = `${w}px`;
			this.minWidthByKey.set(col.key, Math.max(this.MIN_COL_PX, w));
		}

		this.config.columns = [...this.config.columns];
		this.cdRef.detectChanges();
	}

	applyFilters() {
		if (this.applyFiltersTimer) clearTimeout(this.applyFiltersTimer);

		this.applyFiltersTimer = setTimeout(() => {
			const baseIdx = Array.from({ length: this._dataSource.length }, (_, i) => i);
			let filteredIdx = baseIdx;

			for (const col of this.config.columns) {
				const filterValue = (this.columnFilters[col.key] ?? '').toString().trim().toLowerCase();
				if (!filterValue) continue;

				filteredIdx = filteredIdx.filter((globalIndex) => {
					const row = this._dataSource[globalIndex];

					if (col.type === 'select' || col.type === 'plausi-error') {
						return this.applySelectFilters(row, col, filterValue);
					}

					const cellValue = (row?.[col.key] ?? '').toString().toLowerCase();
					return cellValue.includes(filterValue);
				});
			}

			this.filteredIndexCache = filteredIdx;
			this.rebuildPosMap();

			this.renderedCount = this.pageSize;
			this.rebuildWindow();

			this.cdRef.detectChanges();
		}, 150);
	}

	private applySelectFilters(row: any, col: DatahandlerColumn, filterValue: string): boolean {
		const value = row?.[col.key];
		if (value === null || value === undefined) return false;

		const label = this.getSelectLabel(col, value);
		if (!label) return false;
		return label.toLowerCase().includes(filterValue);
	}

	onSortClick(columnKey: string) {
		if (this.sortState.active !== columnKey) {
			this.sortState = { active: columnKey, direction: 'asc' };
		} else if (this.sortState.direction === 'asc') {
			this.sortState.direction = 'desc';
		} else if (this.sortState.direction === 'desc') {
			this.sortState = { active: '', direction: '' };
		}
		this.applySort();
	}

	applySort() {
		const { active, direction } = this.sortState;

		if (!active || !direction) {
			this.rebuildWindow();
			this.cdRef.detectChanges();
			return;
		}

		if (!this.filteredIndexCache) this.rebuildFilteredIndexCacheFromSource();

		const idx = [...this.filteredIndexCache!];

		idx.sort((a, b) => {
			const aVal = (this._dataSource[a]?.[active] ?? '').toString().toLowerCase();
			const bVal = (this._dataSource[b]?.[active] ?? '').toString().toLowerCase();
			return direction === 'asc' ? aVal.localeCompare(bVal) : bVal.localeCompare(aVal);
		});

		this.filteredIndexCache = idx;
		this.rebuildPosMap();

		this.rebuildWindow();
		this.cdRef.detectChanges();
	}

	onSortChange(event: Sort) {
		const { active, direction } = event;
		if (!direction) return;
		this.sortState = { active, direction: direction as any };
		this.applySort();
	}

	isRowSelected(windowIndex: number): boolean {
		const globalIndex = this.displayedRows[windowIndex]?.__globalIndex;
		return globalIndex !== undefined && this.selectedRows.has(globalIndex);
	}

	onCheckboxClick(event: MouseEvent, windowIndex: number) {
		const mode = this.config?.selection?.mode;
		const globalIndex = this.displayedRows[windowIndex]?.__globalIndex;
		if (globalIndex === undefined) return;

		if (mode === 'single') {
			if (this.selectedRows.has(globalIndex)) {
				this.selectedRows.clear();
				this.lastSelectedGlobalIndex = null;
				this._dataSource.forEach((r) => (r._selected = false));
				this.rowSelected.emit(null);
				this.cdRef.detectChanges();
				return;
			}

			this.selectedRows.clear();
			this.selectedRows.add(globalIndex);
			this.lastSelectedGlobalIndex = globalIndex;

			this._dataSource.forEach((r) => (r._selected = false));
			this._dataSource[globalIndex]._selected = true;

			this.rowSelected.emit(this._dataSource[globalIndex]);
			this.cdRef.detectChanges();
			return;
		}

		const shift = event.shiftKey;
		const ctrl = event.ctrlKey || event.metaKey;

		if (shift && this.selectedRows.has(globalIndex)) {
			this.cdRef.detectChanges();
			return;
		}

		if (ctrl) {
			if (this.selectedRows.has(globalIndex)) {
				this.selectedRows.delete(globalIndex);
				this._dataSource[globalIndex]._selected = false;
			} else {
				this.selectedRows.add(globalIndex);
				this._dataSource[globalIndex]._selected = true;
				this.lastSelectedGlobalIndex = globalIndex;
			}
			this.emitSelection();
			this.cdRef.detectChanges();
			return;
		}

		if (shift && this.lastSelectedGlobalIndex !== null && this.filteredIndexCache) {
			const posA = this.filteredPosMap.get(this.lastSelectedGlobalIndex);
			const posB = this.filteredPosMap.get(globalIndex);

			if (posA !== undefined && posB !== undefined) {
				const [start, end] = [posA, posB].sort((x, y) => x - y);
				for (let p = start; p <= end; p++) {
					const idx = this.filteredIndexCache[p];
					this.selectedRows.add(idx);
					this._dataSource[idx]._selected = true;
				}
				this.emitSelection();
				this.cdRef.detectChanges();
				return;
			}
		}

		this.selectedRows.clear();
		this._dataSource.forEach((r) => (r._selected = false));

		this.selectedRows.add(globalIndex);
		this._dataSource[globalIndex]._selected = true;
		this.lastSelectedGlobalIndex = globalIndex;

		this.emitSelection();
		this.cdRef.detectChanges();
	}

	private emitSelection(): void {
		if (this.selectedRows.size === 0) {
			this.rowSelected.emit(null);
			return;
		}
		const selectedItems = Array.from(this.selectedRows).map((i) => this._dataSource[i]);
		this.rowSelected.emit(selectedItems);
	}

	toggleSelectAll(event: any) {
		this.selectedRows.clear();
		if (event.checked) {
			for (const r of this.displayedRows) {
				this.selectedRows.add(r.__globalIndex);
				this._dataSource[r.__globalIndex]._selected = true;
			}
		} else {
			for (const r of this.displayedRows) {
				this._dataSource[r.__globalIndex]._selected = false;
			}
		}
		this.emitSelection();
		this.cdRef.detectChanges();
	}

	isAllSelected() {
		if (this.displayedRows.length === 0) return false;
		return this.displayedRows.every((r) => this.selectedRows.has(r.__globalIndex));
	}

	isSomeSelected() {
		const any = this.displayedRows.some((r) => this.selectedRows.has(r.__globalIndex));
		return any && !this.isAllSelected();
	}

	clearSelectedRows(): void {
		this.selectedRows.clear();
		this.lastSelectedGlobalIndex = null;
		this._dataSource.forEach((r) => (r._selected = false));
		this.rowSelected.emit(null);
		this.cdRef.detectChanges();
	}

	private selectFirstRowSilentlyIfNeeded(): void {
		if (!this.selectFirstRowOnInit || this._dataSource.length === 0) return;
		const mode = this.config?.selection?.mode;
		if (mode === 'none') return;

		if (this.selectedRows.size > 0) return;

		if (!this.filteredIndexCache) this.rebuildFilteredIndexCacheFromSource();
		const first = this.filteredIndexCache?.[0];
		if (first === undefined) return;

		this.selectedRows.clear();
		this.selectedRows.add(first);
		this.lastSelectedGlobalIndex = first;

		this._dataSource.forEach((row, i) => (row._selected = i === first));
	}

	onActionClick(actionIcon: string) {
		const action = actionIcon.toLowerCase();

		switch (action) {
			case 'save':
				this.save();
				break;
			case 'add':
				this.insertRow();
				break;
			case 'delete':
				this.deleteRow();
				break;
			case 'block':
				this.onBlock();
				this.cancelChanges();
				break;
			default:
				this.actionParent(action);
				this.tableEvent.emit({ action });
				break;
		}
	}

	onBlock() {
		this.selectedRows.clear();
		this.lastSelectedGlobalIndex = null;

		this.newRows.clear();
		this.deletedRows.clear();
		this.editedRows.clear();
		this.modifiedRows.clear();
		this.editingParams.clear();

		this.editingSelect = null;
		this.autoCtrlCache.clear();

		this._dataSource = this._dataSource.map((row) => ({
			...row,
			isNew: false,
			isDeleted: false,
			isModified: false,
		}));

		this.sortState = { active: '', direction: '' };
		this.isCollapsed = false;

		this.filteredIndexCache = null;
		this.filteredPosMap.clear();
		this.renderedCount = this.pageSize;

		this.config.columns.forEach((col) => (this.columnFilters[col.key] = ''));
		if (this.filtersForm) this.filtersForm.reset({}, { emitEvent: false });

		this.rebuildWindow();
		this.cdRef.detectChanges();

		this.tableEvent.emit({ action: 'block' });
	}

	insertRow() {
		const emptyRow = this.buildEmptyRow();
		this._dataSource = [emptyRow, ...this._dataSource];

		this.formGroupCache.clear();

		this.newRows = new Set(Array.from(this.newRows).map((i) => i + 1));
		this.deletedRows = new Set(Array.from(this.deletedRows).map((i) => i + 1));
		this.modifiedRows = new Set(Array.from(this.modifiedRows).map((i) => i + 1));
		this.editedRows = new Set(Array.from(this.editedRows).map((i) => i + 1));
		this.newRows.add(0);

		this.filteredIndexCache = null;
		this.filteredPosMap.clear();

		this.rebuildWindow();

		this.emitUpdatedData('add');
		this.actionParent('add');

		this.cdRef.detectChanges();
	}

	deleteRow() {
		if (this.selectedRows.size === 0) {
			alert('Veuillez sélectionner au moins une ligne.');
			return;
		}

		for (const globalIndex of Array.from(this.selectedRows)) {
			this.deletedRows.add(globalIndex);

			if (this._dataSource[globalIndex]) {
				this._dataSource[globalIndex] = {
					...this._dataSource[globalIndex],
					isDeleted: true,
					isNew: false,
				};
				this.syncWindowRow(globalIndex);
			}
		}

		this.selectedRows.clear();
		this.lastSelectedGlobalIndex = null;

		this.emitUpdatedData('delete');
		this.actionParent('delete');

		this.cdRef.detectChanges();
	}

	private readonly validationErrorMap: Record<string, string> = {
		refObject: 'FILTER_OBJECTTYPE_EMPTY',
		authorisationLevel: 'FILTER_AUTHORISATION_EMPTY',
		source: 'FILTER_SOURCE_EMPTY',
		type: 'PLAUSI_TYPE_EMPTY',
		objectLevel: 'PLAUSI_OBJECTLEVEL_EMPTY',
	};

	private markInvalidRows(invalidRows: any[]) {
		invalidRows.forEach((item) => {
			this.editedRows.add(item.globalIndex);
			Object.values(item.fg.controls).forEach((control: any) => control.markAsTouched());
		});
		this.cdRef.detectChanges();
	}

	save() {
		const invalidRows = this.displayedFormGroups
			.map((fg, windowIndex) => {
				const globalIndex = this.displayedRows[windowIndex]?.__globalIndex;
				return { fg, windowIndex, globalIndex };
			})
			.filter(
				(x) => x.globalIndex !== undefined && x.fg.invalid && !this.deletedRows.has(x.globalIndex),
			)
			.map((x) => ({ fg: x.fg, index: x.windowIndex, globalIndex: x.globalIndex }));

		if (invalidRows.length > 0) {
			const firstInvalid = invalidRows[0];
			let errorType = 'VALIDATION_ERROR';

			for (const col of this.config.columns) {
				const control = firstInvalid.fg.get(col.key);
				if (control && control.invalid) {
					errorType = this.validationErrorMap[col.key] || 'VALIDATION_ERROR';
					break;
				}
			}

			this.tableEvent.emit({
				action: 'validation-error',
				errorType,
				invalidRows,
			});

			this.markInvalidRows(invalidRows);
			return;
		}

		this.emitUpdatedData('save');
		this.onBlock();
		this.cdRef.detectChanges();
	}

	emitUpdatedData(action?: string) {
		const updated = this._dataSource.map((dsRow, index) => ({
			...dsRow,
			isNew: dsRow?.isNew === true || this.newRows.has(index),
			isDeleted: dsRow?.isDeleted === true || this.deletedRows.has(index),
			isModified: dsRow?.isModified === true || this.modifiedRows.has(index),
		}));

		const itemsToUpdate = updated.filter(
			(item) => item.isModified && !item.isNew && !item.isDeleted,
		);
		const itemsToCreate = updated.filter((item) => item.isNew && !item.isDeleted);
		const itemsToDelete = updated.filter((item) => item.isDeleted && !item.isNew);

		this.tableEvent.emit({
			action: action ?? 'dataChanged',
			data: updated,
			itemsToUpdate,
			itemsToCreate,
			itemsToDelete,
		});
	}

	private cellKey(globalIndex: number, colKey: string) {
		return `${globalIndex}:${colKey}`;
	}

	onCellFocus(colKey: string, windowIndex: number) {
		const row = this.displayedRows[windowIndex];
		if (!row) return;
		const gi = row.__globalIndex;
		this.cellStartValue.set(this.cellKey(gi, colKey), this._dataSource[gi]?.[colKey]);
	}

	onCellCommit(colKey: string, windowIndex: number) {
		const row = this.displayedRows[windowIndex];
		if (!row) return;

		const gi = row.__globalIndex;
		if (this.deletedRows.has(gi)) return;

		const fg = this.formGroupCache.get(gi);
		if (!fg) return;

		const value = (fg.controls[colKey] as FormControl).value;

		const key = this.cellKey(gi, colKey);
		const prev = this.cellStartValue.get(key);
		this.cellStartValue.delete(key);

		if (prev === value) return;

		this.onCellChange(colKey, value, windowIndex);
	}

	onSelectCommit(colKey: string, windowIndex: number, value: any) {
		const row = this.displayedRows[windowIndex];
		if (!row) return;

		const gi = row.__globalIndex;
		if (this.deletedRows.has(gi)) return;

		const fg = this.formGroupCache.get(gi);
		if (fg) (fg.controls[colKey] as FormControl).setValue(value, { emitEvent: false });

		this.onCellChange(colKey, value, windowIndex);
	}

	onCellChange(key: string, v: any, windowIndex: number) {
		const row = this.displayedRows[windowIndex];
		if (!row) return;

		const globalIndex = row.__globalIndex;
		if (this.deletedRows.has(globalIndex)) return;

		const targets = this.selectedRows.size > 0 ? Array.from(this.selectedRows) : [globalIndex];

		for (const gi of targets) {
			if (!this._dataSource[gi]) continue;

			let value = v;
			const col = this.config.columns.find((c) => c.key === key);
			if (col?.type === 'date') {
				value = v instanceof Date ? v.getTime() : null;
			}
			this._dataSource[gi] = { ...this._dataSource[gi], [key]: value };

			this.editedRows.add(gi);
			if (!this.newRows.has(gi)) {
				this.modifiedRows.add(gi);
				this._dataSource[gi].isModified = true;
			}

			this.syncWindowRow(gi);
		}

		const changed = targets
			.filter(
				(gi) => this.modifiedRows.has(gi) && !this.newRows.has(gi) && !this.deletedRows.has(gi),
			)
			.map((gi) => ({ ...this._dataSource[gi] }));

		if (changed.length > 0) {
			this.tableEvent.emit({
				action: 'rowChanged',
				data: changed,
				itemsToUpdate: changed,
			});
		}

		this.cdRef.detectChanges();
	}

	private autoKey(windowIndex: number, colKey: string): string {
		return `${windowIndex}:${colKey}`;
	}

	getAutoCtrl(windowIndex: number, colKey: string): FormControl {
		const row = this.displayedRows[windowIndex];
		const gi = row?.__globalIndex;
		const cacheKey = `${gi}:${colKey}`;

		let ctrl = this.autoCtrlCache.get(cacheKey);
		if (!ctrl) {
			ctrl = new FormControl('');
			this.autoCtrlCache.set(cacheKey, ctrl);
		}
		return ctrl;
	}

	startEditingSelect(windowIndex: number, colKey: string, currentValue: any) {
		const col = this.config.columns.find((c) => c.key === colKey);
		if (!col) return;
		if (!this.isEditable(col, windowIndex)) return;

		this.editingSelect = { row: windowIndex, key: colKey };

		const ctrl = this.getAutoCtrl(windowIndex, colKey);
		const selectedOpt = this.resolveOptions(col).find((o) => o.value === currentValue) ?? null;

		ctrl.setValue(selectedOpt, { emitEvent: false });

		this.cdRef.detectChanges();
		this.focusActiveSelectEditor();
	}

	private focusActiveSelectEditor(): void {
		queueMicrotask(() => {
			setTimeout(() => {
				const input = this.selectEditorInputs?.last?.nativeElement;
				const trigger = this.autoTriggers?.last;

				if (!input) return;

				input.focus();
				input.select?.();
				trigger?.openPanel();
			});
		});
	}

	stopEditingSelect() {
		this.editingSelect = null;
		this.cdRef.detectChanges();
	}

	autoDisplay(col: DatahandlerColumn) {
		return (opt: any) => opt?.label ?? '';
	}

	getFilteredSelectOptions(
		col: DatahandlerColumn,
		windowIndex: number,
	): { label: string; value: any }[] {
		const ctrl = this.getAutoCtrl(windowIndex, col.key);
		const q = (ctrl.value?.label ?? ctrl.value ?? '').toString().toLowerCase().trim();

		const globalIndex = this.displayedRows[windowIndex]?.__globalIndex ?? windowIndex;
		const isNew = this.newRows.has(globalIndex);
		const all = isNew && col.filterOptions?.length ? col.filterOptions : this.getSelectOptions(col);

		if (!q) return all;
		return all.filter((o) => (o.label ?? '').toString().toLowerCase().includes(q));
	}

	getSelectLabel(col: DatahandlerColumn, value: any): string {
		if (value === null || value === undefined || value === '') return '';

		let mapForCol = this.optionLabelCache.get(col.key);
		if (!mapForCol) {
			mapForCol = new Map<any, string>();
			const opts = this.resolveOptions(col);
			for (const o of opts) mapForCol.set(o.value, o.label ?? String(o.value));
			this.optionLabelCache.set(col.key, mapForCol);
		}

		return mapForCol.get(value) ?? String(value);
	}

	getRowClass(row: any, globalIndex: number): string {
		if (this.deletedRows.has(globalIndex) || row?.isDeleted) {
			return 'deleted-row';
		}

		if (this.newRows.has(globalIndex) || row?.isNew) {
			return 'new-row';
		}

		if (this.editedRows.has(globalIndex) || this.modifiedRows.has(globalIndex) || row?.isModified) {
			return 'edited-row';
		}

		if (row?.classCondition) {
			return `${row.classCondition}-condition`;
		}

		return '';
	}

	buildEmptyRow() {
		const row: any = {};
		this.config.columns.forEach((col) => {
			row[col.key] = col.type === 'checkbox' ? false : col.default || '';
		});
		row.isNew = true;
		row.isDeleted = false;
		row.isModified = false;
		return row;
	}

	isEditable(col: any, windowIndex: number): boolean {
		const globalIndex = this.displayedRows[windowIndex]?.__globalIndex;
		const row = globalIndex !== undefined ? this._dataSource[globalIndex] : null;

		if (col?.editableOnCondition && typeof col.editableOnCondition === 'function') {
			return col.editableOnCondition(row, windowIndex);
		}

		return !!col?.editable;
	}

	buildFormGroup(row: any) {
		const group: any = {};
		this.config.columns.forEach((col) => {
			const validators = [];
			if (col.required) validators.push(Validators.required);
			const value = col.type === 'date' ? new Date(row[col.key]) : row[col.key];
			group[col.key] = new FormControl(value, validators);
		});
		group.isNew = new FormControl(row.isNew);
		group.isDeleted = new FormControl(row.isDeleted);
		group.isModified = new FormControl(row.isModified || false);
		return this.fb.group(group);
	}

	drop(event: CdkDragDrop<string[]>) {
		const cols = this.visibleColumns.filter((c) => c !== 'select');
		moveItemInArray(cols, event.previousIndex, event.currentIndex);
		this.visibleColumns = ['select', ...cols];

		this.refreshHeaderMinWidths();
	}

	get totalTableWidth(): number {
		return this.config.columns
			.filter((col) => this.visibleColumns.includes(col.key))
			.reduce((sum, col) => sum + parseInt(col.width, 10), 0);
	}

	onResizeMouseDown(event: MouseEvent, col: any) {
		event.preventDefault();
		this.isResizing = true;
		this.resizingCol = col;
		this.startX = event.pageX;
		this.startWidth = parseInt(col.width, 10);

		document.addEventListener('mousemove', this.onResizeMouseMove);
		document.addEventListener('mouseup', this.onResizeMouseUp);
	}

	onResizeMouseMove = (event: MouseEvent) => {
		if (!this.isResizing) return;
		const delta = event.pageX - this.startX;

		const newWidth = Math.max(this.getMinWidthPx(this.resizingCol), this.startWidth + delta);
		this.resizingCol.width = `${newWidth}px`;

		this.config.columns = [...this.config.columns];
		this.cdRef.detectChanges();
	};

	onResizeMouseUp = () => {
		this.isResizing = false;
		document.removeEventListener('mousemove', this.onResizeMouseMove);
		document.removeEventListener('mouseup', this.onResizeMouseUp);

		this.refreshHeaderMinWidths();
	};

	onDragStart() {
		this.isResizing = false;
		document.body.classList.add('dragging');
	}

	onDragEnd() {
		document.body.classList.remove('dragging');
	}

	openColumnDialog() {
		this.dialogColumns = this.config.columns.map((col) => ({
			key: col.key,
			label: col.label,
			visible: this.visibleColumns.includes(col.key),
			nonSelectable: col.nonSelectable,
		}));

		this.columnDialogRef = this.dialog.open(this.columnDialog, {
			width: '400px',
			disableClose: true,
		});
	}

	closeColumnDialog() {
		this.columnDialogRef.close();
	}

	confirmColumnDialog() {
		const selectedKeys = this.dialogColumns.filter((c) => c.visible).map((c) => c.key);
		this.visibleColumns = ['select', ...selectedKeys];
		this.columnDialogRef.close();
		this.cdRef.detectChanges();

		this.refreshHeaderMinWidths();
	}

	toggleAllColumns(value: boolean) {
		this.dialogColumns.forEach((col) => {
			if (!col.nonSelectable) col.visible = value;
		});
	}

	protected isDisabledAction(action: DatahandlerAction): boolean {
		if (action.disabledFn) return action.disabledFn();
		return action.disabled ?? false;
	}

	actionParent(action: string) {
		this.config.actions.filter((value) => action === value.icon).every((value) => value.handler());
	}

	getSelectOptions(col: DatahandlerColumn): { label: string; value: any }[] {
		const opts = this.resolveOptions(col);
		return col.optionsSource ? opts : sortByLabelThenValue(opts);
	}

	private resolveOptions(col: DatahandlerColumn): { label: string; value: any }[] {
		if (this.optionsCache.has(col.key)) return this.optionsCache.get(col.key)!;

		let opts: any[] = [];
		if (typeof col.options === 'function') opts = col.options();
		else if (Array.isArray(col.options)) opts = col.options;

		this.optionsCache.set(col.key, opts);
		return opts;
	}

	private invalidateOptionsCache(): void {
		this.optionsCache.clear();
		this.optionLabelCache.clear();
	}

	startEditingParam(windowIndex: number, columnKey: string, value: any) {
		this.editingParam = { row: windowIndex, key: columnKey };
		const val = typeof value === 'object' ? value : this.parseParams(value);
		this.editableParams = Array.isArray(val)
			? val
			: Object.entries(val).map(([k, v]) => ({ key: k, value: v as string }));
	}

	cancelParamEditor() {
		this.editingParam = null;
		this.editableParams = [];
		this.cdRef.detectChanges();
	}

	saveParamEditor(windowIndex: number, columnKey: string) {
		const newValueStr = this.editableParams.map((p) => `${p.key}=${p.value}`).join('; ');

		const row = this.displayedRows[windowIndex];
		if (!row) return;

		const globalIndex = row.__globalIndex;
		this._dataSource[globalIndex] = { ...this._dataSource[globalIndex], [columnKey]: newValueStr };
		this.syncWindowRow(globalIndex);

		this.onCellChange(columnKey, newValueStr, windowIndex);

		if (this.selectedRows.has(globalIndex)) {
			const mode = this.config?.selection?.mode;
			if (mode === 'single') {
				this.rowSelected.emit(this._dataSource[globalIndex]);
			} else {
				this.emitSelection();
			}
		}

		this.cancelParamEditor();
	}

	parseParams(value: any): { key: string; value: string }[] {
		if (!value) return [];
		if (typeof value === 'object') {
			return Object.entries(value).map(([key, val]) => ({ key, value: String(val ?? '') }));
		}
		return value
			.split(';')
			.map((part: string) => part.trim())
			.filter((part: string) => part)
			.map((part: string) => {
				const [key, val = ''] = part.split('=');
				return { key: key.trim(), value: val.trim() };
			});
	}

	private autoJustSelected = false;

	onAutoBlur(windowIndex: number, colKey: string): void {
		setTimeout(() => {
			if (!this.autoJustSelected) {
				this.stopEditingSelect();
			}
			this.autoJustSelected = false;
		}, 150);
	}

	onAutoSelected(
		col: DatahandlerColumn,
		windowIndex: number,
		selectedOpt: { label: string; value: any } | null,
	) {
		if (!selectedOpt) return;

		this.autoJustSelected = true;

		const row = this.displayedRows[windowIndex];
		if (!row) return;

		const gi = row.__globalIndex;
		if (this.deletedRows.has(gi)) return;

		const fg = this.formGroupCache.get(gi);
		if (fg) {
			(fg.controls[col.key] as FormControl).setValue(selectedOpt.value, { emitEvent: false });
		}

		this.onCellChange(col.key, selectedOpt.value, windowIndex);
		this.stopEditingSelect();
	}

	get totalLoaded() {
		return this._dataSource.length;
	}

	get totalFiltered() {
		return (this.filteredIndexCache ?? []).length || this._dataSource.length;
	}

	get totalSelected() {
		return this.selectedRows.size;
	}

	toggleCollapse() {
		this.isCollapsed = !this.isCollapsed;
	}

	trackByKey(index: number, col: any): string {
		return col.key;
	}

	onPlausiErrorsChanged(event: { rowIndex: number; errors: any[] }) {
		if (this.dataSource?.[event.rowIndex]) {
			const col = this.config.columns.find((c) => c.type === 'plausi-error');
			if (col?.plausiConfig) {
				this.dataSource[event.rowIndex] = {
					...this.dataSource[event.rowIndex],
					[col.plausiConfig.errorArrayKey]: event.errors,
				};
			}
		}
	}

	onPlausiRowModified(rowIndex: number) {
		this.modifiedRows.add(rowIndex);
		this.editedRows.add(rowIndex);

		if (this._dataSource[rowIndex]) {
			this._dataSource[rowIndex].isModified = true;
		}

		this.syncWindowRow(rowIndex);

		if (!this.newRows.has(rowIndex) && !this.deletedRows.has(rowIndex)) {
			const changed = { ...this._dataSource[rowIndex] };

			this.tableEvent.emit({
				action: 'rowChanged',
				data: [changed],
				itemsToUpdate: [changed],
			});
		}

		this.cdRef.detectChanges();
	}
}

export function sortByLabelThenValue(
	options: { label: string; value: any }[],
): { label: string; value: any }[] {
	if (!options) return options;

	return [...options].sort((a, b) => {
		const labelCompare = String(a.label ?? '').localeCompare(String(b.label ?? ''), undefined, {
			sensitivity: 'base',
		});

		if (labelCompare !== 0) return labelCompare;

		if (typeof a.value === 'number' && typeof b.value === 'number') return a.value - b.value;

		return String(a.value ?? '').localeCompare(String(b.value ?? ''));
	});
}
