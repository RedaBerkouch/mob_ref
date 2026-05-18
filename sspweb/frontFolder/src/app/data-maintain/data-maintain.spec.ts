import {ComponentFixture, TestBed} from "@angular/core/testing";
import {DataMaintain} from "./data-maintain";
import {DataDelivery} from "../data-delivery/data-delivery";

describe('DataMaintain', () => {
  let component: DataDelivery;
  let fixture: ComponentFixture<DataMaintain>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DataMaintain]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataMaintain);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
