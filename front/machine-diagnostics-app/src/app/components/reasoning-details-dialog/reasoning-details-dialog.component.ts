import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-reasoning-details-dialog',
  templateUrl: './reasoning-details-dialog.component.html',
  styleUrls: ['./reasoning-details-dialog.component.css']
})
export class ReasoningDetailsDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {}
}