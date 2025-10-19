import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateMachineDialogComponent } from './create-machine-dialog.component';

describe('CreateMachineDialogComponent', () => {
  let component: CreateMachineDialogComponent;
  let fixture: ComponentFixture<CreateMachineDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateMachineDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateMachineDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
