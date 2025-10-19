import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MachineService } from '../../services/machine.service';
import { EditMachineDialogComponent } from '../../components/edit-machine-dialog/edit-machine-dialog.component';
import { Machine } from '../../models/Machine';

@Component({
  selector: 'app-forward-page',
  templateUrl: './forward-page.component.html',
  styleUrls: ['./forward-page.component.css']
})
export class ForwardPageComponent implements OnInit {
  machines: Machine[] = [];

  constructor(private machineService: MachineService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.fetchAllMachines();
  }

  fetchAllMachines(): void {
    this.machineService.getAll().subscribe(data => (this.machines = data));
  }

  randomizeSensors(machine: Machine): Machine {
    return {
      ...machine,
      temperature: machine.temperature + (Math.random() * 10 - 5),
      vibration: machine.vibration + (Math.random() * 2 - 1),
      currentPercentOfRated: machine.currentPercentOfRated + (Math.random() * 10 - 5),
    };
  }

  runBatch(): void {
    const randomized = this.machines.map(m => this.randomizeSensors(m));

    this.machineService.runForwardBatch(randomized).subscribe(updated => {
      this.machines = updated;
    });
  }

  openDialog(machine: Machine): void {
    const dialogRef = this.dialog.open(EditMachineDialogComponent, {
      width: '400px',
      data: { ...machine }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.machineService.runForwardRules(result).subscribe(updated => {
          Object.assign(machine, updated);
          console.log(machine);    
          // this.fetchAllMachines();
        });
      }
    });
  }
}
