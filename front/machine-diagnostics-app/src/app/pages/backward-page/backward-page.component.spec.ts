import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackwardPageComponent } from './backward-page.component';

describe('BackwardPageComponent', () => {
  let component: BackwardPageComponent;
  let fixture: ComponentFixture<BackwardPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [BackwardPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BackwardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
