import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PreAdvanceFilter} from './pre-advance-filter';

describe('PreAdvanceFilter', () => {
	let component: PreAdvanceFilter;
	let fixture: ComponentFixture<PreAdvanceFilter>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			declarations: [PreAdvanceFilter]
		})
			.compileComponents();

		fixture = TestBed.createComponent(PreAdvanceFilter);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
