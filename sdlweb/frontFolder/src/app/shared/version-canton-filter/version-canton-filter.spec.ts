import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VersionCantonFilter} from './version-canton-filter';

describe('VersionCantonFilter', () => {
	let component: VersionCantonFilter;
	let fixture: ComponentFixture<VersionCantonFilter>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			declarations: [VersionCantonFilter]
		})
			.compileComponents();

		fixture = TestBed.createComponent(VersionCantonFilter);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
