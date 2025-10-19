import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-create-machine-dialog',
  templateUrl: './create-machine-dialog.component.html',
  styleUrls: ['./create-machine-dialog.component.css']
})
export class CreateMachineDialogComponent {
  name = '';

  constructor(private dialogRef: MatDialogRef<CreateMachineDialogComponent>) {}

  cancel(): void {
    this.dialogRef.close();
  }

  create(): void {
    if (this.name.trim()) {
      this.dialogRef.close(this.name.trim());
    }
  }
}
