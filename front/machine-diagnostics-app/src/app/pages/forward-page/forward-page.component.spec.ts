import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ForwardPageComponent } from './forward-page.component';

describe('ForwardPageComponent', () => {
  let component: ForwardPageComponent;
  let fixture: ComponentFixture<ForwardPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ForwardPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ForwardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
