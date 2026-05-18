import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataWizard } from './data-wizard';

describe('DataWizard', () => {
  let component: DataWizard;
  let fixture: ComponentFixture<DataWizard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DataWizard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataWizard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
