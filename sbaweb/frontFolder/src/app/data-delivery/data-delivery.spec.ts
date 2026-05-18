import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataDelivery } from './data-delivery';

describe('DataDelivery', () => {
  let component: DataDelivery;
  let fixture: ComponentFixture<DataDelivery>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DataDelivery]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataDelivery);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
