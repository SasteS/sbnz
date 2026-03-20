import { Component, OnInit } from '@angular/core';
import { MachineService } from '../../services/machine.service';
import { MatDialog } from '@angular/material/dialog';
import { CreateMachineDialogComponent } from '../../components/create-machine-dialog/create-machine-dialog.component';
import { Machine } from '../../models/Machine';
import { WebsocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-machines-page',
  templateUrl: './machines-page.component.html',
  styleUrls: ['./machines-page.component.css']
})
export class MachinesPageComponent implements OnInit {
  machines: Machine[] = [];
  displayedColumns = ['name', 'status', 'temperature', 'vibration', 'current'];

  constructor(private service: MachineService, private dialog: MatDialog, private websocketService: WebsocketService) {}

  ngOnInit(): void {
    this.loadMachines();

    
     // Listen for real-time heartbeat updates
    this.websocketService.machineUpdates$.subscribe((updatedMachine: Machine) => {
        const index = this.machines.findIndex(m => m.id === updatedMachine.id);
        if (index !== -1) {
            // 1. Update the data
            this.machines[index] = updatedMachine;
            
            // 2. TRIGGER REAL-TIME UPDATE
            // This replaces the array with a new copy, forcing Angular to re-render the row
            this.machines = [...this.machines]; 
            
            console.log("UI Updated for: " + updatedMachine.name);
        }
    });
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
