import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateMachineDialogComponent } from '../../components/create-machine-dialog/create-machine-dialog.component';
import { Machine } from '../../models/Machine';

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.component.html',
  styleUrls: ['./machines-page.component.css']
})
export class MachinesPageComponent implements OnInit {
  machines: Machine[] = [];
  displayedColumns = ['name', 'status'];

  constructor(private service: MachineService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadMachines();
  }

  loadMachines(): void {
    this.service.getAll().subscribe((data) => (this.machines = data));
  }

  openDialog(): void {
    const dialogRef = this.dialog.open(CreateMachineDialogComponent);

    dialogRef.afterClosed().subscribe((name: string) => {
      if (name) {
        this.service.create(name).subscribe(() => this.loadMachines());
      }
    });
  }
}
