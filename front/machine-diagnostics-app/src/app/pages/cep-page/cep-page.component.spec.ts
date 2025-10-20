import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CepPageComponent } from './cep-page.component';

describe('CepPageComponent', () => {
  let component: CepPageComponent;
  let fixture: ComponentFixture<CepPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CepPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CepPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
