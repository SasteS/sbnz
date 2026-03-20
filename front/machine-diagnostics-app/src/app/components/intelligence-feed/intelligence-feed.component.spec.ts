import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IntelligenceFeedComponent } from './intelligence-feed.component';

describe('IntelligenceFeedComponent', () => {
  let component: IntelligenceFeedComponent;
  let fixture: ComponentFixture<IntelligenceFeedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IntelligenceFeedComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IntelligenceFeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
