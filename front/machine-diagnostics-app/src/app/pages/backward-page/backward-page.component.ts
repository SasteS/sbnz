import { Component, OnInit } from '@angular/core';
import { Machine } from '../../models/Machine';
import { MachineService } from '../../services/machine.service';
import { MatDialog } from '@angular/material/dialog';
import { BackwardDialogComponent } from '../../components/backward-dialog/backward-dialog.component';

@Component({
  selector: 'app-backward-page',
  templateUrl: './backward-page.component.html',
  styleUrls: ['./backward-page.component.css'] // <-- should be plural
})
export class BackwardPageComponent implements OnInit {
  machines: Machine[] = [];
  results: any[] = []; // now holds BackwardResultDTO[]
  logs: string[] = []; // ✅ added this
  loading = false;

  constructor(private machineService: MachineService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadMachines();
  }

  loadMachines() {
    this.machineService.getAll().subscribe({
      next: (data) => (this.machines = data),
      error: (err) => console.error(err)
    });
  }

  openBackwardDialog(machine: Machine) {
    const dialogRef = this.dialog.open(BackwardDialogComponent, {
      width: '400px',
      data: { machine }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.runBackward(machine, result);
      }
    });
  }

  runBackward(machine: Machine, hypothesis: string) {
    this.loading = true;
    this.machineService.runBackwardChaining(machine.id, hypothesis).subscribe({
      next: (data) => {
        this.results = data.results;
        this.logs = data.logs || []; // ✅ logs now works
        this.loading = false;
        // this.machines = data.machines;
        console.log("RESULTS:", this.results);
        console.log("LOGS:", this.logs);
        // console.log("MACHINES:", data.machines);
        // console.log("PROVEN:", data.results.proven);
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }
}
