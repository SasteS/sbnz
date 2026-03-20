import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReasoningDetailsDialogComponent } from './reasoning-details-dialog.component';

describe('ReasoningDetailsDialogComponent', () => {
  let component: ReasoningDetailsDialogComponent;
  let fixture: ComponentFixture<ReasoningDetailsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReasoningDetailsDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReasoningDetailsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
