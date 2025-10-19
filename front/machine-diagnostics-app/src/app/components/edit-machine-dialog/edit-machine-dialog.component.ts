// edit-machine-dialog.component.ts

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Machine } from '../../models/Machine';

@Component({
  selector: 'app-edit-machine-dialog',
  templateUrl: './edit-machine-dialog.component.html'
})
export class EditMachineDialogComponent {
  
  public contexts: string[] = ['NORMAL', 'IDLE', 'HIGH_LOAD', 'POST_MAINTENANCE']; 

  constructor(
    public dialogRef: MatDialogRef<EditMachineDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Machine
  ) {
    // You should ensure the incoming machine data has a context property
    if (!this.data.context) {
      this.data.context = this.contexts[0];
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onRun(): void {
    this.dialogRef.close(this.data);
  }
}