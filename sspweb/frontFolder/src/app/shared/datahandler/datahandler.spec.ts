import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Datahandler} from './datahandler';

describe('Datahandler', () => {
	let component: Datahandler;
	let fixture: ComponentFixture<Datahandler>;

	beforeEach(async () => {
		await TestBed.configureTestingModule({
			declarations: [Datahandler]
		})
			.compileComponents();

		fixture = TestBed.createComponent(Datahandler);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
