import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Initialisation } from './initialisation';

describe('Initialisation', () => {
  let component: Initialisation;
  let fixture: ComponentFixture<Initialisation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Initialisation]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Initialisation);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
