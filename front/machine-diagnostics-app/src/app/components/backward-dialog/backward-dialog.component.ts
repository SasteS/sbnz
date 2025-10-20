import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-backward-dialog',
  templateUrl: './backward-dialog.component.html',
})
export class BackwardDialogComponent {
  hypotheses = ['Overheat', 'BearingFault', 'ElectricalOverload'];
  selectedHypothesis: string = '';

  constructor(
    public dialogRef: MatDialogRef<BackwardDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  confirm() {
    if (this.selectedHypothesis) {
      this.dialogRef.close(this.selectedHypothesis);
    }
  }

  cancel() {
    this.dialogRef.close();
  }
}
