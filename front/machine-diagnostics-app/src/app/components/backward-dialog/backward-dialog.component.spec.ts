import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackwardDialogComponent } from './backward-dialog.component';

describe('BackwardDialogComponent', () => {
  let component: BackwardDialogComponent;
  let fixture: ComponentFixture<BackwardDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BackwardDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BackwardDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
